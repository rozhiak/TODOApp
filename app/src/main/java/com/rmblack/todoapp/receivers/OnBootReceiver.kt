package com.rmblack.todoapp.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.rmblack.todoapp.alarm.AlarmScheduler
import com.rmblack.todoapp.alarm.AlarmSchedulerImpl
import com.rmblack.todoapp.data.repository.TaskRepository
import com.rmblack.todoapp.models.local.Task
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class OnBootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var taskRepository: TaskRepository

    override fun onReceive(p0: Context?, p1: Intent?) {
        if (p1?.action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            CoroutineScope(Dispatchers.IO).launch {
                val tasks = taskRepository.getTasks()
                val scheduler = AlarmSchedulerImpl(p0!!)
                reScheduleAlarm(tasks, scheduler)
            }
        }
    }

    private fun reScheduleAlarm(tasks: List<Task>, scheduler: AlarmScheduler) {
        for (t in tasks) {
            if (t.alarm) {
                scheduler.schedule(t.deadLine.timeInMillis, t.id)
            }
        }
    }
}