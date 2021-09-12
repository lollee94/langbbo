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
import com.example.langbbo.modelDTO.UserDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_user.view.*
import kotlinx.android.synthetic.main.item_language.view.*

class UserFragment: Fragment() {

    var auth : FirebaseAuth? = null
    var firestore: FirebaseFirestore? = null
    var uid : String = ""

    var fView : View? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        uid = auth?.currentUser?.uid!!

        fView = inflater.inflate(R.layout.fragment_user, container, false)


        /*
        fView!!.user_languageSelect_lo.setOnClickListener { languageDialog() }

        firestore?.collection(Utils.userInfo)?.document(uid)?.addSnapshotListener {
            documentSnapshot, firebaseFirestoreException ->

            if(documentSnapshot != null){

                val item = documentSnapshot.toObject(UserDTO::class.java)!!
                fView!!.user_languageSelect_tv.text = "언어 선택: ${Utils.langToKorean(item.lang)}"

                /*
                fView!!.user_group_lo.setOnClickListener {
                    val intent = Intent(context, GroupManageActivity::class.java)
                    val args = Bundle()
                    args.putString("lang", item.lang)
                    intent.putExtras(args)
                    startActivity(intent)
                }
                */

            }
        }
         */

        fView!!.user_practice_lo.setOnClickListener {
            val intent = Intent(context, PracticeActivity::class.java)
            startActivity(intent)
        }


        fView!!.user_alarm_lo.setOnClickListener {
            val intent = Intent(context, AlarmActivity::class.java)
            startActivity(intent)
        }

        fView!!.user_settings_lo.setOnClickListener {
            val intent = Intent(context, SettingsActivity::class.java)
            startActivity(intent)
        }


        fView!!.user_signOut_lo.setOnClickListener {

            if(auth != null) {
                val builder = AlertDialog.Builder(context)
                with(builder) {
                    setTitle("정말 로그아웃하시겠습니까?")
                    setPositiveButton("확인") { dialogInterface, i ->
                        auth!!.signOut()

                        val intent = Intent(context, LoginActivity::class.java)
                        startActivity(intent)
                        activity?.finish()
                    }
                    setNegativeButton("취소") { dialogInterface, i -> }
                    show()
                }
            }
        }




        return fView
    }

}
