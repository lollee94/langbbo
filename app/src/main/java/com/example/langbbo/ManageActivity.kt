package com.example.langbbo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.langbbo.modelDTO.StudyDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_manage.*
import kotlinx.android.synthetic.main.activity_study.*
import kotlinx.android.synthetic.main.item_manage.view.*

class ManageActivity : AppCompatActivity() {

    var firestore : FirebaseFirestore? = null
    var auth : FirebaseAuth? = null
    var uid : String = ""
    var storage: FirebaseStorage? = null

    var lang: String = ""
    var category: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        uid = auth?.currentUser?.uid!!
        storage = FirebaseStorage.getInstance()

        val extras = intent.extras
        lang = extras?.getString("lang")!!
        category = extras?.getString("category")

        manage_back.setOnClickListener { finish() }
        manage_rv.adapter = StudyManageRecyclerviewAdapter()
        manage_rv.layoutManager = LinearLayoutManager(this)

    }

    inner class StudyManageRecyclerviewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        val studyDTOs: ArrayList<StudyDTO>

        init {
            studyDTOs = ArrayList()

            firestore?.collection(Utils.userInfo)?.document(uid)
                    ?.collection(Utils.langInfo)?.document(lang)
                    ?.collection(Utils.studyInfo)
                    ?.orderBy("timestamp", Query.Direction.DESCENDING)
                    ?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->

                        studyDTOs.clear() // ??????
                        if (querySnapshot != null) {
                            for (snapshot in querySnapshot) {
                                var item = snapshot.toObject(StudyDTO::class.java)!!

                                if(category != null) {
                                    if (item.catData[category!!] == true) {
                                        studyDTOs.add(item)
                                    }
                                } else{
                                    studyDTOs.add(item)
                                }
                            }
                        }
                        notifyDataSetChanged()
                    }
        }


        override fun getItemCount(): Int {
            return studyDTOs.size
        }

        override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
            val viewHolder = (holder as CustomViewHolder).itemView

            if(category == null){
                viewHolder.manage_revise_tv.visibility = View.VISIBLE
                viewHolder.manage_delete_tv.visibility = View.VISIBLE
                viewHolder.manage_answer_tv.visibility = View.GONE

                viewHolder.manage_name_tv.text = studyDTOs[position].sentenceKorean
            }
            else{
                viewHolder.manage_revise_tv.visibility = View.GONE
                viewHolder.manage_delete_tv.visibility = View.GONE
                viewHolder.manage_answer_tv.visibility = View.VISIBLE

                if(category == "word" || category == "reading") {
                    viewHolder.manage_name_tv.text = studyDTOs[position].sentenceList[studyDTOs[position].sentenceNum - 1]

                    var isAnswer = false
                    viewHolder.manage_answer_tv.setOnClickListener {
                        if(isAnswer){
                            viewHolder.manage_name_tv.text = studyDTOs[position].sentenceList[studyDTOs[position].sentenceNum - 1]
                        }
                        else {
                            viewHolder.manage_name_tv.text = studyDTOs[position].sentenceKorean
                        }
                        isAnswer = !isAnswer
                    }
                }
                else{
                    viewHolder.manage_name_tv.text = studyDTOs[position].sentenceKorean

                    var isAnswer = false
                    viewHolder.manage_answer_tv.setOnClickListener {
                        if(isAnswer){
                            viewHolder.manage_name_tv.text = studyDTOs[position].sentenceKorean
                        }
                        else {
                            viewHolder.manage_name_tv.text = studyDTOs[position].sentenceList[studyDTOs[position].sentenceNum - 1]
                        }
                        isAnswer = !isAnswer
                    }

                }


                viewHolder.manage_answer_tv.setOnLongClickListener {

                    val intent = Intent(applicationContext, HintActivity::class.java)
                    val args = Bundle()

                    args.putString("studyId", studyDTOs[position].studyId)
                    args.putString("lang", studyDTOs[position].lang)
                    args.putString("category", category)
                    intent.putExtras(args)
                    startActivity(intent)

                    true
                }
            }


            viewHolder.manage_revise_tv.setOnClickListener {
                var intent = Intent(applicationContext, FormActivity::class.java)
                val args = Bundle()

                args.putString("revise", "revise")
                args.putString("block", "block")
                args.putString("lang", studyDTOs[position].lang)
                args.putString("studyId", studyDTOs[position].studyId)
                args.putStringArrayList("sentenceList", studyDTOs[position].sentenceList)
                args.putInt("sentenceNum", studyDTOs[position].sentenceNum)
                args.putString("sentenceKorean", studyDTOs[position].sentenceKorean)
                args.putStringArrayList("coreExpression", studyDTOs[position].coreExpression)
                args.putString("situationExplain", studyDTOs[position].situationExplain)
                args.putStringArrayList("sentenceExplainList", studyDTOs[position].sentenceExplainList) // ????????? ??????
                args.putString("makeSituation", studyDTOs[position].makeSituation)
                args.putSerializable("catData", studyDTOs[position].catData)

                intent.putExtras(args)
                startActivity(intent)
            }

            viewHolder.manage_delete_tv.setOnClickListener {


                // ???????????? ?????? audio??? image??? ???????????? ?????????.

                val builder = androidx.appcompat.app.AlertDialog.Builder(this@ManageActivity)
                with(builder) {
                    setTitle("?????????????????????????")
                    setPositiveButton("??????") { dialogInterface, i ->

                        val imageFile = studyDTOs[position].imageFile
                        val audioFile = studyDTOs[position].audioFile

                        firestore?.collection(Utils.userInfo)?.document(uid)
                                ?.collection(Utils.langInfo)?.document(lang)
                                ?.collection(Utils.studyInfo)?.document(studyDTOs[position].studyId)?.delete()?.addOnSuccessListener {

                                    // ?????? ?????? ??? ???????????? ??????, position??? ????????? index ????????? ???. ????????? position??? ?????? ????????????????

                                    firestore?.collection(Utils.userInfo)?.document(uid)
                                            ?.collection(Utils.langInfo)?.document(lang)?.collection(Utils.studyInfo)?.get()?.addOnSuccessListener {
                                                querySnapshot ->

                                                var isThereSameImageFile = false
                                                var isThereSameAudioFile = false
                                                if(querySnapshot != null){
                                                    for(snapshot in querySnapshot){
                                                        val item = snapshot.toObject(StudyDTO::class.java)!!

                                                        if(item.imageFile == imageFile){
                                                            isThereSameImageFile = true
                                                        }

                                                        if(item.audioFile == audioFile){
                                                            isThereSameAudioFile = true
                                                        }


                                                    }
                                                }


                                                if (imageFile != "" && !isThereSameImageFile) {

                                                    val imageStorageRef =
                                                            storage?.reference?.child("users")?.child(uid)
                                                                    ?.child("image")
                                                                    ?.child(imageFile)
                                                    imageStorageRef?.delete()
                                                }

                                                //audioFile??? ?????????, ????????? ????????? ?????? ?????? ??????.
                                                if (audioFile != "" && !isThereSameAudioFile) {
                                                    val audioStorageRef =
                                                            storage?.reference?.child("users")?.child(uid)
                                                                    ?.child("audio")
                                                                    ?.child(audioFile)
                                                    audioStorageRef?.delete()
                                                }

                                                Toast.makeText(
                                                        this@ManageActivity,
                                                        "?????????????????????.",
                                                        Toast.LENGTH_LONG
                                                ).show()
                                            } // firestore





                            }
                    }
                    setNegativeButton("??????") { dialogInterface, i -> }
                    show()
                }

            }

        } // OnBindView Finish

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder {

            var view = LayoutInflater.from(parent?.context).inflate(R.layout.item_manage, parent, false)
            return CustomViewHolder(view)
        }

        private inner class CustomViewHolder(view: View?) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view!!)

    }
}
