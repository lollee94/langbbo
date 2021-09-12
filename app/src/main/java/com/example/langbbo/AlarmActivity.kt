package com.example.langbbo

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_alarm.*
import java.util.*


class AlarmActivity : AppCompatActivity() {
    private var timePicker: TimePicker? = null
    private var alarmManager: AlarmManager? = null
    private var hour = 0
    private var minute = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm)

        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        timePicker = findViewById(R.id.tp_timepicker)

        alarm_register_btn.setOnClickListener { register() }
        alarm_cancel_btn.setOnClickListener { cancel() }

        alarm_sch.isChecked = true
        alarm_sch.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked){
                alarm_sch_tv.text = "aaa"
            }
            else{
                alarm_sch_tv.text = "bbb"
            }
        }
    } // onCreate()..


    fun register() {

        Toast.makeText(this, "등록되었습니다.", Toast.LENGTH_LONG).show()

        val intent = Intent(this, AlarmService::class.java)
        val pIntent = PendingIntent.getBroadcast(this, 0, intent, 0)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            hour = timePicker!!.hour
            minute = timePicker!!.minute
        }
        val calendar = Calendar.getInstance()
        calendar[Calendar.HOUR_OF_DAY] = hour
        calendar[Calendar.MINUTE] = minute
        calendar[Calendar.SECOND] = 0
        calendar[Calendar.MILLISECOND] = 0


        // 지정한 시간에 매일 알림
        alarmManager!!.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_HALF_DAY,
                pIntent)



    } // regist()..

    fun cancel() {

        //Toast.makeText(this, "취소되었습니다.", Toast.LENGTH_LONG).show()

        val intent = Intent(this, AlarmService::class.java)
        val pIntent = PendingIntent.getBroadcast(this, 0, intent, 0)


        alarmManager!!.cancel(pIntent)


    } // unregist()..
} // MainActivity class..
