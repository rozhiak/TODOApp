package com.rmblack.todoapp.receivers

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.rmblack.todoapp.R
import com.rmblack.todoapp.activities.MainActivity
import com.rmblack.todoapp.alarm.AlarmSchedulerImpl.Companion.TASK_ID_KEY
import com.rmblack.todoapp.data.repository.TaskRepository
import com.rmblack.todoapp.models.local.Task
import com.rmblack.todoapp.utils.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class AlarmReceiver: BroadcastReceiver() {

    val taskRepository = TaskRepository.get()

    override fun onReceive(p0: Context?, p1: Intent?) {
        val taskIdString = p1?.getStringExtra(TASK_ID_KEY) ?: return
        val uuid = UUID.fromString(taskIdString)
        CoroutineScope(Dispatchers.IO).launch {
            val task = taskRepository.getTask(uuid) ?: return@launch
            withContext(Dispatchers.Default) {
                prepareNotification(task, p0!!)
            }
            taskRepository.updateAlarm(uuid, false)
        }
    }

    private fun prepareNotification(task: Task, context: Context) {
        val soundUri =
            Uri.parse(
                ContentResolver.SCHEME_ANDROID_RESOURCE +
                        "://" + context.packageName + "/" + R.raw.soft_alarm_2010
            )
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        createChannel(soundUri, notificationManager)
        showNotification(soundUri, notificationManager, task, context)
    }

    private fun showNotification(
        soundUri: Uri?,
        notificationManager: NotificationManager,
        task: Task,
        context: Context
    ) {
        val notification =
            NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_ID)
        val remoteViews = RemoteViews(context.packageName, R.layout.notification_layout)
        if (task.title.length > 40) {
            val trimmedTitle = task.title.substring(0, 40) + " ..."
            remoteViews.setTextViewText(R.id.tv_title, trimmedTitle)
        } else {
            remoteViews.setTextViewText(R.id.tv_title, task.title)

        }
        if (task.description.isNotBlank() || task.description.isNotEmpty()) {
            remoteViews.setViewVisibility(R.id.tv_description, View.VISIBLE)
            if (task.description.length > 100) {
                val trimmedDescription = task.description.substring(0, 100) + " ..."
                remoteViews.setTextViewText(R.id.tv_description, trimmedDescription)
            } else {
                remoteViews.setTextViewText(R.id.tv_description, task.description)
            }
        }
        val requestID = System.currentTimeMillis().toInt()
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            requestID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        notification
            .setAutoCancel(false)
            .setWhen(System.currentTimeMillis())
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle(task.title)
            .setVibrate(longArrayOf(100, 200))
            .setDefaults(Notification.DEFAULT_LIGHTS)
            .setSound(soundUri)
            .setContent(remoteViews)
            .setContentIntent(pendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        notificationManager.notify(task.id.hashCode(), notification.build())
    }

    private fun createChannel(
        soundUri: Uri?,
        notificationManager: NotificationManager
    ) {
        val channel: NotificationChannel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_ID,
                "Alarm notification",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.lightColor = Color.GRAY
            channel.enableLights(true)
            channel.description = "Shows alarms as notifications"
            channel.enableVibration(true)
            channel.vibrationPattern = longArrayOf(0, 500, 1000)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()
            channel.setSound(soundUri, audioAttributes)
            notificationManager.createNotificationChannel(channel)
        }
    }
}