package com.rmblack.todoapp.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.rmblack.todoapp.data.repository.TaskRepository
import com.rmblack.todoapp.utils.AlarmUtil
import java.util.UUID

class AlarmReceiver: BroadcastReceiver() {
    private val taskRepository = TaskRepository.get()

    override fun onReceive(p0: Context?, p1: Intent?) {
        // todo put null on alarm property of related task

        val taskIdString = p1?.getStringExtra(AlarmUtil.TASK_ID)

        if (taskIdString != null) {
            val taskId = UUID.fromString(taskIdString)

            if (taskId != null) {
                Toast.makeText(p0, "alarm", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(p0, "no id", Toast.LENGTH_LONG).show()
            }
        }

    }

}