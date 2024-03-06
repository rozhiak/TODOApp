package com.rmblack.todoapp.alarm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
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
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.rmblack.todoapp.R
import com.rmblack.todoapp.activities.AlarmActivity
import com.rmblack.todoapp.activities.MainActivity
import com.rmblack.todoapp.alarm.AlarmUtil.Companion.TASK_ID
import com.rmblack.todoapp.data.repository.TaskRepository
import com.rmblack.todoapp.models.local.Task
import com.rmblack.todoapp.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID


class AlarmWorker(private val context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    val taskRepository = TaskRepository.get()

    override suspend fun doWork(): Result {
        val taskIdString = inputData.getString(TASK_ID) ?: return Result.failure()
        val uuid = UUID.fromString(taskIdString)
        val task = getTaskFromRoom(uuid)
        task?.let {
            prepareNotification(it)
            changeTaskAlarmState(uuid)
        }
        return Result.success()
    }

    private suspend fun getTaskFromRoom(id: UUID): Task? {
        return withContext(Dispatchers.IO) {
            taskRepository.getTask(id)
        }
    }

    private suspend fun changeTaskAlarmState(id: UUID) {
        withContext(Dispatchers.IO) {
            taskRepository.updateAlarm(id, false)
        }
    }

    private fun prepareNotification(task: Task) {
        val soundUri =
            Uri.parse(
                ContentResolver.SCHEME_ANDROID_RESOURCE +
                        "://" + applicationContext.packageName + "/" + R.raw.soft_alarm_2010
            )
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        createChannel(soundUri, notificationManager)
        showNotification(soundUri, notificationManager, task)
    }

    private fun showNotification(
        soundUri: Uri?,
        notificationManager: NotificationManager,
        task: Task
    ) {
        val notification =
            NotificationCompat.Builder(applicationContext, Constants.NOTIFICATION_CHANNEL_ID)
        val remoteViews = RemoteViews(applicationContext.packageName, R.layout.notification_layout)
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
        val intent = Intent(applicationContext, MainActivity::class.java)
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
            .setVibrate(longArrayOf(0, 500, 1000))
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

    private fun showAlarmActivity(context: Context, taskIDString: String) {
        val intent = Intent(context, AlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra(AlarmActivity.ALARM_ID, taskIDString)
        }
        context.startActivity(intent)
    }
}