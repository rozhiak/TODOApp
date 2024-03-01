package com.rmblack.todoapp.alarm

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.rmblack.todoapp.R
import com.rmblack.todoapp.activities.AlarmActivity
import com.rmblack.todoapp.alarm.AlarmUtil.Companion.TASK_ID

class AlarmWorker(private val context: Context, params: WorkerParameters): Worker(context, params) {
    override fun doWork(): Result {
        val taskIdString = inputData.getString(TASK_ID) ?: return Result.failure()
        showAlarmActivity(context, taskIdString)
        showNotification(context)
        return Result.success()
    }

    private fun showNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Alarm")
            .setContentText("Time to wake up!")
            .setSmallIcon(R.drawable.icon_for_login)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true) // Keeps notification visible
            .setSound(soundUri) // Set your desired sound

//        notificationBuilder.flags = notificationBuilder.flags or Notification.FLAG_INSISTENT // Plays sound even in Do Not Disturb

        val notification = notificationBuilder.build()
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun showAlarmActivity(context: Context, taskIDString: String) {
        val intent = Intent(context, AlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra(AlarmActivity.ALARM_ID, taskIDString)
        }
        context.startActivity(intent)
    }
}