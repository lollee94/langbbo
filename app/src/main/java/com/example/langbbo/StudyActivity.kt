package com.example.langbbo

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.text.*
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.polly.AmazonPollyPresigningClient
import com.amazonaws.services.polly.model.OutputFormat
import com.amazonaws.services.polly.model.SynthesizeSpeechPresignRequest
import com.example.langbbo.modelDTO.StudyDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_study.*
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class StudyActivity : AppCompatActivity() {
    private var time = 0
    private var timerTask: Timer? = null

    var auth: FirebaseAuth? = null
    var firestore: FirebaseFirestore? = null
    var extras: Bundle? = null

    var sView: View? = null
    var sData = ArrayList<StudyDTO>()

    var lThread: Thread? = null
    var client: AmazonPollyPresigningClient? = null

    var uid: String = ""
    var errorCheck: Int = 0
    val answerLength = 12

    var i: Intent? = null
    var mRecognizer: SpeechRecognizer? = null
    var mHandler: Handler? = null
    var resultTextList = ArrayList<String>()

    var situationEt: EditText? = null
    var situationTv2: TextView? = null

    var lang: String = ""
    var category: String = ""

    private var player: MediaPlayer? = null
    var playOn: Boolean = false

    var isNotChecking: Boolean = true
    var isNotSuccess: Boolean = true
    var idx : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_study)

        window.statusBarColor = Color.parseColor("#ffffff")

        extras = intent?.extras // bundle???
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val credentialsProvider = CognitoCachingCredentialsProvider(
                this,
                "ap-northeast-2:c903f1b4-7a90-42ff-a53a-df75f34036b2",
                Regions.AP_NORTHEAST_2
        )
        client = AmazonPollyPresigningClient(credentialsProvider)

        uid = auth?.currentUser?.uid!!

        lang = extras?.getString("lang")!!
        category = extras?.getString("category")!!

        mHandler = Handler()
        mRecognizer = SpeechRecognizer.createSpeechRecognizer(this)

        study_upper_lo.visibility = View.GONE
        study_skip_lo.visibility = View.GONE
        study_loading_lo.visibility = View.VISIBLE

        idx = "idx" + category.substring(0,1).toUpperCase() + category.substring(1).toLowerCase()
        // ex> idxReading

        firestore?.collection(Utils.userInfo)?.document(uid)
                ?.collection(Utils.langInfo)?.document(lang)
                ?.collection(Utils.studyInfo)
                ?.orderBy(idx, Query.Direction.ASCENDING)
                ?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    sData.clear()

                    // ?????? ????????? ??? ????????? ?????? ??? ??????????????? ??????????????? ???
                    study_help_lo.visibility = View.GONE
                    study_success_lo.visibility = View.GONE
                    study_fail_lo.visibility = View.GONE


                    if (querySnapshot != null) {

                        for (snapshot in querySnapshot) {
                            val sItem = snapshot.toObject(StudyDTO::class.java)!!

                            if(sItem.catData[category] == true) {
                                sData.add(sItem)
                            }

                        }

                    }


                    study_loading_lo.visibility = View.GONE
                    study_upper_lo.visibility = View.VISIBLE
                    study_skip_lo.visibility = View.VISIBLE

                    //study_info_rl.visibility = View.VISIBLE


                    if (sData.size <= 1){
                        study_contentLack_lo.visibility = View.VISIBLE
                        study_contentLack_lo.bringToFront()
                        study_contentLack_lo.isClickable = true
                    }
                    else {
                        study_contentLack_lo.visibility = View.GONE

                        val middle = sData.size / 2
                        val final = sData.size

                        val item = sData[0] // indicator ?????? ?????? ???

                        var middleItem: StudyDTO
                        var finalItem: StudyDTO
                        if(sData.size == 1){
                            middleItem = sData[0]
                            finalItem = sData[0]
                        }
                        else{
                            middleItem = sData[middle - 1] // ??? ?????? ???
                            finalItem = sData[final - 1]
                        }


                        study_hint_tv.setTextColor(Color.parseColor("#000000"))
                        study_hint_tv.text = "?????? ??????"
                        study_hint_tv.typeface = Typeface.DEFAULT_BOLD
                        study_hint_tv.setOnClickListener {

                            // ????????? ????????? ????????? ??????, isNotChecking??? ?????? ????????????.
                            if(isNotSuccess) {
                                if (isNotChecking) {
                                    isNotChecking = false
                                }
                            }

                            val intent = Intent(this, HintActivity::class.java)
                            val args = Bundle()
                            args.putString("studyId", item.studyId)
                            args.putString("lang", item.lang)
                            args.putString("category", category)
                            intent.putExtras(args)
                            startActivity(intent)
                        }

                        study_situation_tv.text = item.situationExplain

                        study_skip_tv.setOnClickListener {
                            goToNext(item, middleItem, finalItem, true)
                        }


                        study_main_lo.removeAllViews()
                        for (sNum in 0 until item.sentenceList.size) {


                            // 4??? ?????? ?????? ?????? ????????? ?????? ????????? ??????.


                            if (sNum != (item.sentenceNum - 1)) {
                                var situationTv1 = TextView(this)
                                var params1 = LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.WRAP_CONTENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                )
                                //params.marginEnd = dpToPx(12)
                                with(situationTv1) {
                                    layoutParams = params1
                                    setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8))
                                    textSize = 19f
                                    setTextColor(Color.parseColor("#90000000"))
                                    text = item.sentenceList[sNum]
                                    typeface = Typeface.DEFAULT_BOLD
                                }

                                study_main_lo.addView(situationTv1)
                            } else {
                                // ?????? ????????? ????????? ???.


                                situationTv2 = TextView(this)
                                var params2 = LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.WRAP_CONTENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                )


                                situationEt = EditText(this)
                                var params3 = LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                )

                                // ?????? ??????
                                val content = SpannableString(item.sentenceKorean)
                                val contentHelp = SpannableString(item.sentenceKorean)
                                val contentReading = SpannableString(item.sentenceList[item.sentenceNum - 1])

                                content.setSpan(UnderlineSpan(), 0, content.length, 0)
                                contentReading.setSpan(UnderlineSpan(), 0, contentReading.length, 0)

                                if (item.coreExpression.size >= 2) {
                                    val word = item.coreExpression[1]
                                    val wordReading = item.coreExpression[0]
                                    if (content.indexOf(word) > -1) {
                                        val start = content.indexOf(word)
                                        val end = start + word.length

                                        content.setSpan(
                                                ForegroundColorSpan(Color.parseColor("#0d47a1")),
                                                start,
                                                end,
                                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                                        )

                                        contentHelp.setSpan(
                                                ForegroundColorSpan(Color.parseColor("#800d47a1")),
                                                start,
                                                end,
                                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                                        )
                                    }


                                    if (contentReading.indexOf(wordReading) > -1) {
                                        val startReading = contentReading.indexOf(wordReading)
                                        val endReading = startReading + wordReading.length

                                        contentReading.setSpan(
                                                ForegroundColorSpan(Color.parseColor("#0d47a1")),
                                                startReading,
                                                endReading,
                                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                                        )
                                    }


                                }

                                // group.split("_")[1]
                                if(category == "listening") {
                                    //TextView ?????? ??????!
                                    // listening ??? ?????? ! ! !

                                    tts(item)
                                    audio(item)

                                    study_stt_lo.visibility = View.GONE
                                    study_listening_lo.visibility = View.VISIBLE
                                    study_help_tv.visibility = View.GONE

                                    val listeningText = SpannableString("????????? ?????? ??????????????????.")
                                    listeningText.setSpan(UnderlineSpan(), 0, listeningText.length, 0)


                                    // ????????? ??????
                                    study_timer_lo.visibility = View.VISIBLE
                                    val timeLength = (500 + (item.sentenceList[item.sentenceNum - 1].length / 4) * 100) * 4
                                    time = timeLength
                                    val maxSecString = "${timeLength / 100}"
                                    val maxMilliString = "." + "${timeLength % 100 + 100}".substring(1,3)
                                    study_timerSec_tv.text = maxSecString
                                    study_timerMilli_tv.text = maxMilliString

                                    time = timeLength
                                    timerTask = kotlin.concurrent.timer(period = 10) {

                                        // time??? 0??? ??? ????????? -10??? ????????????, -0.1?????? ????????? ???.
                                        if(time > 0) {
                                            time--
                                        }
                                        val sec = time / 100
                                        val milli = time % 100

                                        runOnUiThread {
                                            study_timerSec_tv.text = "$sec"
                                            study_timerMilli_tv.text = "." + "${milli + 100}".substring(1,3)


                                            if(sec == 0 && milli == 0) {

                                                pause() // ????????? ?????????.

                                                study_timerSec_tv.text = "0"
                                                study_timerMilli_tv.text = ".00"

                                                isNotChecking = false // ?????? ???????????????, ?????? X

                                                //val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                                                //imm.hideSoftInputFromWindow(situationEt!!.windowToken, 0)
                                            }
                                        }
                                    }
                                    // ????????? ???


                                    with(situationTv2!!) {
                                        layoutParams = params2
                                        setPadding(dpToPx(5), dpToPx(5), dpToPx(5), dpToPx(5))
                                        textSize = 26f
                                        setTextColor(Color.parseColor("#000000"))
                                        text = listeningText
                                        typeface = Typeface.DEFAULT_BOLD
                                        setOnClickListener {

                                            situationTv2!!.visibility = View.GONE
                                            situationEt!!.visibility = View.VISIBLE
                                            situationEt!!.setFocusableInTouchMode(true)
                                            situationEt!!.requestFocus()

                                            val imm =
                                                    context!!.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                                            imm.showSoftInput(
                                                    situationEt,
                                                    InputMethodManager.SHOW_IMPLICIT
                                            )
                                        }
                                    }


                                    with(situationEt!!) {

                                        layoutParams = params3
                                        setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8))
                                        textSize = 26f
                                        setTextColor(Color.parseColor("#000000"))
                                        //hint = item.answer //item.situation[sNum]
                                        typeface = Typeface.DEFAULT_BOLD
                                        visibility = View.GONE
                                        //setEms(item.answer.length)

                                        // ????????? ????????? ?????? ???????????? ?????????!
                                        imeOptions = EditorInfo.IME_ACTION_DONE
                                        inputType = InputType.TYPE_CLASS_TEXT
                                        maxLines = 3
                                        setHorizontallyScrolling(false) // edittext ??????????????? ?????????(TYPE_CLASS_TEXT ?????? ?????????)

                                        setOnEditorActionListener(object :
                                                TextView.OnEditorActionListener {
                                            override fun onEditorAction(
                                                    v: TextView,
                                                    actionId: Int,
                                                    event: KeyEvent?
                                            ): Boolean {
                                                if (actionId == EditorInfo.IME_ACTION_DONE) {
                                                    study_help_tv.visibility = View.GONE

                                                    situationTv2!!.visibility = View.VISIBLE
                                                    situationEt!!.visibility = View.GONE

                                                    getResultText(
                                                            situationEt!!.text.toString().trim(),
                                                            item.sentenceList[item.sentenceNum - 1],
                                                            item.lang,
                                                            situationTv2!!,
                                                            situationEt!!,
                                                            item,
                                                            middleItem,
                                                            finalItem
                                                    )

                                                    //Toast.makeText(context!!, "??????!", Toast.LENGTH_LONG).show()
                                                    return true
                                                }

                                                return false
                                            }
                                        })

                                    }


                                    study_main_lo.addView(situationTv2)
                                    study_main_lo.addView(situationEt)
                                }
                                else if(category == "reading" || category == "word"){
                                    // ??????

                                    study_success_lo.visibility = View.VISIBLE
                                    // ?????? ?????? ?????? ??????. ????????? ?????? ????????? ????????? ??? ???.

                                    var sentenceLength = 0
                                    for(sentence in item.sentenceList){ sentenceLength += sentence.length }

                                    // ????????? ??????
                                    study_timer_lo.visibility = View.VISIBLE
                                    val timeLength = (500 + (sentenceLength / 11) * 100) * 4
                                    time = timeLength
                                    val maxSecString = "${timeLength / 100}"
                                    val maxMilliString = "." + "${timeLength % 100 + 100}".substring(1,3)
                                    study_timerSec_tv.text = maxSecString
                                    study_timerMilli_tv.text = maxMilliString

                                    time = timeLength
                                    timerTask = kotlin.concurrent.timer(period = 10) {
                                        // time??? 0??? ??? ????????? -10??? ????????????, -0.1?????? ????????? ???.
                                        if(time > 0) {
                                            time--
                                        }
                                        val sec = time / 100
                                        val milli = time % 100

                                        runOnUiThread {
                                            study_timerSec_tv.text = "$sec"
                                            study_timerMilli_tv.text = "." + "${milli + 100}".substring(1,3)
                                            if(sec == 0 && milli == 0) {

                                                pause() // ????????? ?????????.

                                                study_timerSec_tv.text = "0"
                                                study_timerMilli_tv.text = ".00"

                                                isNotChecking = false // ?????? ???????????????, ?????? X


                                            }
                                        }
                                    }

                                    ////////////////// ????????? ??? ///////////////////////////

                                    study_stt_lo.visibility = View.GONE
                                    study_again_tv.visibility = View.GONE
                                    study_next_tv.setOnClickListener {
                                        goToNext(item, middleItem, finalItem, false)
                                    }

                                    with(situationTv2!!) {
                                        layoutParams = params2
                                        setPadding(dpToPx(5), dpToPx(5), dpToPx(5), dpToPx(5))
                                        textSize = 26f
                                        // 0d47a1 ?????????
                                        setTextColor(Color.parseColor("#000000"))
                                        text = contentReading//item.sentenceList[item.sentenceNum - 1]
                                        typeface = Typeface.DEFAULT_BOLD
                                    }
                                    study_main_lo.addView(situationTv2)

                                }
                                else{
                                    // ?????? -> ?????????, ??????

                                    study_timer_lo.visibility = View.VISIBLE

                                    val timeLength = (500 + (item.sentenceList[item.sentenceNum - 1].length / 4) * 100) * 3
                                    time = timeLength
                                    val maxSecString = "${timeLength / 100}"
                                    val maxMilliString = "." + "${timeLength % 100 + 100}".substring(1,3)
                                    study_timerSec_tv.text = maxSecString
                                    study_timerMilli_tv.text = maxMilliString

                                    time = timeLength
                                    timerTask = kotlin.concurrent.timer(period = 10) {

                                        // time??? 0??? ??? ????????? -10??? ????????????, -0.1?????? ????????? ???.
                                        if(time > 0) {
                                            time--
                                        }
                                        val sec = time / 100
                                        val milli = time % 100

                                        runOnUiThread {
                                            study_timerSec_tv.text = "$sec"
                                            study_timerMilli_tv.text = "." + "${milli + 100}".substring(1,3)


                                            if(sec == 0 && milli == 0) {

                                                pause() // ????????? ?????????.

                                                study_timerSec_tv.text = "0"
                                                study_timerMilli_tv.text = ".00"

                                                isNotChecking = false // ?????? ???????????????, ?????? X

                                                //val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                                                //imm.hideSoftInputFromWindow(situationEt!!.windowToken, 0)
                                            }
                                        }
                                    }


                                    with(situationTv2!!) {
                                        layoutParams = params2
                                        setPadding(dpToPx(5), dpToPx(5), dpToPx(5), dpToPx(5))
                                        textSize = 26f
                                        setTextColor(Color.parseColor("#000000"))
                                        text = content
                                        typeface = Typeface.DEFAULT_BOLD
                                        setOnClickListener {

                                            study_help_lo.visibility = View.VISIBLE
                                            study_help_tv.text = contentHelp //item.answer

                                            situationTv2!!.visibility = View.GONE
                                            situationEt!!.visibility = View.VISIBLE
                                            situationEt!!.setFocusableInTouchMode(true)
                                            situationEt!!.requestFocus()

                                            val imm =
                                                    context!!.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                                            imm.showSoftInput(
                                                    situationEt,
                                                    InputMethodManager.SHOW_IMPLICIT
                                            )
                                        }
                                    }


                                    with(situationEt!!) {

                                        layoutParams = params3
                                        setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8))
                                        textSize = 26f
                                        setTextColor(Color.parseColor("#000000"))
                                        //hint = item.answer //item.situation[sNum]
                                        typeface = Typeface.DEFAULT_BOLD
                                        visibility = View.GONE
                                        //setEms(item.answer.length)

                                        // ????????? ????????? ?????? ???????????? ?????????!
                                        imeOptions = EditorInfo.IME_ACTION_DONE
                                        inputType = InputType.TYPE_CLASS_TEXT
                                        maxLines = 3
                                        setHorizontallyScrolling(false) // edittext ??????????????? ?????????(TYPE_CLASS_TEXT ?????? ?????????)

                                        setOnEditorActionListener(object :
                                                TextView.OnEditorActionListener {
                                            override fun onEditorAction(
                                                    v: TextView,
                                                    actionId: Int,
                                                    event: KeyEvent?
                                            ): Boolean {
                                                if (actionId == EditorInfo.IME_ACTION_DONE) {

                                                    //pause()

                                                    study_help_lo.visibility = View.GONE
                                                    situationTv2!!.visibility = View.VISIBLE
                                                    situationEt!!.visibility = View.GONE

                                                    getResultText(
                                                            situationEt!!.text.toString().trim(),
                                                            item.sentenceList[item.sentenceNum - 1],
                                                            item.lang,
                                                            situationTv2!!,
                                                            situationEt!!,
                                                            item,
                                                            middleItem,
                                                            finalItem
                                                    )

                                                    //Toast.makeText(context!!, "??????!", Toast.LENGTH_LONG).show()
                                                    return true
                                                }

                                                return false
                                            }
                                        })

                                        addTextChangedListener(object : TextWatcher {
                                            override fun afterTextChanged(p0: Editable?) {

                                                study_help_lo.visibility = View.VISIBLE
                                                study_help_tv.text = contentHelp //item.answer

                                                /*
                                            if(text.toString().trim() == ""){
                                                sView!!.study_help_tv.visibility = View.GONE
                                            }
                                            else{
                                                sView!!.study_help_tv.visibility = View.VISIBLE
                                                sView!!.study_help_tv.text = content2 //item.answer
                                            }

                                             */
                                            }

                                            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                                                study_help_lo.visibility = View.GONE
                                            }

                                            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                                                study_help_lo.visibility = View.GONE

                                            }
                                        })
                                    }
                                    study_main_lo.addView(situationTv2)
                                    study_main_lo.addView(situationEt)


                                }


                            }
                        }


                        /*
                        var situationTv3 = TextView(this)
                        var params3 = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        with(situationTv3) {
                            layoutParams = params3
                            setPadding(dpToPx(5), dpToPx(5), dpToPx(5), dpToPx(5))
                            textSize = 21f
                            setTextColor(Color.parseColor("#50000000"))
                            //text = "#${Utils.enGroupHash()[item.group]}"
                            typeface = Typeface.DEFAULT_BOLD
                        }
                        study_main_lo.addView(situationTv3)
                         */


                        // ????????????
                        study_stt_lo.setOnClickListener {


                            //resultText = null
                            resultTextList.clear()

                            // ?????? ??? ?????????
                            study_black_lo.visibility = View.VISIBLE
                            study_black_lo.bringToFront()

                            study_black_lo.setOnClickListener {
                                mHandler!!.removeMessages(0)
                                study_black_lo.visibility = View.GONE
                                mRecognizer?.destroy() // cancle > destroy
                            }

                            i =
                                    Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                            i!!.putExtra(
                                    RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS,
                                    5000
                            )
                            i!!.putExtra(
                                    RecognizerIntent.EXTRA_LANGUAGE,
                                    item.lang
                            )
                            i!!.putExtra(
                                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                    item.lang
                            )
                            i!!.putExtra(
                                    RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
                                    item.lang
                            )
                            i!!.putExtra(
                                    RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES,
                                    item.lang
                            )
                            i!!.putExtra(
                                    RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE,
                                    item.lang
                            )
                            i!!.putExtra(
                                    RecognizerIntent.EXTRA_CALLING_PACKAGE,
                                    item.lang
                            )
                            i!!.putExtra(
                                    RecognizerIntent.EXTRA_RESULTS,
                                    item.lang
                            )

                            mRecognizer!!.setRecognitionListener(listener)


                            if (ContextCompat.checkSelfPermission(
                                            this!!,
                                            Manifest.permission.RECORD_AUDIO
                                    ) != PackageManager.PERMISSION_GRANTED
                            ) {
                                ActivityCompat.requestPermissions(this, arrayOf(
                                        Manifest.permission.RECORD_AUDIO), 1)

                                /*requestPermissions(
                                    arrayOf(Manifest.permission.RECORD_AUDIO),
                                    MY_PERMISSIONS_RECORD_AUDIO
                                )*/
                            } else {
                                try {

                                    mRecognizer!!.startListening(i)
                                    try {

                                        var num = 0
                                        mHandler?.postDelayed(object :
                                                Runnable {
                                            override fun run() {

                                                if (resultTextList.size == 0 && num <= 20) {
                                                    num += 1

                                                    mHandler!!.postDelayed(
                                                            this,
                                                            400
                                                    )

                                                } else if (num > 20) {

                                                    mHandler!!.removeMessages(
                                                            0
                                                    )
                                                    study_black_lo.visibility =
                                                            View.GONE

                                                    Toast.makeText(
                                                            applicationContext,
                                                            "?????? ??? ????????? ???????????????!",
                                                            Toast.LENGTH_LONG
                                                    ).show()
                                                } else {

                                                    mHandler!!.removeMessages(
                                                            0
                                                    )

                                                    getResult(
                                                            resultTextList,
                                                            item.sentenceList[item.sentenceNum - 1],
                                                            item.lang,
                                                            item.coreExpression
                                                    )

                                                }
                                            }

                                        }, 400)
                                    } catch (e: KotlinNullPointerException) {
                                        mHandler!!.removeMessages(0)
                                        study_black_lo.visibility =
                                                View.GONE

                                        Toast.makeText(
                                                this,
                                                "?????? ??????????????????.",
                                                Toast.LENGTH_LONG
                                        ).show()
                                    }
                                } catch (e: SecurityException) {
                                    e.printStackTrace()
                                }
                            }
                        }// ?????? ?????? setOnClick


                    } // sData.size > 0

                }

    }




    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return Math.round(dp.toFloat() * density)
    }



    private fun getResult( // ?????? ?????? ?????????
            sss1List: ArrayList<String>,
            sss2: String,
            lang: String,
            coreVocaList: ArrayList<String>) {

        val costList = ArrayList<Int>()
        for(i in 0 until sss1List.size) {
            val longStrLen : Int
            val shortStrLen : Int
            var allcosts : Int

            val sss1 = sss1List[i]

            val ss1 =
                    sss1.toLowerCase().replace(".", "").replace("?", "").replace("!", "")
                            .replace("~", "")
                            .replace(",", "").replace("-", "")
            val ss2 =
                    sss2.toLowerCase().replace(".", "").replace("?", "").replace("!", "")
                            .replace("~", "")
                            .replace(",", "").replace("-", "")

            val s1 = numbersToWords(ss1)
            val s2 = numbersToWords(ss2)

            if (s1.length >= s2.length) {
                longStrLen = s1.length
                shortStrLen = s2.length
            } else {
                longStrLen = s2.length
                shortStrLen = s1.length
            }

            var cost = IntArray(longStrLen + 1) // 18??? ??? list
            var newcost = IntArray(longStrLen + 1)
            var checkValue = IntArray(shortStrLen)

            for (i in 0..longStrLen) {
                cost[i] = i
            }
            for (j in 1..shortStrLen) {
                // ?????? Cost??? 1, 2, 3, 4...
                newcost[0] = j // ??? ????????? ????????? ??????.
                for (i in 1..longStrLen) {
                    // ????????? ????????? 0, ????????? 1
                    var match = 0

                    if (s1.length >= s2.length) {
                        if (s1[i - 1] != s2[j - 1]) {
                            match = 1
                        }
                    } else {
                        if (s1[j - 1] != s2[i - 1]) {
                            match = 1
                        }
                    }

                    // ??????, ??????, ????????? ????????? ????????????.
                    val replace = cost[i - 1] + match
                    val insert = cost[i] + 1
                    val delete = newcost[i - 1] + 1
                    // ?????? ?????? ?????? ????????? ?????????.
                    newcost[i] = Math.min(Math.min(insert, delete), replace)

                    if (j == i) {
                        checkValue[j - 1] = newcost[i]
                    }

                } // ?????? ????????? & ??? ????????? ?????????
                val temp = cost
                cost = newcost
                newcost = temp
            }
            allcosts = cost[longStrLen]
            costList.add(allcosts)
        }
        // costList ??? ????????????. ????????? ????????????!


        val minPosition = costList.indexOf(costList.min())
        val sss1 = sss1List[minPosition]

        val longStrLen : Int
        val shortStrLen : Int
        var allcosts : Int

        val ss1 =
                sss1.toLowerCase().replace(".", "").replace("?", "").replace("!", "")
                        .replace("~", "")
                        .replace(",", "").replace("-", "")
        val ss2 =
                sss2.toLowerCase().replace(".", "").replace("?", "").replace("!", "")
                        .replace("~", "")
                        .replace(",", "").replace("-", "")

        var coreVocaCheck = false
        var costPercent: Double
        var score: Double
        var finalScore: Int

        if(ss2.length <= answerLength && lang == "en") {

            if(ss1.substring(0, 1) == ss2.substring(0,1)) {
                finalScore = 99
                coreVocaCheck = true
            }
            else{
                finalScore = 1
            }

            var ss = SpannableString(ss1)

            /*
            val splitCoreVocaList = Utils.makePureArray(coreVocaList[2].split(","))
            for (coreVoca in splitCoreVocaList) {

                val pureCoreVoca = coreVoca.toLowerCase().replace(".", "").replace("?", "").replace("!", "")
                    .replace("~", "").replace(",", "").replace("-", "")

                if (ss.toString().toLowerCase().indexOf(pureCoreVoca.toLowerCase()) > -1) {
                    val start = ss.toString().toLowerCase().indexOf(pureCoreVoca.toLowerCase())
                    val end = start + pureCoreVoca.length
                    ss.setSpan(
                        ForegroundColorSpan(Color.parseColor("#ff8c00")),
                        start,
                        end,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    ss.setSpan(StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

                    coreVocaCheck = true
                }
            }

             */

            // ?????? ?????? ????????????, TextView??? ?????????, EditText??? ??????.
            study_black_lo.visibility = View.GONE
            situationTv2!!.visibility = View.GONE
            situationEt!!.visibility = View.VISIBLE
            situationEt!!.setText(ss)

        }
        else{

            val s1 = numbersToWords(ss1)
            val s2 = numbersToWords(ss2)

            if (s1.length >= s2.length) {
                longStrLen = s1.length
                shortStrLen = s2.length
            } else {
                longStrLen = s2.length
                shortStrLen = s1.length
            }

            var cost = IntArray(longStrLen + 1) // 18??? ??? list
            var newcost = IntArray(longStrLen + 1)
            var checkValue = IntArray(shortStrLen)

            for (i in 0..longStrLen) {
                cost[i] = i
            }
            for (j in 1..shortStrLen) {
                // ?????? Cost??? 1, 2, 3, 4...
                newcost[0] = j // ??? ????????? ????????? ??????.
                for (i in 1..longStrLen) {
                    // ????????? ????????? 0, ????????? 1
                    var match = 0

                    if (s1.length >= s2.length) {
                        if (s1[i - 1] != s2[j - 1]) {
                            match = 1
                        }
                    } else {
                        if (s1[j - 1] != s2[i - 1]) {
                            match = 1
                        }
                    }

                    // ??????, ??????, ????????? ????????? ????????????.
                    val replace = cost[i - 1] + match
                    val insert = cost[i] + 1
                    val delete = newcost[i - 1] + 1
                    // ?????? ?????? ?????? ????????? ?????????.
                    newcost[i] = Math.min(Math.min(insert, delete), replace)

                    if (j == i) {
                        checkValue[j - 1] = newcost[i]
                    }

                } // ?????? ????????? & ??? ????????? ?????????
                val temp = cost
                cost = newcost
                newcost = temp
            }
            allcosts = cost[longStrLen]

            if (allcosts > longStrLen) { allcosts = longStrLen }
            costPercent = allcosts * 1.0 / longStrLen * 1.0
            score = 1.0 - costPercent
            finalScore = Math.round(score * 100).toInt()


            var ss = SpannableString(ss1)
            if (lang == "en") {
                ss = SpannableString(ss.substring(0, 1).toUpperCase() + ss.substring(1)) // ??? ?????????

                val checkArray = ArrayList<Int>()
                for (i in 1 until shortStrLen) {
                    if (checkValue[i] - checkValue[i - 1] == 1) {
                        checkArray.add(i)
                    }
                }

                val wArray = ArrayList<String>()
                val sArray = s1.split(" ")
                for (i in 0 until sArray.size) {
                    val word = sArray[i]
                    val start = s1.indexOf(word)
                    val end = start + word.length
                    for (j in 0 until checkArray.size) {
                        if (start <= checkArray[j] && checkArray[j] < end) {
                            wArray.add(word)
                        }
                    }
                }

                val rArray = ArrayList<String>()
                for (i in 0 until wArray.size) {
                    if (!rArray.contains(wArray[i])) {
                        rArray.add(wArray[i])
                    }
                }

                /*
                for (i in 0 until rArray.size) {
                    if (ss1.indexOf(rArray[i]) > -1) {
                        val start = ss1.indexOf(rArray[i])
                        val end = start + rArray[i].length
                        ss.setSpan(
                            ForegroundColorSpan(Color.parseColor("#ff0000")),
                            start,
                            end,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                }

                 */
            }


            /*
            val splitCoreVocaList = Utils.makePureArray(coreVocaList[0].split(","))
            for (coreVoca in splitCoreVocaList) {

                val pureCoreVoca = coreVoca.toLowerCase().replace(".", "").replace("?", "").replace("!", "")
                    .replace("~", "").replace(",", "").replace("-", "")

                if (ss.toString().toLowerCase().indexOf(pureCoreVoca.toLowerCase()) > -1) {

                    val start = ss.toString().toLowerCase().indexOf(pureCoreVoca.toLowerCase())
                    val end = start + pureCoreVoca.length
                    ss.setSpan(
                        ForegroundColorSpan(Color.parseColor("#ff8c00")),
                        start,
                        end,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    ss.setSpan(StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

                    coreVocaCheck = true
                }
            }
             */

            // ?????? ?????? ????????????, TextView??? ?????????, EditText??? ??????.
            study_black_lo.visibility = View.GONE
            situationTv2!!.visibility = View.GONE
            situationEt!!.visibility = View.VISIBLE
            situationEt!!.setText(ss)

        }


        /*
        if (finalScore >= 60 || coreVocaCheck) {
            Toast.makeText(context!!, "??????!", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(context!!, "?????? ??????????????????!", Toast.LENGTH_LONG).show()
        }
         */


    }


    private fun getResultText(sss1: String, sss2: String, lang: String, answerTv: TextView, answerEt: EditText, sData: StudyDTO, mData: StudyDTO, fData: StudyDTO) {

        val longStrLen : Int
        val shortStrLen : Int
        var allcosts : Int

        val s1 =
                sss1.toLowerCase().replace(".", "").replace("?", "").replace("!", "")
                        .replace("~", "").replace("???", "").replace("???", "")
                        .replace(",", "").replace("-", "")
        val s2 =
                sss2.toLowerCase().replace(".", "").replace("?", "").replace("!", "")
                        .replace("~", "").replace("???", "").replace("???", "")
                        .replace(",", "").replace("-", "")


        var costPercent: Double
        var score: Double
        var finalScore: Int

        if (s1.length >= s2.length) {
            longStrLen = s1.length
            shortStrLen = s2.length
        } else {
            longStrLen = s2.length
            shortStrLen = s1.length
        }

        var cost = IntArray(longStrLen + 1) // 18??? ??? list
        var newcost = IntArray(longStrLen + 1)
        var checkValue = IntArray(shortStrLen)

        for (i in 0..longStrLen) {
            cost[i] = i
        }
        for (j in 1..shortStrLen) {
            // ?????? Cost??? 1, 2, 3, 4...
            newcost[0] = j // ??? ????????? ????????? ??????.
            for (i in 1..longStrLen) {
                // ????????? ????????? 0, ????????? 1
                var match = 0

                if (s1.length >= s2.length) {
                    if (s1[i - 1] != s2[j - 1]) {
                        match = 1
                    }
                } else {
                    if (s1[j - 1] != s2[i - 1]) {
                        match = 1
                    }
                }

                // ??????, ??????, ????????? ????????? ????????????.
                val replace = cost[i - 1] + match
                val insert = cost[i] + 1
                val delete = newcost[i - 1] + 1
                // ?????? ?????? ?????? ????????? ?????????.
                newcost[i] = Math.min(Math.min(insert, delete), replace)

                if (j == i) {
                    checkValue[j - 1] = newcost[i]
                }

            } // ?????? ????????? & ??? ????????? ?????????
            val temp = cost
            cost = newcost
            newcost = temp
        }
        allcosts = cost[longStrLen]

        if (allcosts > longStrLen) { allcosts = longStrLen }
        costPercent = allcosts * 1.0 / longStrLen * 1.0
        score = 1.0 - costPercent
        finalScore = Math.round(score * 100).toInt()


        var ss = SpannableString(s1)
        //if (lang == "en") {}
        ss = SpannableString(ss.substring(0, 1).toUpperCase() + ss.substring(1)) // ??? ?????????

        val checkArray = ArrayList<Int>()
        for (i in 1 until shortStrLen) {
            if (checkValue[i] - checkValue[i - 1] == 1) {
                checkArray.add(i)
            }
        }

        val wArray = ArrayList<String>()
        val sArray = s1.split(" ")
        for (i in 0 until sArray.size) {
            val word = sArray[i]
            val start = s1.indexOf(word)
            val end = start + word.length
            for (j in 0 until checkArray.size) {
                if (start <= checkArray[j] && checkArray[j] < end) {
                    wArray.add(word)
                }
            }
        }

        val rArray = ArrayList<String>()
        for (i in 0 until wArray.size) {
            if (!rArray.contains(wArray[i])) {
                rArray.add(wArray[i])
            }
        }

        for (i in 0 until rArray.size) {
            if (s1.indexOf(rArray[i]) > -1) {
                val start = s1.indexOf(rArray[i])
                val end = start + rArray[i].length
                ss.setSpan(
                        ForegroundColorSpan(Color.parseColor("#ff0000")),
                        start,
                        end,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
        answerTv.text = ss


        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(answerEt.windowToken, 0)

        var coreExpression = "!langbbo!"
        if(sData.coreExpression.size >= 2){
            coreExpression = sData.coreExpression[0].trim()
        }

        // 90??? ????????? ?????? ?????? ?????? ????????? ???
        if (finalScore >= 90 || sss1.trim() == coreExpression) {

            pause() // ??????????????? ????????? ??????

            var ss = SpannableString(sss2)// s1??? ?????? sss2??? ?????? ????????? ????????????.
            ss.setSpan(
                    ForegroundColorSpan(Color.parseColor("#0000ff")),
                    0,
                    sss2.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            if(sData.coreExpression.size >= 2) {
                val word = sData.coreExpression[0]
                if (ss.indexOf(word) > -1) {
                    val start = ss.indexOf(word)
                    val end = start + word.length
                    ss.setSpan(
                            ForegroundColorSpan(Color.parseColor("#ff8c00")),
                            start,
                            end,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            }

            answerTv.text = ss

            // ?????????
            study_fail_lo.visibility = View.GONE
            study_success_lo.visibility = View.VISIBLE
            study_help_lo.visibility = View.GONE //View.VISIBLE
            answerTv.setOnClickListener {} // ????????? Et??? ????????? ?????? ??????.

            isNotSuccess = false // ??????????????? false??? ??????
            study_next_tv.setOnClickListener {
                goToNext(sData, mData, fData, false)
                // ???????????? ???????????? ??? ???
            }


        } else if(finalScore > 80){
            study_fail_lo.visibility = View.VISIBLE
            study_fail_tv.text = "?????? ??? ?????????!"
        } else if(finalScore > 50){
            study_fail_lo.visibility = View.VISIBLE
            study_fail_tv.text = "??? ?????? ????????????! ????????? ???!"
        } else{
            study_fail_lo.visibility = View.VISIBLE
            study_fail_tv.text = "?????? ???????????? ????????? ??????????????????."
        }
    }


    fun goToNext(sData: StudyDTO, mData: StudyDTO, fData: StudyDTO, isSkip: Boolean){

        if(isNotChecking && !isSkip ){

            // Build ??? => ?????? ???????????? ?????????????
            val builder =
                    androidx.appcompat.app.AlertDialog.Builder(this)
            with(builder) {
                setTitle("?????? ?????? ??????")
                setMessage("?????? ?????? ????????? ?????? ?????? ???????????????! ?????? ?????? ???????????? ?????? ??? ????????????.")
                setPositiveButton("????????????") { dialogInterface, i ->


                    // reading?????? reading false??? ?????????.
                    // ?????? ?????? ????????????

                    val sHashMap = HashMap<String, Any?>()

                    var catHashMap = sData.catData
                    catHashMap[category] = false
                    sHashMap["catData"] = catHashMap


                    firestore?.collection(Utils.userInfo)?.document(uid)
                            ?.collection(Utils.langInfo)?.document(sData.lang)
                            ?.collection(Utils.studyInfo)?.document(sData.studyId)
                            ?.update(sHashMap)


                    // ????????? ???????????????.
                    val completeData = sData

                    var cUid = uid.substring(0, 3)
                    val completeTimeStamp = SimpleDateFormat("yyMMddHHmmss").format(Date())
                    val completeId = "c_${cUid}_$completeTimeStamp"

                    completeData.completeId = completeId
                    completeData.completeCategory = category
                    completeData.completeTimestamp = completeTimeStamp.toLong()
                    completeData.isComplete = true

                    firestore?.collection(Utils.userInfo)?.document(uid)
                            ?.collection(Utils.langInfo)?.document(sData.lang)
                            ?.collection(Utils.completeInfo)?.document(completeId)
                            ?.set(completeData)

                }
                setNegativeButton("????????? ?????? ?????? ") { dialogInterface, i ->
                    val sHashMap = HashMap<String, Any?>()

                    var indicator = 0
                    if(category == "reading") {
                        indicator = getIndicator(mData.idxReading, fData.idxReading)
                    }
                    else if(category == "listening"){
                        indicator = getIndicator(mData.idxListening, fData.idxListening)
                    }
                    else if(category == "speaking"){
                        indicator = getIndicator(mData.idxSpeaking, fData.idxSpeaking)
                    }
                    else if(category == "writing"){
                        indicator = getIndicator(mData.idxWriting, fData.idxWriting)
                    }
                    else if(category == "word"){
                        indicator = getIndicator(mData.idxWord, fData.idxWord)
                    }

                    sHashMap[idx] = indicator

                    firestore?.collection(Utils.userInfo)?.document(uid)
                            ?.collection(Utils.langInfo)?.document(sData.lang)
                            ?.collection(Utils.studyInfo)?.document(sData.studyId)
                            ?.update(sHashMap)


                    // ????????? ???????????????.
                    val completeData = sData

                    var cUid = uid.substring(0, 3)
                    val completeTimeStamp = SimpleDateFormat("yyMMddHHmmss").format(Date())
                    val completeId = "c_${cUid}_$completeTimeStamp"

                    completeData.completeId = completeId
                    completeData.completeCategory = category
                    completeData.completeTimestamp = completeTimeStamp.toLong()

                    firestore?.collection(Utils.userInfo)?.document(uid)
                            ?.collection(Utils.langInfo)?.document(sData.lang)
                            ?.collection(Utils.completeInfo)?.document(completeId)
                            ?.set(completeData)

                }
                show()
            }

        }
        else if(isSkip){
            val builder =
                    androidx.appcompat.app.AlertDialog.Builder(this)
            with(builder) {
                setTitle("?????????????????????? ????????? ?????? ???????????????.")
                setPositiveButton("???") { dialogInterface, i ->

                    val sHashMap = HashMap<String, Any?>()

                    var indicator = 0
                    if(category == "reading") {
                        indicator = getIndicator(mData.idxReading, fData.idxReading)
                    }
                    else if(category == "listening"){
                        indicator = getIndicator(mData.idxListening, fData.idxListening)
                    }
                    else if(category == "speaking"){
                        indicator = getIndicator(mData.idxSpeaking, fData.idxSpeaking)
                    }
                    else if(category == "writing"){
                        indicator = getIndicator(mData.idxWriting, fData.idxWriting)
                    }
                    else if(category == "word"){
                        indicator = getIndicator(mData.idxWord, fData.idxWord)
                    }
                    sHashMap[idx] = indicator

                    firestore?.collection(Utils.userInfo)?.document(uid)
                            ?.collection(Utils.langInfo)?.document(sData.lang)
                            ?.collection(Utils.studyInfo)?.document(sData.studyId)
                            ?.update(sHashMap)

                }
                setNegativeButton("?????????") { dialogInterface, i -> }
                show()
            }
        }
        else{

            val sHashMap = HashMap<String, Any?>()

            var indicator = 0
            if(category == "reading") {
                indicator = getIndicator(mData.idxReading, fData.idxReading)
            }
            else if(category == "listening"){
                indicator = getIndicator(mData.idxListening, fData.idxListening)
            }
            else if(category == "speaking"){
                indicator = getIndicator(mData.idxSpeaking, fData.idxSpeaking)
            }
            else if(category == "writing"){
                indicator = getIndicator(mData.idxWriting, fData.idxWriting)
            }
            else if(category == "word"){
                indicator = getIndicator(mData.idxWord, fData.idxWord)
            }
            sHashMap[idx] = indicator

            firestore?.collection(Utils.userInfo)?.document(uid)
                    ?.collection(Utils.langInfo)?.document(sData.lang)
                    ?.collection(Utils.studyInfo)?.document(sData.studyId)
                    ?.update(sHashMap)


            // ???????????? ?????? ??????, ???????????? ????????? ?????? ????????? ???
            val completeData = sData

            var cUid = uid.substring(0, 3)
            val completeTimeStamp = SimpleDateFormat("yyMMddHHmmss").format(Date())
            val completeId = "c_${cUid}_$completeTimeStamp"

            completeData.completeId = completeId
            completeData.completeCategory = category
            completeData.completeTimestamp = completeTimeStamp.toLong()

            firestore?.collection(Utils.userInfo)?.document(uid)
                ?.collection(Utils.langInfo)?.document(sData.lang)
                ?.collection(Utils.completeInfo)?.document(completeId)
                ?.set(completeData)
        }

        isNotChecking = true // ?????????
        isNotSuccess = true // ?????????

    }


    private fun getIndicator(value1: Int, value2: Int): Int {
        // 5??? 10??????, ?????? ????????? 5~15?????? ??????... 345?????? ????????? ????????? ????????? ??? ??????????????? +3?????? ???????????? ???.
        return (value1..value2).random() + (0..(value2 - value1)).random() + 3
    }

    private val listener = object : RecognitionListener {

        override fun onRmsChanged(rmsdB: Float) {}
        override fun onResults(results: Bundle) {
            val key = SpeechRecognizer.RESULTS_RECOGNITION
            val mResult = results.getStringArrayList(key) //??? ????????????
            val rs = arrayOfNulls<String>(mResult!!.size) // rs?????? string array??? ??????(size??? ????????? ??????)
            mResult.toArray(rs)
            //resultText = rs[0] // ????????? ?????? //

            var resultNum : Int
            if(rs.size >= 3){ resultNum = 3 }
            else{ resultNum = rs.size } // ???????????? 3??? ????????? ???,

            if(resultNum > 0) {
                for (i in 0 until resultNum) {
                    rs[i].let { resultTextList.add(it!!) }
                }
            }

            /*
            try {
                sy_finalTouch.text = "${rs[0]}, ${rs[1]}, ${rs[2]}"
            }
            catch(e: Exception){}

             */

        }

        override fun onReadyForSpeech(params: Bundle) {}
        override fun onPartialResults(partialResults: Bundle) {}
        override fun onEvent(eventType: Int, params: Bundle) {}

        override fun onError(error: Int) {
            val message: String

            when (error) {
                SpeechRecognizer.ERROR_AUDIO -> message = "????????? ???????????????."
                SpeechRecognizer.ERROR_CLIENT -> message = "??????????????? ???????????????."
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> message = "????????? ???????????? ????????????."
                SpeechRecognizer.ERROR_NETWORK -> message = "???????????? ???????????????."
                SpeechRecognizer.ERROR_NO_MATCH -> message = "?????? ????????? ???????????????."
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> message = "?????? ?????? ?????? ????????????."
                SpeechRecognizer.ERROR_SERVER -> message = "?????? ????????? ??????????????????."
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> message = "????????? ????????? ?????????????????????."
                else -> message = "??? ??? ?????? ???????????????."
            }
            try {
                errorCheck += 1

                mHandler!!.removeMessages(0)
                mRecognizer?.destroy()
                study_black_lo.visibility = View.GONE

                if (errorCheck % 3 == 0) {
                    Toast.makeText(
                            applicationContext,
                            "????????? ?????? ??????, ?????? ????????? ?????? ??????, ?????? ?????? ?????? ???????????? ?????? ????????? ????????? ????????? ??? ????????????.",
                            Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(applicationContext, "${message} ?????? ??????????????????!", Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                mHandler!!.removeMessages(0)
                mRecognizer?.destroy()
            } // main Fragment ????????? ???????????? ????????? Toast.makeText??? ????????? ??? ????????? ????????? ???
        }

        override fun onBufferReceived(buffer: ByteArray) {}
        override fun onBeginningOfSpeech() {}
        override fun onEndOfSpeech() {}
    }


    private fun numbersToWords(body: String): String {
        try {
            val regex = Regex(".*\\d+.*")

            //val body0 = bbb.text.toString()
            //val body = body0.replace(",", "")


            var body2 = ""
            for (i in 0 until body.length) {
                val ch = body[i].toString()

                if (regex.containsMatchIn(ch) || ch == " ") {
                    body2 += ch
                }
            }

            val kkk = body2.split(" ")
            val uuu = ArrayList<String>()
            val uuu2 = ArrayList<String>()

            var body3 = ""
            for (i in 0 until kkk.size) {
                if (regex.containsMatchIn(kkk[i])) {
                    uuu.add(kkk[i])

                    val num = kkk[i].toLong()
                    uuu2.add(EnglishNumberToWords.convert(num))
                    body3 += EnglishNumberToWords.convert(num)
                }

            }

            var newBody = body
            for (i in 0 until uuu.size) {
                newBody = newBody.replace(uuu[i], uuu2[i])
            }

            return newBody
        } catch (e: Exception) {
            return body
        }
    }


    fun tts(item: StudyDTO){

        var speaker: String
        speaker = when (item.lang) {
            "en" -> "Joanna"
            "zh" -> "Zhiyu"
            "ja" -> "Mizuki"
            "ko" -> "Seoyeon"
            "ca" -> "Penelope"
            "ar" -> "Zeina"
            else -> "Joanna"
        }

        lThread = Thread(Runnable {
            try {
                var synthesizeSpeechPresignRequest =
                        SynthesizeSpeechPresignRequest()
                                .withText(item.sentenceList[item.sentenceNum - 1]).withVoiceId(speaker)
                                .withOutputFormat(
                                        OutputFormat.Mp3
                                )

                var presignedSynthesizeSpeechUrl =
                        client?.getPresignedSynthesizeSpeechUrl(
                                synthesizeSpeechPresignRequest
                        )


                this@StudyActivity.runOnUiThread(Runnable {

                    study_tts_tv.setOnClickListener {
                        //Utils.bubbleAnim(this@StudyActivity, hint_tts_iv, 0.1, 10.0)

                        val mediaPlayer = MediaPlayer()
                        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)
                        mediaPlayer.setVolume(100f, 100f)


                        try {
                            mediaPlayer.setDataSource(presignedSynthesizeSpeechUrl.toString())
                        } catch (e: IOException) {
                            //Toast.makeText(applicationContext, "TTS??? ?????? ???????????? ???????????????.", Toast.LENGTH_LONG).show()
                        }

                        mediaPlayer.prepareAsync()
                        mediaPlayer.setOnPreparedListener { mp -> mp.start() }
                        mediaPlayer.setOnCompletionListener { mp -> mp.release() }
                    }

                })
            }
            catch(e:Exception){

            }

        })

        lThread?.start() // TTS ??????
        try { lThread?.join() }
        catch (e: Exception){}

    }


    fun audio(item: StudyDTO){
        if (item.audioUrl == "") {  // audioUrl??? ?????????
            study_audio_tv.isClickable = false
            study_audio_tv.setBackgroundResource(R.drawable.border_rounded_gray)
            study_audio_tv.text = "?????? ?????? ??????"
        }
        else{
            study_audio_tv.isClickable = true
            study_audio_tv.setBackgroundResource(R.drawable.border_rounded_gray)
            study_audio_tv.text = "?????? ?????? ??????"

            player = MediaPlayer().apply {
                try {
                    setDataSource(item.audioUrl)
                    prepare()
                } catch (e: IOException) {
                }
            }

            player?.setOnCompletionListener(MediaPlayer.OnCompletionListener {
                study_audio_tv.setBackgroundResource(R.drawable.border_rounded_gray)
                playOn = false
            })

            study_audio_tv.setOnClickListener {
                if (!playOn) {
                    study_audio_tv.setBackgroundResource(R.drawable.border_rounded_green)
                    playOn = true
                    player?.start()
                } else {
                    study_audio_tv.setBackgroundResource(R.drawable.border_rounded_gray)
                    playOn = false

                    player?.pause()
                    player?.seekTo(0)

                }
            }

        }
    }

    override fun onBackPressed() {
        lThread?.interrupt()
        super.onBackPressed()
    }

    // Timer ??????
    private fun pause() {
        //fab_start.setImageResource(R.drawable.ic_play)
        timerTask?.cancel()
    }


}