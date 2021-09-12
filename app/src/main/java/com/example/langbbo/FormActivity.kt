package com.example.langbbo

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

import java.text.SimpleDateFormat
import java.util.*
import java.io.File
import kotlin.collections.ArrayList
import com.example.langbbo.modelDTO.StudyDTO
import kotlinx.android.synthetic.main.activity_form.*
import kotlinx.android.synthetic.main.fragment_form1.view.*
import kotlinx.android.synthetic.main.fragment_form2.view.*
import kotlinx.android.synthetic.main.item_dialog.view.*
import kotlin.collections.HashMap

class FormActivity : AppCompatActivity() {

    var auth: FirebaseAuth? = null
    var firestore : FirebaseFirestore? = null
    var storage: FirebaseStorage? = null

    var extras : Bundle? = null

    var uid : String = ""
    var audioUrlString: String? = null
    var imageUrlString: String? = null

    var audioUrl: String? = null
    var audioFile: String? = null
    var imageUrl: String? = null
    var imageFile: String? = null
    var block: String? = null

    var revise_studyId: String? = null

    var lang: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form)

        extras = intent?.extras // bundle임
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        uid = auth?.currentUser?.uid!!

        lang = extras?.getString("lang")!!

        audioUrl = extras?.getString("audioUrl")
        audioFile = extras?.getString("audioFile")
        imageUrl = extras?.getString("imageUrl")
        imageFile = extras?.getString("imageFile")

        block = extras?.getString("block")


        form_title_tv.text = "자료 추가 - ${Utils.langToKorean(lang)}"

        form_back.setOnClickListener{
            finishDialog()
        }

        form_upload_lo.setOnClickListener {
            contentUpload()
        }

        // connect tabadapter
        val fragmentAdapter = FormPagerAdapter(supportFragmentManager)
        form_viewpager.adapter = fragmentAdapter
        form_tab.setupWithViewPager(form_viewpager)
        form_tab.setSelectedTabIndicatorHeight((3 * resources.displayMetrics.density).toInt())
        form_tab.setSelectedTabIndicatorColor(Color.parseColor("#0d47a1"))
        form_tab.setTabTextColors(Color.parseColor("#000000"), Color.parseColor("#000000"))


    }



    override fun onBackPressed() {

        finishDialog()

        /*
        val builder = AlertDialog.Builder(this)
        with(builder) {
            setTitle("컨텐츠 만들기를 종료하시겠습니까?")
            setPositiveButton("확인") { dialogInterface, i ->
                super.onBackPressed()
            }
            setNegativeButton("취소") { dialogInterface, i -> }
            show()
        }

         */
    }



    inner class FormPagerAdapter(fm: androidx.fragment.app.FragmentManager): androidx.fragment.app.FragmentStatePagerAdapter(fm){

        override fun getItemPosition(`object`: Any): Int {
            return androidx.viewpager.widget.PagerAdapter.POSITION_NONE
        }

        override fun getItem(position: Int): androidx.fragment.app.Fragment {
            when (position) {
                0 -> {
                    var formFragment1= FormFragment1()

                    val args = Bundle()
                    args.putString("revise", extras?.getString("revise"))
                    args.putStringArrayList("sentenceList", extras?.getStringArrayList("sentenceList"))
                    extras?.getInt("sentenceNum")?.let { args.putInt("sentenceNum", it) }
                    args.putString("sentenceKorean", extras?.getString("sentenceKorean"))
                    args.putStringArrayList("coreExpression", extras?.getStringArrayList("coreExpression"))
                    args.putString("situationExplain", extras?.getString("situationExplain"))
                    args.putStringArrayList("sentenceExplainList", extras?.getStringArrayList("sentenceExplainList"))
                    args.putString("makeSentence", extras?.getString("makeSentence"))
                    args.putSerializable("catData", extras?.getSerializable("catData"))

                    formFragment1.arguments = args

                    return formFragment1

                    return formFragment1
                }
                else -> {
                    var formFragment2 = FormFragment2()

                    val args = Bundle()
                    args.putString("block", block)

                    formFragment2.arguments = args

                    return formFragment2
                }
            }
        }

        override fun getCount(): Int {
            return 2
        }

        override fun getPageTitle(position: Int): CharSequence {
            return when (position) {
                0 -> "1단계(필수 내용)"
                else -> "2단계(참고 자료)"
            }
        }
    }


    fun contentUpload(){

        //수정에서 온 데이터들
        revise_studyId = extras?.getString("studyId")

        // studyId가 없다면 컨텐츠를 만든다.
        if(revise_studyId == null) {

            try {

                form_viewpager.currentItem = 0

                val firstSentence = form_viewpager.form_sentence1_et.text.toString().trim()
                val sentenceNum = Integer.parseInt(form_viewpager.form_sentenceNum_et.text.toString().trim())
                val sentenceKorean = form_viewpager.form_sentenceKorean_et.text.toString().trim()

                val reading = form_viewpager.form_reading_et.text.toString().trim()
                val listening = form_viewpager.form_listening_et.text.toString().trim()
                val speaking = form_viewpager.form_speaking_et.text.toString().trim()
                val writing = form_viewpager.form_writing_et.text.toString().trim()
                val word = form_viewpager.form_word_et.text.toString().trim()

                val selectCategory = (reading != "" || listening != "" || speaking != "" || writing != "" || word != "")

                if(!selectCategory) {
                    Toast.makeText(this, "카테고리를 한 개 이상 선택해주세요.", Toast.LENGTH_LONG).show()
                }
                else {
                    if (firstSentence == "") {
                        Toast.makeText(this, "첫 번째 문장은 꼭 입력해주세요.", Toast.LENGTH_LONG).show()
                    } else {
                        if (sentenceNum == 0) {
                            Toast.makeText(this, "학습이 필요한 문장에 체크 표시를 해주세요.", Toast.LENGTH_LONG).show()
                        } else {
                            if (sentenceKorean == "") {
                                Toast.makeText(this, "정답 문장을 입력해주세요.", Toast.LENGTH_LONG).show()
                            } else {
                                //// ////
                                var sUid = uid.substring(0, 3)
                                val timeStamp = SimpleDateFormat("yyMMddHHmmss").format(Date())
                                val studyId = "s_${sUid}_$timeStamp"
                                val situationExplain = form_viewpager.form_situationExplain_et.text.toString().trim()


                                var studyDTO = StudyDTO()
                                studyDTO.studyId = studyId
                                studyDTO.uid = uid
                                studyDTO.timestamp = timeStamp.toLong()
                                //studyDTO.indicator = (0..10).random()

                                studyDTO.idxReading = (0..10).random()
                                studyDTO.idxListening = (0..10).random()
                                studyDTO.idxWriting = (0..10).random()
                                studyDTO.idxSpeaking = (0..10).random()

                                studyDTO.lang = lang

                                val catData = HashMap<String, Boolean>()
                                catData["reading"] = reading != ""
                                catData["listening"] = listening != ""
                                catData["speaking"] = speaking != ""
                                catData["writing"] = writing != ""
                                catData["word"] = word != ""

                                studyDTO.catData = catData

                                val sArray = ArrayList<String>()
                                sArray.add(form_viewpager.form_sentence1_et.text.toString().trim())
                                sArray.add(form_viewpager.form_sentence2_et.text.toString().trim())
                                sArray.add(form_viewpager.form_sentence3_et.text.toString().trim())
                                sArray.add(form_viewpager.form_sentence4_et.text.toString().trim())
                                studyDTO.sentenceList = sArray

                                val cArray = ArrayList<String>()
                                cArray.add(form_viewpager.form_core1_et.text.toString().trim())
                                cArray.add(form_viewpager.form_core2_et.text.toString().trim())
                                studyDTO.coreExpression = cArray

                                studyDTO.sentenceNum =
                                        Integer.parseInt(form_viewpager.form_sentenceNum_et.text.toString().trim())
                                studyDTO.sentenceKorean = form_viewpager.form_sentenceKorean_et.text.toString().trim()
                                studyDTO.situationExplain = situationExplain


                                val seArray = ArrayList<String>()
                                seArray.add(form_viewpager.form_sentenceExplain1_et.text.toString().trim())
                                seArray.add(form_viewpager.form_sentenceExplain2_et.text.toString().trim())
                                seArray.add(form_viewpager.form_sentenceExplain3_et.text.toString().trim())
                                seArray.add(form_viewpager.form_sentenceExplain4_et.text.toString().trim())
                                studyDTO.sentenceExplainList = seArray

                                studyDTO.makeSituation = form_viewpager.form_makeSituation_et.text.toString().trim()

                                if(block != null){

                                    studyDTO.imageUrl = imageUrl!!
                                    studyDTO.imageFile = imageFile!!
                                    studyDTO.audioUrl = audioUrl!!
                                    studyDTO.audioFile = audioFile!!

                                    firestore?.collection(Utils.userInfo)?.document(uid)
                                            ?.collection(Utils.langInfo)?.document(lang)
                                            ?.collection(Utils.studyInfo)
                                            ?.document(studyId)?.set(studyDTO)?.addOnSuccessListener {

                                                addContentDialog(studyDTO)

                                            }


                                }
                                else{

                                    form_viewpager.currentItem = 1
                                    audioUrlString = form_viewpager.form_audioUrl_tv.text.toString().trim()
                                    imageUrlString = form_viewpager.form_imageUrl_tv.text.toString().trim()

                                    if (audioUrlString == "" && imageUrlString == "") {
                                        // 둘 다 없는 경우
                                        //Toast.makeText(this, "여.", Toast.LENGTH_LONG).show()

                                        firestore?.collection(Utils.userInfo)?.document(uid)
                                                ?.collection(Utils.langInfo)?.document(lang)
                                                ?.collection(Utils.studyInfo)
                                                ?.document(studyId)?.set(studyDTO)?.addOnSuccessListener {


                                                    addContentDialog(studyDTO)

                                                }
                                    } else if (audioUrlString != "" && imageUrlString == "") {
                                        // 녹음만 있는 경우
                                        Toast.makeText(this, "잠시만 기다려주세요.", Toast.LENGTH_LONG).show()

                                        val audioUrl = Uri.fromFile(File(audioUrlString))

                                        //Toast.makeText(this, uri.toString(), Toast.LENGTH_LONG).show()

                                        val audioFileName = "${sUid}_$timeStamp.3gp"
                                        val storageRef =
                                                storage?.reference?.child("users")?.child(uid)?.child("audio")
                                                        ?.child(audioFileName)

                                        storageRef?.putFile(audioUrl)?.addOnSuccessListener { taskSnapshot ->

                                            storageRef?.downloadUrl?.addOnSuccessListener { Uri ->
                                                //Toast.makeText(this, "저장완료요.", Toast.LENGTH_LONG).show()

                                                studyDTO.audioUrl = Uri.toString()
                                                studyDTO.audioFile = audioFileName



                                                firestore?.collection(Utils.userInfo)?.document(uid)
                                                        ?.collection(Utils.langInfo)?.document(lang)
                                                        ?.collection(Utils.studyInfo)
                                                        ?.document(studyId)?.set(studyDTO)?.addOnSuccessListener {

                                                            addContentDialog(studyDTO)

                                                        }


                                            }
                                        }
                                    } else if (audioUrlString == "" && imageUrlString != "") {
                                        // 이미지만 있는 경우

                                        Toast.makeText(this, "잠시만 기다려주세요.", Toast.LENGTH_LONG).show()

                                        val imageUrl = Uri.parse(imageUrlString)

                                        //Toast.makeText(this, imageUrl.toString(), Toast.LENGTH_LONG).show()

                                        val imageFileName = "${sUid}_${timeStamp}.png"
                                        val storageRef =
                                                storage?.reference?.child("users")?.child(uid)?.child("image")
                                                        ?.child(imageFileName)

                                        storageRef?.putFile(imageUrl)?.addOnSuccessListener { taskSnapshot ->


                                            storageRef?.downloadUrl?.addOnSuccessListener { Uri ->
                                                //Toast.makeText(this, "저장완료요.", Toast.LENGTH_LONG).show()

                                                studyDTO.imageUrl = Uri.toString()
                                                studyDTO.imageFile = imageFileName


                                                firestore?.collection(Utils.userInfo)?.document(uid)
                                                        ?.collection(Utils.langInfo)?.document(lang)
                                                        ?.collection(Utils.studyInfo)
                                                        ?.document(studyId)?.set(studyDTO)?.addOnSuccessListener {

                                                            addContentDialog(studyDTO)

                                                        }


                                            }
                                        }

                                    } else {
                                        // 둘 다 있는 경우
                                        Toast.makeText(this, "잠시만 기다려주세요.", Toast.LENGTH_LONG).show()

                                        val audioUrl = Uri.fromFile(File(audioUrlString))

                                        //Toast.makeText(this, uri.toString(), Toast.LENGTH_LONG).show()

                                        val audioFileName = "${sUid}_$timeStamp.3gp"
                                        val audioStorageRef =
                                                storage?.reference?.child("users")?.child(uid)?.child("audio")
                                                        ?.child(audioFileName)

                                        audioStorageRef?.putFile(audioUrl)?.addOnSuccessListener { taskSnapshot ->

                                            audioStorageRef?.downloadUrl?.addOnSuccessListener { Uri ->
                                                //Toast.makeText(this, "저장완료요.", Toast.LENGTH_LONG).show()

                                                studyDTO.audioUrl = Uri.toString()
                                                studyDTO.audioFile = audioFileName

                                                val imageUrl = android.net.Uri.parse(imageUrlString)


                                                val imageFileName = "${sUid}_${timeStamp}.png"
                                                val imageStorageRef =
                                                        storage?.reference?.child("users")?.child(uid)?.child("image")
                                                                ?.child(imageFileName)

                                                imageStorageRef?.putFile(imageUrl)?.addOnSuccessListener { taskSnapshot ->


                                                    imageStorageRef?.downloadUrl?.addOnSuccessListener { Uri ->
                                                        //Toast.makeText(this, "저장완료요.", Toast.LENGTH_LONG).show()

                                                        studyDTO.imageUrl = Uri.toString()
                                                        studyDTO.imageFile = imageFileName


                                                        firestore?.collection(Utils.userInfo)?.document(uid)
                                                                ?.collection(Utils.langInfo)?.document(lang)
                                                                ?.collection(Utils.studyInfo)
                                                                ?.document(studyId)?.set(studyDTO)?.addOnSuccessListener {

                                                                    addContentDialog(studyDTO)

                                                                }


                                                    }
                                                }

                                            }
                                        }
                                    }
                                } // block else 끝
                            }
                        }
                    }
                } // selectCategory

            } catch (e: Exception) { }
        }

        // studyId가 이미 있는 경우라면, 수정하는 방향으로 간다.
        else {
            try {

                // 수정은 이미지 및 녹음 불가
                form_viewpager.currentItem = 0

                val firstSentence = form_viewpager.form_sentence1_et.text.toString().trim()
                val sentenceNum = Integer.parseInt(form_viewpager.form_sentenceNum_et.text.toString().trim())
                val sentenceKorean = form_viewpager.form_sentenceKorean_et.text.toString().trim()

                val reading = form_viewpager.form_reading_et.text.toString().trim()
                val listening = form_viewpager.form_listening_et.text.toString().trim()
                val speaking = form_viewpager.form_speaking_et.text.toString().trim()
                val writing = form_viewpager.form_writing_et.text.toString().trim()
                val word = form_viewpager.form_word_et.text.toString().trim()

                val selectCategory = (reading != "" || listening != "" || speaking != "" || writing != "" || word != "")

                if(!selectCategory) {
                    Toast.makeText(this, "카테고리를 한 개 이상 선택해주세요.", Toast.LENGTH_LONG).show()
                }
                else {
                    if (firstSentence == "") {
                        Toast.makeText(this, "첫 번째 문장은 꼭 입력해주세요.", Toast.LENGTH_LONG).show()
                    } else {
                        if (sentenceNum == 0) {
                            Toast.makeText(this, "학습이 필요한 문장에 체크 표시를 해주세요.", Toast.LENGTH_LONG).show()
                        } else {
                            if (sentenceKorean == "") {
                                Toast.makeText(this, "정답 문장을 입력해주세요.", Toast.LENGTH_LONG).show()
                            } else {

                                val situationExplain = form_viewpager.form_situationExplain_et.text.toString().trim()

                                var sData = HashMap<String, Any?>()

                                val catData = HashMap<String, Boolean>()
                                catData["reading"] = reading != ""
                                catData["listening"] = listening != ""
                                catData["speaking"] = speaking != ""
                                catData["writing"] = writing != ""
                                sData["catData"] = catData

                                val sArray = ArrayList<String>()
                                sArray.add(form_viewpager.form_sentence1_et.text.toString().trim())
                                sArray.add(form_viewpager.form_sentence2_et.text.toString().trim())
                                sArray.add(form_viewpager.form_sentence3_et.text.toString().trim())
                                sArray.add(form_viewpager.form_sentence4_et.text.toString().trim())
                                sData["sentenceList"] = sArray

                                val cArray = ArrayList<String>()
                                cArray.add(form_viewpager.form_core1_et.text.toString().trim())
                                cArray.add(form_viewpager.form_core2_et.text.toString().trim())
                                sData["coreExpression"] = cArray

                                sData["sentenceNum"] = Integer.parseInt(form_viewpager.form_sentenceNum_et.text.toString().trim())
                                sData["sentenceKorean"] = form_viewpager.form_sentenceKorean_et.text.toString().trim()

                                sData["situationExplain"] = situationExplain

                                val seArray = ArrayList<String>()
                                seArray.add(form_viewpager.form_sentenceExplain1_et.text.toString().trim())
                                seArray.add(form_viewpager.form_sentenceExplain2_et.text.toString().trim())
                                seArray.add(form_viewpager.form_sentenceExplain3_et.text.toString().trim())
                                seArray.add(form_viewpager.form_sentenceExplain4_et.text.toString().trim())
                                sData["sentenceExplainList"] = seArray

                                sData["makeSituation"] = form_viewpager.form_makeSituation_et.text.toString().trim()


                                firestore?.collection(Utils.userInfo)?.document(uid)
                                        ?.collection(Utils.langInfo)?.document(lang)
                                        ?.collection(Utils.studyInfo)
                                        ?.document(revise_studyId!!)?.update(sData)?.addOnSuccessListener {

                                            Toast.makeText(applicationContext, "수정 완료", Toast.LENGTH_LONG).show()
                                            finish()
                                        }
                            }
                        }
                    }
                }



            } catch (e: Exception) {
            }
        }
    }


    fun addContentDialog(sData: StudyDTO) {

        val builder = android.app.AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.item_dialog, null)

        builder.setView(dialogView)
        val alertDialog = builder.create()

        dialogView.dialog_finish_tv.text = "네"
        dialogView.dialog_cancel_tv.text = "아니오"
        dialogView.dialog_message_tv.text = "작성 완료! 동일한 내용으로 컨텐츠를 더 만드시겠어요?"
        dialogView.dialog_finish_lo.setOnClickListener{
            var intent = Intent(applicationContext, FormActivity::class.java)
            val args = Bundle()

            args.putString("block", "block")
            args.putString("lang", sData.lang)
            args.putStringArrayList("sentenceList", sData.sentenceList)
            args.putString("situationExplain", sData.situationExplain)
            args.putString("audioUrl", sData.audioUrl)
            args.putString("audioFile", sData.audioFile)
            args.putString("imageUrl", sData.imageUrl)
            args.putString("imageFile", sData.imageFile)

            intent.putExtras(args)
            startActivity(intent)

            alertDialog.dismiss()
            finish()
        }
        dialogView.dialog_cancel_lo.setOnClickListener {
            alertDialog.dismiss()
            finish()
        }
        alertDialog.show()
    }


    private fun finishDialog() {

        val builder = android.app.AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.item_dialog, null)

        builder.setView(dialogView)
        val alertDialog = builder.create()
        dialogView.dialog_finish_lo.setOnClickListener{
            alertDialog.dismiss()
            finish()
        }
        dialogView.dialog_cancel_lo.setOnClickListener { alertDialog.dismiss() }
        alertDialog.show()
    }


}
