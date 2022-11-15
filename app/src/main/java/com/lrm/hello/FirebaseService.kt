package com.lrm.hello

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.lrm.hello.activities.MainActivity
import kotlin.random.Random

class FirebaseService: FirebaseMessagingService() {

    val CHANNEL_ID = "hello_notification_channel"

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onMessageReceived(p0: RemoteMessage) {
        super.onMessageReceived(p0)

        val intent = Intent(this, MainActivity::class.java)
        val notificationManager = getSystemService(NOTIFICATION_SERVICE)as NotificationManager
        val notificationId = Random.nextInt()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivities(this, 0, arrayOf(intent), FLAG_IMMUTABLE)
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(p0.data["title"])
            .setContentText(p0.data["message"])
            .setSmallIcon(R.drawable.app_icon)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {

        val channelName = "ChannelHello"
        val channel = NotificationChannel(CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_HIGH).apply {
            description = "Chats"
            enableLights(true)
            lightColor = Color.WHITE
        }
        notificationManager.createNotificationChannel(channel)
    }
}