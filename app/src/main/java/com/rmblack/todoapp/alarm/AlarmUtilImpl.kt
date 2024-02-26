import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import com.rmblack.todoapp.alarm.AlarmUtil
import com.rmblack.todoapp.receivers.AlarmReceiver
import java.util.UUID

class AlarmUtilImpl(private val context: Context) : AlarmUtil {
    private val alarmManager: AlarmManager? = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

    override fun setAlarm(alarmTime: Long, taskId: UUID): Boolean {
        val alarmIntent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(AlarmUtil.TASK_ID, taskId.toString())
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.hashCode(),
            alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager?.canScheduleExactAlarms()!!) {
                Intent().also { intent ->
                    intent.action = Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                    context.startActivity(intent)
                }
            }
        }

        return try {
            alarmManager?.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent
            )
            true
        } catch (e: Exception) {
            false
        }
    }

    override fun cancelAlarm(taskId: UUID) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager?.cancel(pendingIntent)
    }
}