package com.rmblack.todoapp.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import com.rmblack.todoapp.receivers.AlarmReceiver
import java.util.UUID

class AlarmUtil {

    companion object {

        const val ALARM_DEADLINE = "ALARM_DEADLINE"

        fun setAlarm(
            context: Context, alarmTime: Long, taskId: UUID, intentAction: String
        ): Boolean {
            val alarmIntent = Intent(context, AlarmReceiver::class.java)
            alarmIntent.action = intentAction
            alarmIntent.putExtra("TASK_ID", taskId)
            val pendingIntent = PendingIntent.getBroadcast(
                context, taskId.hashCode(), alarmIntent, PendingIntent.FLAG_IMMUTABLE
            )
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (!alarmManager.canScheduleExactAlarms()) {
                    Intent().also { intent ->
                        intent.action = Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                        context.startActivity(intent)
                    }
                }
            }

            return try {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent
                )
                true
            } catch (e: SecurityException) {
                false
            }
        }

        fun cancelAlarm(context: Context, uniqueId: UUID, intentAction: String) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, AlarmReceiver::class.java)
            intent.action = intentAction
            val pendingIntent = PendingIntent.getBroadcast(
                context, uniqueId.hashCode(), intent, PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        }

    }

}