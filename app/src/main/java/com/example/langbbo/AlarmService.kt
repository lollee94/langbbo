package com.example.langbbo

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class AlarmService : BroadcastReceiver() {


    companion object {
        const val NOTIFICATION_CHANNEL_ID = "1000"
        const val NOTIFICATION_ID = 100
    }

    override fun onReceive(context: Context, intent: Intent) {

        // 채널 생성
        createNotificationChannel(context)
        // 알림
        notifyNotification(context)

    }

    private fun createNotificationChannel(context: Context) {
        // context : 실행하고 있는 앱의 상태나 맥락을 담고 있음
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    "학습 알람",
                    NotificationManager.IMPORTANCE_HIGH
            )

            NotificationManagerCompat.from(context)
                    .createNotificationChannel(notificationChannel)
        }
    }

    private fun notifyNotification(context: Context) {

        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)

        with(NotificationManagerCompat.from(context)) {
            val build = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                    .setContentTitle("학습 알람")
                    .setContentText("오늘도 꼭 학습해요!")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setSmallIcon(R.drawable.ic_langbbo)
                    .setContentIntent(pendingIntent)


            notify(NOTIFICATION_ID, build.build())
        }
    }
}