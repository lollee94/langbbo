package com.example.langbbo


import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.example.langbbo.modelDTO.StudyDTO
import com.example.langbbo.modelDTO.UserDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_storage.view.*
import kotlinx.android.synthetic.main.fragment_user.view.*
import kotlinx.android.synthetic.main.item_language.view.*

class StorageFragment: Fragment() {

    var auth : FirebaseAuth? = null
    var firestore: FirebaseFirestore? = null
    var uid : String = ""
    var lang : String = ""

    var fView : View? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        uid = auth?.currentUser?.uid!!

        fView = inflater.inflate(R.layout.fragment_storage, container, false)

        val lo = fView!!.storage_category_lo
        val childCount = lo.childCount

        /*
        for(i in 0 until childCount){
            val childLo = lo.getChildAt(i)
            childLo.setOnClickListener {

                val idValue = childLo.id
                val category = resources.getResourceEntryName(idValue)

                val intent = Intent(context, StudyActivity::class.java)
                val args = Bundle()
                args.putString("lang", lang)
                args.putString("category", category)

                intent.putExtras(args)
                startActivity(intent)
            }
        }
         */
        fView!!.word.setOnClickListener {
            val intent = Intent(context, StudyActivity::class.java)
            val args = Bundle()
            args.putString("lang", lang)
            args.putString("category", "word")

            intent.putExtras(args)
            startActivity(intent)
        }

        fView!!.reading.setOnClickListener {
            val intent = Intent(context, StudyActivity::class.java)
            val args = Bundle()
            args.putString("lang", lang)
            args.putString("category", "reading")

            intent.putExtras(args)
            startActivity(intent)
        }

        fView!!.listening.setOnClickListener {
            val intent = Intent(context, StudyActivity::class.java)
            val args = Bundle()
            args.putString("lang", lang)
            args.putString("category", "listening")

            intent.putExtras(args)
            startActivity(intent)
        }

        fView!!.speaking.setOnClickListener {
            val intent = Intent(context, StudyActivity::class.java)
            val args = Bundle()
            args.putString("lang", lang)
            args.putString("category", "speaking")

            intent.putExtras(args)
            startActivity(intent)
        }

        fView!!.writing.setOnClickListener {
            val intent = Intent(context, StudyActivity::class.java)
            val args = Bundle()
            args.putString("lang", lang)
            args.putString("category", "writing")

            intent.putExtras(args)
            startActivity(intent)
        }

        fView!!.word.setOnLongClickListener {
            val intent = Intent(context, ManageActivity::class.java)
            val args = Bundle()
            args.putString("lang", lang)
            args.putString("category", "word")

            intent.putExtras(args)
            startActivity(intent)

            true
        }

        fView!!.reading.setOnLongClickListener {
            val intent = Intent(context, ManageActivity::class.java)
            val args = Bundle()
            args.putString("lang", lang)
            args.putString("category", "reading")

            intent.putExtras(args)
            startActivity(intent)

            true
        }

        fView!!.listening.setOnLongClickListener {
            val intent = Intent(context, ManageActivity::class.java)
            val args = Bundle()
            args.putString("lang", lang)
            args.putString("category", "listening")

            intent.putExtras(args)
            startActivity(intent)

            true
        }

        fView!!.speaking.setOnLongClickListener {
            val intent = Intent(context, ManageActivity::class.java)
            val args = Bundle()
            args.putString("lang", lang)
            args.putString("category", "speaking")

            intent.putExtras(args)
            startActivity(intent)

            true
        }

        fView!!.writing.setOnLongClickListener {
            val intent = Intent(context, ManageActivity::class.java)
            val args = Bundle()
            args.putString("lang", lang)
            args.putString("category", "writing")

            intent.putExtras(args)
            startActivity(intent)

            true
        }


        fView!!.storage_add_lo.setOnClickListener {
            val intent = Intent(context, FormActivity::class.java)
            val args = Bundle()
            args.putString("lang", lang)

            intent.putExtras(args)
            startActivity(intent)
        }

        fView!!.storage_manage_lo.setOnClickListener {
            val intent = Intent(context, ManageActivity::class.java)
            val args = Bundle()
            args.putString("lang", lang)

            intent.putExtras(args)
            startActivity(intent)
        }

