package com.rmblack.todoapp.alarm

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.rmblack.todoapp.R
import com.rmblack.todoapp.activities.AlarmActivity
import com.rmblack.todoapp.alarm.AlarmUtil.Companion.TASK_ID
import com.rmblack.todoapp.utils.Constants


class AlarmWorker(private val context: Context, params: WorkerParameters): Worker(context, params) {
    override fun doWork(): Result {
        val taskIdString = inputData.getString(TASK_ID) ?: return Result.failure()
        showNotification(context, taskIdString)
        // TODO change alarm state of task to false
        return Result.success()
    }

    private fun showNotification(context: Context, idString: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val soundUri = Uri.parse(
            "android.resource://${context.packageName}/raw/soft_alarm_2010"
        )

        val notificationBuilder = NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_ID)
            .setContentTitle("عنوان کار")
            .setContentText("توضیحات کار اینجا قرار می گیرد")
            .setSmallIcon(R.drawable.icon_for_login)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(false)
            .setSound(soundUri)
        val notification = notificationBuilder.build()
        notificationManager.notify(idString.hashCode(), notification)
    }

    private fun showAlarmActivity(context: Context, taskIDString: String) {
        val intent = Intent(context, AlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra(AlarmActivity.ALARM_ID, taskIDString)
        }
        context.startActivity(intent)
    }
}