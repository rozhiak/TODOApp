package com.rmblack.todoapp.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.rmblack.todoapp.receivers.AlarmReceiver
import java.util.UUID

class AlarmSchedulerImpl(private val context: Context): AlarmScheduler {

    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    override fun schedule(alarmTime: Long, taskId: UUID) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            !alarmManager.canScheduleExactAlarms()) {
            return
        }
        val now  = System.currentTimeMillis()
        if (alarmTime > now) {
            val intent = Intent(context, AlarmReceiver::class.java).apply {
                putExtra(TASK_ID_KEY, taskId.toString())
            }
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                alarmTime,
                PendingIntent.getBroadcast(
                    context,
                    taskId.hashCode(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )
        }
    }

    override fun cancel(taskID: UUID) {
        alarmManager.cancel(
            PendingIntent.getBroadcast(
                context,
                taskID.hashCode(),
                Intent(context, AlarmReceiver::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
    }

    companion object {
        const val TASK_ID_KEY = "TASK_ID_KEY"
    }
}