        firestore?.collection(Utils.userInfo)?.document(uid)?.get()?.addOnSuccessListener {
            documentSnapshot ->

            if (documentSnapshot != null) {
                var uItem = documentSnapshot.toObject(UserDTO::class.java)!!
                lang = uItem.lang

                fView!!.storage_lang_tv.text = "??????: ${Utils.langToKorean(lang)}"
                fView!!.storage_swap_iv.setOnClickListener { languageDialog() }
                fView!!.storage_add_tv.text = "${Utils.langToKorean(lang)} ?????? ??????"
                fView!!.storage_manage_tv.text = "${Utils.langToKorean(lang)} ?????? ??????"

                fView!!.storage_lang_lo.visibility = View.VISIBLE
                fView!!.storage_add_lo.visibility = View.VISIBLE
                fView!!.storage_manage_lo.visibility = View.VISIBLE

                firestore?.collection(Utils.userInfo)?.document(uid)
                        ?.collection(Utils.langInfo)?.document(lang)
                        ?.collection(Utils.studyInfo)?.get()?.addOnSuccessListener {
                         querySnapshot ->

                            var readingStoreNum = 0
                            var listeningStoreNum = 0
                            var speakingStoreNum = 0
                            var writingStoreNum = 0
                            var wordStoreNum = 0

                            if(querySnapshot != null){
                                for(snapshot in querySnapshot){

                                    var item = snapshot.toObject(StudyDTO::class.java)

                                    if(item.catData["reading"] == true){ readingStoreNum += 1 }
                                    if(item.catData["listening"] == true){ listeningStoreNum += 1 }
                                    if(item.catData["speaking"] == true){ speakingStoreNum += 1 }
                                    if(item.catData["writing"] == true){ writingStoreNum += 1 }
                                    if(item.catData["word"] == true){ wordStoreNum += 1 }

                                }
                            }

                            fView!!.reading_store_tv.text = "?????? ?????? $readingStoreNum"
                            fView!!.listening_store_tv.text = "?????? ?????? $listeningStoreNum"
                            fView!!.speaking_store_tv.text = "?????? ?????? $speakingStoreNum"
                            fView!!.writing_store_tv.text = "?????? ?????? $writingStoreNum"
                            fView!!.word_store_tv.text = "?????? ?????? $wordStoreNum"

                        }


                firestore?.collection(Utils.userInfo)?.document(uid)
                        ?.collection(Utils.langInfo)?.document(lang)
                        ?.collection(Utils.completeInfo)?.get()?.addOnSuccessListener {
                            querySnapshot ->

                            var readingStudyNum = 0
                            var listeningStudyNum = 0
                            var speakingStudyNum = 0
                            var writingStudyNum = 0
                            var wordStudyNum = 0

                            var readingCompleteNum = 0
                            var listeningCompleteNum = 0
                            var speakingCompleteNum = 0
                            var writingCompleteNum = 0
                            var wordCompleteNum = 0

                            if(querySnapshot != null){
                                for(snapshot in querySnapshot){

                                    var item = snapshot.toObject(StudyDTO::class.java)

                                    if (item.completeCategory == "reading") {
                                        readingStudyNum += 1
                                        if(item.isComplete) readingCompleteNum += 1
                                    }
                                    if (item.completeCategory == "listening") {
                                        listeningStudyNum += 1
                                        if(item.isComplete) listeningCompleteNum += 1
                                    }
                                    if (item.completeCategory == "speaking") {
                                        speakingStudyNum += 1
                                        if(item.isComplete) speakingCompleteNum += 1
                                    }
                                    if (item.completeCategory == "writing") {
                                        writingStudyNum += 1
                                        if(item.isComplete) writingCompleteNum += 1
                                    }
                                    if (item.completeCategory == "word") {
                                        wordStudyNum += 1
                                        if(item.isComplete) wordCompleteNum += 1
                                    }

                                }
                            }

                            fView!!.reading_complete_tv.text = "?????? ?????? $readingCompleteNum"
                            fView!!.listening_complete_tv.text = "?????? ?????? $listeningCompleteNum"
                            fView!!.speaking_complete_tv.text = "?????? ?????? $speakingCompleteNum"
                            fView!!.writing_complete_tv.text = "?????? ?????? $writingCompleteNum"
                            fView!!.word_complete_tv.text = "?????? ?????? $wordCompleteNum"

                            fView!!.reading_study_tv.text = "?????? ?????? $wordStudyNum"
                            fView!!.listening_study_tv.text = "?????? ?????? $listeningStudyNum"
                            fView!!.speaking_study_tv.text = "?????? ?????? $speakingStudyNum"
                            fView!!.writing_study_tv.text = "?????? ?????? $writingStudyNum"
                            fView!!.word_study_tv.text = "?????? ?????? $wordStudyNum"


                            (activity as MainActivity).controlSplash(false)
                        }

            }
        }


        return fView

    }


    private fun languageDialog() {

        val builder = AlertDialog.Builder(context)
        val dialogView = layoutInflater.inflate(R.layout.item_language, null)

        var langList = ArrayList<String>()
        langList.add("??????")
        langList.add("?????????")
        langList.add("?????????")
        langList.add("????????????")

        var firstAdapter = ArrayAdapter(context!!, R.layout.item_spinner, langList)
        firstAdapter.setDropDownViewResource(R.layout.item_spinner)

        dialogView.language_spinner.adapter = firstAdapter
        dialogView.language_spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
            override fun onItemSelected(parent: AdapterView<*>, sView: View?, position: Int, id: Long) {
                dialogView.language_tv.text = firstAdapter.getItem(position)
            }
        }

        firestore?.collection(Utils.userInfo)?.document(uid)?.get()?.addOnSuccessListener {

            documentSnapshot ->

            if(documentSnapshot != null){

                var item = documentSnapshot.toObject(UserDTO::class.java)!!

                var spinnerPosition = langList.indexOf(Utils.langToKorean(item.lang))
                if(spinnerPosition > -1){ dialogView.language_spinner.setSelection(spinnerPosition) }
                else{ dialogView.language_spinner.setSelection(0) }

                with(builder) {
                    setView(dialogView)
                    setPositiveButton("??????") { dialogInterface, i ->

                        val lang = Utils.koreanToLang(dialogView.language_tv.text.toString().trim())

                        val uData = HashMap<String, Any?>()
                        uData["lang"] = lang
                        firestore?.collection(Utils.userInfo)?.document(uid)?.update(uData)?.addOnCompleteListener {


                        }

                    }
                    setNegativeButton("??????") { dialogInterface, i -> }
                    show()
                }
            }
        }
    }
}
