package com.rmblack.todoapp.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.rmblack.todoapp.data.repository.TaskRepository
import java.util.UUID

class AlarmReceiver: BroadcastReceiver() {
    private val taskRepository = TaskRepository.get()

    override fun onReceive(p0: Context?, p1: Intent?) {
        // todo put null on alarm property of related task

        val taskId = p1?.getSerializableExtra("TASK_ID") as UUID?

        if (taskId != null) {
            taskRepository.updateAlarm(taskId, false)
            Toast.makeText(p0, "alarm", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(p0, "no id", Toast.LENGTH_LONG).show()
        }

    }

}