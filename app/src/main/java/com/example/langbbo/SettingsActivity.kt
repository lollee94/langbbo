package com.example.langbbo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.langbbo.modelDTO.LangDTO
import com.example.langbbo.modelDTO.UserDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.android.synthetic.main.fragment_calendar.view.*

class SettingsActivity : AppCompatActivity() {

    var auth : FirebaseAuth? = null
    var firestore: FirebaseFirestore? = null
    var uid : String = ""
    var lang : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        uid = auth?.currentUser?.uid!!

        firestore?.collection(Utils.userInfo)?.document(uid)?.addSnapshotListener {
                documentSnapshot, firebaseFirestoreException ->

            if (documentSnapshot != null) {
                var uItem = documentSnapshot.toObject(UserDTO::class.java)!!
                lang = uItem.lang

                firestore?.collection(Utils.userInfo)?.document(auth?.currentUser?.uid!!)
                    ?.collection(Utils.langInfo)?.document(lang)
                    ?.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->

                        if (documentSnapshot != null) {
                            var lItem = documentSnapshot.toObject(LangDTO::class.java)!!
                            val registerNum = lItem.registerNum
                            val studyNum = lItem.studyNum

                            settings_register_np.minValue = 0
                            settings_register_np.maxValue = 10

                            settings_study_np.minValue = 3
                            settings_study_np.maxValue = 10

                            settings_register_np.value = registerNum
                            settings_study_np.value = studyNum
                        }

                        settings_save_lo.setOnClickListener {

                            val lData = HashMap<String, Any?>()
                            lData["registerNum"] = settings_register_np.value
                            lData["studyNum"] = settings_study_np.value

                            firestore?.collection(Utils.userInfo)?.document(auth?.currentUser?.uid!!)
                                ?.collection(Utils.langInfo)?.document(lang)?.update(lData)?.addOnCompleteListener {

                                    Toast.makeText(this, "저장되었습니다.", Toast.LENGTH_LONG).show()
                                    finish()
                                }

                        }
                    }
            }
        }


    }
}