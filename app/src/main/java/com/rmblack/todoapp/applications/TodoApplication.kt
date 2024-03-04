package com.rmblack.todoapp.applications

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentResolver
import android.content.Context
import android.graphics.Color
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import com.rmblack.todoapp.R
import com.rmblack.todoapp.data.repository.TaskRepository
import com.rmblack.todoapp.utils.Constants


class TodoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        TaskRepository.initialize(this)
//        makeNotificationChannel()
    }

    private fun makeNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val soundUri =
                Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + applicationContext.packageName + "/" + R.raw.soft_alarm_2010)
            val channelName = "Alarm"
            val notificationManager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = "Notifications for alarm events"
            channel.lightColor = Color.GREEN
            channel.enableLights(true)
            val audioAttributes =
                AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION).build()

            channel.setSound(soundUri, audioAttributes)
            notificationManager.createNotificationChannel(channel)
        }
    }
}