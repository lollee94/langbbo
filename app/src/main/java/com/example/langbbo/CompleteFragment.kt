package com.example.langbbo


import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.langbbo.modelDTO.StudyDTO
import com.example.langbbo.modelDTO.UserDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.collection.LLRBNode
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.fragment_complete.view.*
import kotlinx.android.synthetic.main.item_complete.view.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class CompleteFragment: Fragment() {

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

        fView = inflater.inflate(R.layout.fragment_complete, container, false)



        val todayTimestamp = SimpleDateFormat("yyMMdd").format(Date())

        firestore?.collection(Utils.userInfo)?.document(uid)?.addSnapshotListener {
            documentSnapshot, firebaseFirestoreException ->

            if (documentSnapshot != null) {
                var uItem = documentSnapshot.toObject(UserDTO::class.java)!!
                lang = uItem.lang

                firestore?.collection(Utils.userInfo)?.document(uid)
                        ?.collection(Utils.langInfo)?.document(lang)
                        ?.collection(Utils.studyInfo)?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->

                            if(querySnapshot != null){

                                val levelA = querySnapshot.size() / 20 + 1
                                fView!!.complete_level_a_tv.text = "LV ${levelA}."
                                fView!!.complete_quest_a_tv.text = "자료를 ${20 * levelA}개 등록해보세요."


                                fView!!.complete_a_pb.getProgressDrawable().setColorFilter(Color.parseColor("#0d47a1"), PorterDuff.Mode.SRC_IN)
                                fView!!.complete_a_pb.max = 20 * levelA
                                fView!!.complete_a_pb.progress = querySnapshot.size()
                                fView!!.complete_a_tv.text = "${querySnapshot.size()}/${20 * levelA}"

                            }

                        }

                firestore?.collection(Utils.userInfo)?.document(uid)
                    ?.collection(Utils.langInfo)?.document(lang)
                    ?.collection(Utils.completeInfo)?.addSnapshotListener {
                            querySnapshot, firebaseFirestoreException ->

                        if(querySnapshot != null) {
                            fView!!.complete_c_tv.text = "${querySnapshot.size()}개/20"

                            var allLearned = 0
                            var allCompleted = 0
                            var todayLearned = 0
                            var todayCompleted = 0

                            for (snapshot in querySnapshot) {
                                var item = snapshot.toObject(StudyDTO::class.java)!!

                                allLearned += 1

                                if(item.isComplete){
                                    allCompleted += 1
                                }

                            }


                            val levelB = allLearned / 10 + 1
                            fView!!.complete_level_b_tv.text = "LV ${levelB}."
                            fView!!.complete_quest_b_tv.text = "학습을 ${10 * levelB}개 진행해보세요."

                            fView!!.complete_b_pb.getProgressDrawable().setColorFilter(Color.parseColor("#0d47a1"), PorterDuff.Mode.SRC_IN)
                            fView!!.complete_b_pb.max = 10 * levelB
                            fView!!.complete_b_pb.progress = allLearned
                            fView!!.complete_b_tv.text = "${allLearned}/${10 * levelB}"


                            val levelC = allCompleted / 5 + 1
                            fView!!.complete_level_c_tv.text = "LV ${levelC}."
                            fView!!.complete_quest_c_tv.text = "완료 표현을 ${5 * levelC}개 만들어보세요."

                            fView!!.complete_c_pb.getProgressDrawable().setColorFilter(Color.parseColor("#0d47a1"), PorterDuff.Mode.SRC_IN)
                            fView!!.complete_c_pb.max = 5 * levelC
                            fView!!.complete_c_pb.progress = allCompleted
                            fView!!.complete_c_tv.text = "${allCompleted}/${5 * levelC}"


                        }
                    }


                    //fView!!.complete_rv.adapter = CompleteRecyclerviewAdapter()
                //fView!!.complete_rv.layoutManager = LinearLayoutManager(context)
            }
        }

        return fView

    }


    /*
    inner class CompleteRecyclerviewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        val studyDTOs: ArrayList<StudyDTO>

        init {
            studyDTOs = ArrayList()

            firestore?.collection(Utils.userInfo)?.document(uid)
                    ?.collection(Utils.langInfo)?.document(lang)
                    ?.collection(Utils.completeInfo)
                    ?.orderBy("completeTimestamp", Query.Direction.DESCENDING)?.addSnapshotListener {
                        querySnapshot, firebaseFirestoreException ->

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

            viewHolder.complete_name_tv.text = studyDTOs[position].sentenceKorean
            viewHolder.complete_category_tv.text = studyDTOs[position].completeCategory

        } // OnBindView Finish

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder {

            var view = LayoutInflater.from(parent?.context).inflate(R.layout.item_complete, parent, false)
            return CustomViewHolder(view)
        }

        private inner class CustomViewHolder(view: View?) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view!!)
    }
     */


    /*
    fun convertTimestampToDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        val date = sdf.format(timestamp)

        return date
    }

     */


}
