package com.rmblack.todoapp.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.rmblack.todoapp.activities.AlarmActivity
import com.rmblack.todoapp.data.repository.TaskRepository
import com.rmblack.todoapp.models.local.Task
import com.rmblack.todoapp.utils.AlarmUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

class AlarmReceiver: BroadcastReceiver() {

    override fun onReceive(p0: Context?, p1: Intent?) {
        val taskIdString = p1?.getStringExtra(AlarmUtil.TASK_ID)

        if (taskIdString != null) {
            val taskId = UUID.fromString(taskIdString)
            val taskRepository = TaskRepository.get()
            CoroutineScope(Dispatchers.IO).launch {
                val task = taskRepository.getTask(taskId)
                taskRepository.updateAlarm(task.id, false)
                showAlarmActivity(p0, task)
            }
        }

    }

    private fun showAlarmActivity(p0: Context?, task: Task) {
        p0?.let { context ->
            val i = Intent(context, AlarmActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            i.putExtra(AlarmActivity.ALARM_TITLE, task.title)
            i.putExtra(AlarmActivity.ALARM_DESCRIPTION, task.description)
            context.startActivity(i)
        }
    }

}