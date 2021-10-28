package com.example.langbbo

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.langbbo.modelDTO.DateDTO
import com.example.langbbo.modelDTO.LangDTO
import com.example.langbbo.modelDTO.StudyDTO
import com.example.langbbo.modelDTO.UserDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.CalendarMode
import kotlinx.android.synthetic.main.activity_form.*
import kotlinx.android.synthetic.main.fragment_calendar.*
import kotlinx.android.synthetic.main.fragment_calendar.view.*
import kotlinx.android.synthetic.main.fragment_complete.view.*
import kotlinx.android.synthetic.main.fragment_form1.view.*
import kotlinx.android.synthetic.main.fragment_user.view.*
import kotlinx.android.synthetic.main.item_language.view.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class CalendarFragment: Fragment() {

    var auth : FirebaseAuth? = null
    var firestore: FirebaseFirestore? = null
    var uid : String = ""
    var lang : String = ""
    var point : Int = 0

    var fView : View? = null

    var registerNum : Int = 0
    var studyNum : Int = 0

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        uid = auth?.currentUser?.uid!!

        fView = inflater.inflate(R.layout.fragment_calendar, container, false)

        val nFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())
        nFormat.currency = Currency.getInstance(Locale.getDefault())

        val todayTimestamp = SimpleDateFormat("yyMMdd").format(Date())

        firestore?.collection(Utils.userInfo)?.document(uid)?.addSnapshotListener {
            documentSnapshot, firebaseFirestoreException ->

            if (documentSnapshot != null) {
                var uItem = documentSnapshot.toObject(UserDTO::class.java)!!
                lang = uItem.lang

                var todayRegister = 0
                var todayStudy = 0


                firestore?.collection(Utils.userInfo)?.document(auth?.currentUser?.uid!!)
                        ?.collection(Utils.langInfo)?.document(lang)?.addSnapshotListener {
                            documentSnapshot, firebaseFirestoreException ->

                            if (documentSnapshot != null) {
                                var lItem = documentSnapshot.toObject(LangDTO::class.java)!!
                                point = lItem.point
                                registerNum = lItem.registerNum
                                studyNum = lItem.studyNum
                                fView!!.calendar_point_tv.text = "${nFormat.format(point)}"
                            }
                        }



                firestore?.collection(Utils.userInfo)?.document(uid)
                        ?.collection(Utils.langInfo)?.document(lang)
                        ?.collection(Utils.studyInfo)?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->

                            if(querySnapshot != null){

                                for(snapshot in querySnapshot){
                                    var item = snapshot.toObject(StudyDTO::class.java)!!

                                    // 0 이상 6 미만
                                    if(item.timestamp.toString().substring(0, 6) == todayTimestamp){

                                        todayRegister += 1
                                    }
                                }

                                fView!!.calendar_todayRegister_tv.text = "$todayRegister / $registerNum"

                            }

                        }

                firestore?.collection(Utils.userInfo)?.document(uid)
                        ?.collection(Utils.langInfo)?.document(lang)
                        ?.collection(Utils.completeInfo)?.addSnapshotListener {
                            querySnapshot, firebaseFirestoreException ->

                            if(querySnapshot != null) {

                                for (snapshot in querySnapshot) {
                                    var item = snapshot.toObject(StudyDTO::class.java)!!

                                    // 0 이상 6 미만
                                    if(item.completeTimestamp.toString().substring(0, 6) == todayTimestamp){
                                        todayStudy += 1
                                    }
                                }

                                fView!!.calendar_todayStudy_tv.text = "$todayStudy / $studyNum"

                            }
                        }

                // Calendar Start

                fView!!.calendar.visibility = View.GONE

                firestore?.collection(Utils.userInfo)?.document(uid)
                    ?.collection(Utils.langInfo)?.document(lang)
                    ?.collection(Utils.dateInfo)?.addSnapshotListener {
                            querySnapshot, firebaseFirestoreException ->


                        // calendar 구성, 7월 1일 이후로만 보여지게 할 것.
                        fView!!.calendar.state().edit()
                            .setMinimumDate(CalendarDay.from(2021, 7, 1))
                            .commit()


                        // 오늘 날짜는 회색 동그라미로 표시

                        context?.let {
                            val todayDecorator = TodayDecorator(context!!)
                            fView!!.calendar.addDecorators(todayDecorator)
                        }


                        if(querySnapshot != null) {

                            val dateList = ArrayList<CalendarDay>()
                            var isThereToday = false
                            for (snapshot in querySnapshot) {
                                var item = snapshot.toObject(DateDTO::class.java)!!

                                val year = Integer.parseInt("20${item.timestamp.toString().substring(0,2)}")
                                val month = Integer.parseInt(item.timestamp.toString().substring(2,4))
                                val day = Integer.parseInt(item.timestamp.toString().substring(4,6))

                                dateList.add(CalendarDay.from(year, month, day))

                                if(item.timestamp.toString().substring(0, 6) == todayTimestamp){
                                    isThereToday = true
                                }
                            }


                            context?.let {
                                // 데이트 있는 날짜는 빨간 동그라미
                                for (date in dateList) {
                                    val dateDecorator = DateDecorator(context!!, date)
                                    fView!!.calendar.addDecorators(dateDecorator)
                                }
                            }

                            // 캘린더 보이게 설정
                            fView!!.calendar.visibility = View.VISIBLE


                            // data 추가하는 버튼
                            fView!!.calendar_addDate_lo.setOnClickListener {
                                if(!isThereToday && (todayRegister >= registerNum || todayStudy >= studyNum)) {
                                    var sUid = uid.substring(0, 3)
                                    val timeStamp = SimpleDateFormat( "yyMMddHHmmss").format(Date())
                                    val dateId = "d_${sUid}_$timeStamp"

                                    var dateDTO = DateDTO()
                                    dateDTO.dateId = dateId
                                    dateDTO.timestamp = timeStamp.toLong()

                                    firestore?.collection(Utils.userInfo)?.document(uid)
                                        ?.collection(Utils.langInfo)?.document(lang)
                                        ?.collection(Utils.dateInfo)
                                        ?.document(dateId)?.set(dateDTO)?.addOnSuccessListener {
                                            Toast.makeText(
                                                context,
                                                "달력에 추가되었습니다!",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }



                                    val lData = HashMap<String,Any?>()
                                    lData["point"] = point + 1000
                                    firestore?.collection(Utils.userInfo)?.document(uid)?.collection(Utils.langInfo)?.document(lang)?.update(lData)

                                }
                                else if(isThereToday){
                                    Toast.makeText(context, "오늘은 이미 목표를 달성했어요!", Toast.LENGTH_LONG).show()
                                }
                                else {
                                    Toast.makeText(context, "위 미션을 달성해주세요!", Toast.LENGTH_LONG).show()
                                }

                            }

                        }

                    }


                //val dateList = ArrayList<CalendarDay>()
                //dateList.add(CalendarDay.from(2021, 7, 1))
                //dateList.add(CalendarDay.from(2021, 7, 2))

                // Calendar End

                //fView!!.complete_rv.adapter = CompleteRecyclerviewAdapter()
                //fView!!.complete_rv.layoutManager = LinearLayoutManager(context)
            }
        }





        return fView
    }

}
