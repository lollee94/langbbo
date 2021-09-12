package com.example.langbbo

import com.example.langbbo.modelDTO.StudyDTO
import kotlinx.android.synthetic.main.activity_hint.*
import android.graphics.Color
import android.graphics.Typeface
import android.media.AudioManager
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.polly.AmazonPollyPresigningClient
import com.amazonaws.services.polly.model.OutputFormat
import com.amazonaws.services.polly.model.SynthesizeSpeechPresignRequest
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.IOException

class HintActivity : AppCompatActivity() {

    var auth: FirebaseAuth? = null
    var firestore: FirebaseFirestore? = null
    var extras: Bundle? = null

    var studyId: String = ""
    var lang: String = ""
    var category: String = ""

    var lThread: Thread? = null
    var client : AmazonPollyPresigningClient? = null

    private var player: MediaPlayer? = null
    var playOn: Boolean = false

    var uid : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hint)

        window.statusBarColor = Color.parseColor("#ffffff")

        val credentialsProvider = CognitoCachingCredentialsProvider(
            this,
            "ap-northeast-2:c903f1b4-7a90-42ff-a53a-df75f34036b2",
            Regions.AP_NORTHEAST_2
        )
        client = AmazonPollyPresigningClient(credentialsProvider)

        extras = intent.extras
        studyId = extras?.getString("studyId")!!
        lang = extras?.getString("lang")!!
        category = extras?.getString("category")!!

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        uid = auth?.currentUser?.uid!!


        firestore?.collection(Utils.userInfo)?.document(uid)
                ?.collection(Utils.langInfo)?.document(lang)
            ?.collection(Utils.studyInfo)?.document(studyId)?.get()
            ?.addOnSuccessListener { documentSnapshot ->

                if (documentSnapshot != null) {


                    val item = documentSnapshot.toObject(StudyDTO::class.java)!!

                    // 정답 문장 외국어
                    val content = SpannableString(item.sentenceList[item.sentenceNum - 1])
                    if (item.coreExpression.size >= 2) {
                        val word = item.coreExpression[0]
                        if (content.indexOf(word) > -1) {
                            val start = content.indexOf(word)
                            val end = start + word.length

                            content.setSpan(
                                    ForegroundColorSpan(Color.parseColor("#0d47a1")),
                                    start,
                                    end,
                                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                            )

                            content.setSpan(StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)


                        }
                    }
                    hint_sentence_tv.text = content

                    // 정답 문장 한국어
                    val content2 = SpannableString(item.sentenceKorean)
                    if (item.coreExpression.size >= 2) {
                        val wordReading = item.coreExpression[1]
                        if (content2.indexOf(wordReading) > -1) {
                            val startReading = content2.indexOf(wordReading)
                            val endReading = startReading + wordReading.length

                            content2.setSpan(
                                    ForegroundColorSpan(Color.parseColor("#0d47a1")),
                                    startReading,
                                    endReading,
                                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                            )

                            content2.setSpan(StyleSpan(Typeface.BOLD), startReading, endReading, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)


                        }
                    }
                    hint_sentence2_tv.text = content2



                    hint_sentenceExplain_lo.visibility = View.GONE
                    if(item.sentenceExplainList[0] != ""){
                        hint_sentenceExplain_lo.visibility = View.VISIBLE
                        hint_sentenceExplain1_lo.visibility = View.VISIBLE
                        hint_sentenceExplain1_tv.text = item.sentenceExplainList[0]
                    }
                    else{
                        hint_sentenceExplain1_lo.visibility = View.GONE
                    }

                    if(item.sentenceExplainList[1] != ""){
                        hint_sentenceExplain_lo.visibility = View.VISIBLE
                        hint_sentenceExplain2_lo.visibility = View.VISIBLE
                        hint_sentenceExplain2_tv.text = item.sentenceExplainList[1]
                    }
                    else{
                        hint_sentenceExplain2_lo.visibility = View.GONE
                    }

                    if(item.sentenceExplainList[2] != ""){
                        hint_sentenceExplain_lo.visibility = View.VISIBLE
                        hint_sentenceExplain3_lo.visibility = View.VISIBLE
                        hint_sentenceExplain3_tv.text = item.sentenceExplainList[2]
                    }
                    else{
                        hint_sentenceExplain3_lo.visibility = View.GONE
                    }

                    if(item.sentenceExplainList[3] != ""){
                        hint_sentenceExplain_lo.visibility = View.VISIBLE
                        hint_sentenceExplain4_lo.visibility = View.VISIBLE
                        hint_sentenceExplain4_tv.text = item.sentenceExplainList[3]
                    }
                    else{
                        hint_sentenceExplain4_lo.visibility = View.GONE
                    }





                    //visible_or_not(hint_situationExplain_lo, hint_situationExplain_tv, item.situationExplain)  //상황 설명
                    //visible_or_not(hint_makeSituation_lo, hint_makeSituation_tv, item.makeSituation)  //상황 설명

                    tts(item)
                    audio(item)

                    if(item.imageUrl != "") { Glide.with(this).load(item.imageUrl).into(study_image_iv) }
                    else{
                        study_image_lo.visibility = View.GONE
                        study_image_iv.visibility = View.GONE
                    }

                    hint_sv.visibility = View.VISIBLE



                }// documentSnapshot

            }
    }


    override fun onBackPressed() {
        lThread?.interrupt()
        super.onBackPressed()
    }

    fun visible_or_not(lo: LinearLayout, tv: TextView, str: String){
        if(str.trim() != ""){
            lo.visibility = View.VISIBLE
            tv.text = str
        }
        else{
            lo.visibility = View.GONE
        }
    }

    fun audio(item: StudyDTO){
        if (item.audioUrl == "") {  // audioUrl이 없으면
            hint_audio_lo.visibility = View.GONE
        } else { // 있으면
            hint_audio_lo.visibility = View.VISIBLE

            player = MediaPlayer().apply {
                try {
                    setDataSource(item.audioUrl)
                    prepare()
                } catch (e: IOException) {
                }
            }

            player?.setOnCompletionListener(MediaPlayer.OnCompletionListener {
                hint_audioFile_lo.setBackgroundColor(Color.parseColor("#808080"))
                playOn = false
            })

            hint_audioDuration_tv.text = Utils.getDuration(player?.duration!!)

            hint_audioFile_lo.setOnClickListener {
                if (!playOn) {
                    hint_audioFile_lo.setBackgroundColor(Color.parseColor("#81c147"))
                    playOn = true
                    player?.start()
                } else {
                    hint_audioFile_lo.setBackgroundColor(Color.parseColor("#808080"))
                    playOn = false

                    player?.pause()
                    player?.seekTo(0)

                }
            }
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

                this@HintActivity.runOnUiThread(Runnable {

                    hint_tts_lo.setOnClickListener {
                        Utils.bubbleAnim(this@HintActivity, hint_tts_iv, 0.1, 10.0)

                        val mediaPlayer = MediaPlayer()
                        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)
                        mediaPlayer.setVolume(100f, 100f)


                        try {
                            mediaPlayer.setDataSource(presignedSynthesizeSpeechUrl.toString())
                        } catch (e: IOException) {
                            //Toast.makeText(applicationContext, "TTS가 아직 준비되지 않았습니다.", Toast.LENGTH_LONG).show()
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

        lThread?.start() // TTS 시작
        try { lThread?.join() }
        catch (e: Exception){}

    }
}
