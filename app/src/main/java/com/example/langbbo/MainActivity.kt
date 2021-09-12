package com.example.langbbo

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.example.langbbo.modelDTO.LangDTO
import com.example.langbbo.modelDTO.StudyDTO
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {

    var pressedTime : Int = 0
    var auth : FirebaseAuth? = null
    var firestore: FirebaseFirestore? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initFirebase() // 푸시 토큰 받아오기


        main_bn.setOnNavigationItemSelectedListener(this)
        main_bn.disableShiftMode()
        main_bn.selectedItemId = R.id.action_storage

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()


        val permission = ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
        )

        // If we don't have permissions, ask user for permissions
        if (permission != PackageManager.PERMISSION_GRANTED) {
            val PERMISSIONS = arrayOf(
                    android.Manifest.permission.RECORD_AUDIO
            )
            val REQUEST_EXTERNAL_STORAGE = 1

            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS,
                    REQUEST_EXTERNAL_STORAGE
            )
        }


        val storage_permission = ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        // If we don't have permissions, ask user for permissions
        if (storage_permission != PackageManager.PERMISSION_GRANTED) {
            val PERMISSIONS_STORAGE = arrayOf(
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            val REQUEST_EXTERNAL_STORAGE = 1

            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            )
        }



        /*
        val firebaseLang = firestore?.collection(Utils.userInfo)
                ?.document(auth?.currentUser?.uid!!)?.collection(Utils.langInfo)

        firebaseLang?.addSnapshotListener {

            querySnapshot, firebaseFirestoreException ->

            if (querySnapshot != null) {
                for (snapshot in querySnapshot) {
                    val item = snapshot.toObject(LangDTO::class.java)!!

                    val sData = HashMap<String,Any?>()
                    sData["registerNum"] = 0
                    sData["studyNum"] = 3
                    firebaseLang?.document(item.lang)?.update(sData)
                }
            }
        }

         */


        /*
        val firebaseLang = firestore?.collection(Utils.userInfo)?.document(auth?.currentUser?.uid!!)
        val sData = HashMap<String,Any?>()
        sData["point"] = 0
        firebaseLang?.update(sData)
         */

        /*
        val gData = GroupDTO()
        gData.group = "en_all"
        val firebaseLang = firestore?.collection(Utils.userInfo)?.document(auth?.currentUser?.uid!!)?.collection(Utils.langInfo2)
        firebaseLang?.document("en")?.collection(Utils.groupInfo)?.document("en_all")?.set(gData)
         */

        /*
        val sData = HashMap<String,Any?>()
        sData["imageUrl"] = ""
        sData["imageFile"] = ""
         */

        /*
        val sExplain = ArrayList<String>()
        sExplain.add("")
        sExplain.add("")
        sExplain.add("")
        sExplain.add("")
        */

        /*
        val firebaseLang = firestore?.collection(Utils.userInfo)?.document(auth?.currentUser?.uid!!)?.collection(Utils.langInfo)
        firebaseLang?.addSnapshotListener {
            querySnapshot, firebaseFirestoreException ->

            if(querySnapshot != null) {
                for (snapshot in querySnapshot) {

                    val item = snapshot.toObject(LangDTO::class.java)!!
                    firebaseLang?.document(item.lang)?.collection(Utils.studyInfo)?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                        if (querySnapshot != null) {
                            for (snapshot2 in querySnapshot) {
                                val item2 = snapshot2.toObject(StudyDTO::class.java)!!

                                val sData = HashMap<String,Any?>()
                                sData["isComplete"] = false

                                firebaseLang?.document(item.lang)?.collection(Utils.studyInfo)?.document(item2.studyId)?.update(sData)


                            }
                        }

                    }
                }

            }
        }

         */


    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_storage -> {
                val storageFragment = StorageFragment()
                supportFragmentManager.beginTransaction().replace(R.id.main_fl, storageFragment).commit()
                return true
            }

            R.id.action_basic-> {
                val  completeFragment = CompleteFragment()
                supportFragmentManager.beginTransaction().replace(R.id.main_fl, completeFragment).commit()
                return true
            }

            R.id.action_calendar -> {
                var calendarFragment = CalendarFragment()
                supportFragmentManager.beginTransaction().replace(R.id.main_fl, calendarFragment).commit()
                return true
            }

            R.id.action_user -> {
                var userFragment = UserFragment()
                supportFragmentManager.beginTransaction().replace(R.id.main_fl, userFragment).commit()
                return true
            }

        }
        return false
    }

    @SuppressLint("RestrictedApi")
    fun BottomNavigationView.disableShiftMode() {
        val menuView = getChildAt(0) as BottomNavigationMenuView
        try {

            val shiftingMode = menuView::class.java.getDeclaredField("mShiftingMode")
            shiftingMode.isAccessible = true
            shiftingMode.setBoolean(menuView, false)
            shiftingMode.isAccessible = false
            for (i in 0 until menuView.childCount) {
                val item = menuView.getChildAt(i) as BottomNavigationItemView
                //item.setShiftingMode(false)
                item.setShifting(false)
                // set once again checked value, so view will be updated
                item.setChecked(item.itemData.isChecked)
            }

        } catch (e: NoSuchFieldException) {
            //Log.e(TAG, "Unable to get shift mode field", e)
        } catch (e: IllegalStateException) {
            //Log.e(TAG, "Unable to change value of shift mode", e)
        }
    }

    override fun onBackPressed() {
        if (pressedTime == 0) {
            Toast.makeText(this, "한 번 더 누르면 앱이 종료됩니다.", Toast.LENGTH_LONG).show()
            pressedTime = System.currentTimeMillis().toInt()
        } else {
            var seconds = System.currentTimeMillis().toInt() - pressedTime

            if (seconds > 3000) {
                Toast.makeText(this, " 한 번 더 누르면 앱이 종료됩니다.", Toast.LENGTH_LONG).show()
                pressedTime = System.currentTimeMillis().toInt()
            } else {
                super.onBackPressed()
                //finish(); // app 종료 시키기
            }
        }
    }


    // firebase 토큰을 가져오는 부분
    private fun initFirebase() {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (task.isSuccessful) { // 성공 시
                    Log.d("aaaa",task.result!!)
                }

            }
    }
    
}
