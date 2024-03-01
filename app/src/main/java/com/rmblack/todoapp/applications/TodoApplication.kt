package com.rmblack.todoapp.applications

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.rmblack.todoapp.data.repository.TaskRepository
import com.rmblack.todoapp.utils.Constants

class TodoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        TaskRepository.initialize(this)
        makeNotificationChannel()
    }

    private fun makeNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "Alarm"
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_ID,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = "Notifications for alarm events"
            notificationManager.createNotificationChannel(channel)
        }
    }
}