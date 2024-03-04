package com.rmblack.todoapp.alarm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.rmblack.todoapp.R
import com.rmblack.todoapp.activities.AlarmActivity
import com.rmblack.todoapp.alarm.AlarmUtil.Companion.TASK_ID


class AlarmWorker(private val context: Context, params: WorkerParameters): Worker(context, params) {
    override fun doWork(): Result {
        val taskIdString = inputData.getString(TASK_ID) ?: return Result.failure()
        showNotification(context, taskIdString)
        // TODO change alarm state of task to false
        return Result.success()
    }

    private fun showNotification(context: Context, idString: String) {
        val CHANNEL_ID = "1234"

        val soundUri =
            Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + applicationContext.packageName + "/" + R.raw.soft_alarm_2010)
        val mNotificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        //For API 26+ you need to put some additional code like below:
        val mChannel: NotificationChannel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mChannel = NotificationChannel(
                CHANNEL_ID,
                "Utils.CHANNEL_NAME",
                NotificationManager.IMPORTANCE_HIGH
            )
            mChannel.lightColor = Color.GRAY
            mChannel.enableLights(true)
            mChannel.description = "Description"
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()
            mChannel.setSound(soundUri, audioAttributes)
            mNotificationManager.createNotificationChannel(mChannel)
        }


        //General code:
        val status = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
        status.setAutoCancel(true)
            .setWhen(System.currentTimeMillis())
            .setSmallIcon(R.drawable.icon_for_login) //.setOnlyAlertOnce(true)
            .setContentTitle("title")
            .setContentText("messageBody")
            .setVibrate(longArrayOf(0, 500, 1000))
            .setDefaults(Notification.DEFAULT_LIGHTS)
            .setSound(Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + context.packageName + "/" + R.raw.soft_alarm_2010))


        mNotificationManager.notify(idString.hashCode(), status.build())
    }

    private fun showAlarmActivity(context: Context, taskIDString: String) {
        val intent = Intent(context, AlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra(AlarmActivity.ALARM_ID, taskIDString)
        }
        context.startActivity(intent)
    }
}