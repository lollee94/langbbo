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
import kotlinx.android.synthetic.main.item_manage.view.*

class ManageActivity : AppCompatActivity() {

    var firestore : FirebaseFirestore? = null
    var auth : FirebaseAuth? = null
    var uid : String = ""
    var storage: FirebaseStorage? = null

    var lang: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        uid = auth?.currentUser?.uid!!
        storage = FirebaseStorage.getInstance()

        val extras = intent.extras
        lang = extras?.getString("lang")!!

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

                        studyDTOs.clear() // 필수
                        if (querySnapshot != null) {
                            for (snapshot in querySnapshot) {
                                var item = snapshot.toObject(StudyDTO::class.java)!!
                                studyDTOs.add(item)
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

            viewHolder.manage_name_tv.text = studyDTOs[position].sentenceKorean

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
                args.putStringArrayList("sentenceExplainList", studyDTOs[position].sentenceExplainList) // 들리는 소리
                args.putString("makeSituation", studyDTOs[position].makeSituation)
                args.putSerializable("catData", studyDTOs[position].catData)

                intent.putExtras(args)
                startActivity(intent)

            }


            viewHolder.manage_delete_tv.setOnClickListener {


                // 삭제하기 전에 audio랑 image도 삭제하고 가야함.

                val builder = androidx.appcompat.app.AlertDialog.Builder(this@ManageActivity)
                with(builder) {
                    setTitle("삭제하시겠습니까?")
                    setPositiveButton("삭제") { dialogInterface, i ->

                        val imageFile = studyDTOs[position].imageFile
                        val audioFile = studyDTOs[position].audioFile

                        firestore?.collection(Utils.userInfo)?.document(uid)
                                ?.collection(Utils.langInfo)?.document(lang)
                                ?.collection(Utils.studyInfo)?.document(studyDTOs[position].studyId)?.delete()?.addOnSuccessListener {

                                    // 모든 자료 다 삭제하고 나면, position이 없어서 index 오류가 남. 안에서 position을 쓰지 말아야할듯?

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

                                                //audioFile이 있는데, 똑같은 파일이 없는 경우 삭제.
                                                if (audioFile != "" && !isThereSameAudioFile) {
                                                    val audioStorageRef =
                                                            storage?.reference?.child("users")?.child(uid)
                                                                    ?.child("audio")
                                                                    ?.child(audioFile)
                                                    audioStorageRef?.delete()
                                                }

                                                Toast.makeText(
                                                        this@ManageActivity,
                                                        "삭제되었습니다.",
                                                        Toast.LENGTH_LONG
                                                ).show()
                                            } // firestore





                            }
                    }
                    setNegativeButton("취소") { dialogInterface, i -> }
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
