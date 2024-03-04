package com.rmblack.todoapp.alarm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.rmblack.todoapp.R
import com.rmblack.todoapp.utils.Constants

class AlarmService: Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createNotification(this)
        startForeground(1, notification)
        return START_STICKY
    }

    private fun createNotification(context: Context): Notification {
        // Create your notification using NotificationCompat.Builder
        val notification = NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_ID)
            // Set notification properties like title, content, icon, etc.
            .build()
        return notification
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
                CHANNEL_ID, "Utils.CHANNEL_NAME", NotificationManager.IMPORTANCE_HIGH
            )
            mChannel.lightColor = Color.GRAY
            mChannel.enableLights(true)
            mChannel.description = "Description"
            val audioAttributes =
                AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION).build()
            mChannel.setSound(soundUri, audioAttributes)
            mNotificationManager.createNotificationChannel(mChannel)
        }


        //General code:
        val status = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
        status.setAutoCancel(true).setWhen(System.currentTimeMillis())
            .setSmallIcon(R.drawable.icon_for_login) //.setOnlyAlertOnce(true)
            .setContentTitle("title").setContentText("messageBody")
            .setVibrate(longArrayOf(0, 500, 1000)).setDefaults(Notification.DEFAULT_LIGHTS)
            .setSound(Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + context.packageName + "/" + R.raw.soft_alarm_2010))


        mNotificationManager.notify(idString.hashCode(), status.build())
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }
}