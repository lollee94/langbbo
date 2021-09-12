package com.example.langbbo

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class FirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(p0: String) {
        // 토큰이 갱신될 때마다 처리 해주는 작업 여기에 필요 (실무에서)
        super.onNewToken(p0)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // FCM 수신 마다 실행
        super.onMessageReceived(remoteMessage)

        Log.d("bbbb", "fcm received")

        // 오레오 이상이면 채널 생성
        createNotificationChannel()

        Log.d("cccc", remoteMessage.data["title"].toString())
        val title = remoteMessage.data["title"]
        val content = remoteMessage.data["content"]


        // 실제로 노티파이를 하기 위해서
        // 메세지가 왔을 때 타이틀과 메세지에 맞게 각각 보여줌
        NotificationManagerCompat.from(this)// 매니저 가져옴
            .notify(0, createNotification(title, content))
    }


    private fun createNotification(
        title: String?,
        message: String?
    ): Notification {

        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("notificationType","0 타입")
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }

        val pendingIntent = PendingIntent.getActivity(this, 0, intent, FLAG_UPDATE_CURRENT)
        // type.id 를 통해 각 타입별로 받아올 수 있도록
        // FLAG_UPDATE_CURRENT 각각의 알림에서는 팬딩인텐트가 동일 하도록

        // 실제 알림 컨텐츠 만들기
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_langbbo)// 아이콘 보여주기
            .setContentTitle(title) // 메세지 에서 받은 타이틀 활용
            .setContentText(message) // 메세지 에서 받은 메세지 활용
            .setPriority(NotificationCompat.PRIORITY_DEFAULT) // 오레오 이하 버전 에서는 지정 필요
            .setContentIntent(pendingIntent)
            .setAutoCancel(true) // 알림 클릭시 자동 제거

        return notificationBuilder.build()
    }


    private fun createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.description = CHANNEL_DESCRIPTION

            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                    .createNotificationChannel(channel)
        }
    }

    companion object {
        private const val CHANNEL_NAME = "Emoji Party"
        private const val CHANNEL_DESCRIPTION = "Emoji Party를 위한 채널"
        private const val CHANNEL_ID = "Channel Id"
    }
}