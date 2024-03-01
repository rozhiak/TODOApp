import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.rmblack.todoapp.alarm.AlarmUtil
import com.rmblack.todoapp.alarm.AlarmUtil.Companion.TASK_ID
import com.rmblack.todoapp.alarm.AlarmWorker
import java.util.UUID

class AlarmUtilImpl(private val context: Context) : AlarmUtil {

    override fun setAlarm(alarmTime: Long, taskId: UUID): Boolean {
        return try {
            val delay = alarmTime - System.currentTimeMillis()
            val workRequest = OneTimeWorkRequestBuilder<AlarmWorker>()
                .setInitialDelay(delay, java.util.concurrent.TimeUnit.MILLISECONDS)
                .setInputData(workDataOf(TASK_ID to taskId.toString()))
                .addTag(taskId.toString())
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                taskId.toString(),
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
            true
        } catch (e: Exception) {
            false
        }
    }

    override fun cancelAlarm(taskId: UUID) {
        WorkManager.getInstance(context).cancelAllWorkByTag(taskId.toString())
    }
}