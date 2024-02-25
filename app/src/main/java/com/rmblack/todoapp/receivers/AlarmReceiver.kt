package com.rmblack.todoapp.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import com.rmblack.todoapp.R
import com.rmblack.todoapp.data.repository.TaskRepository
import com.rmblack.todoapp.utils.AlarmUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

class AlarmReceiver: BroadcastReceiver() {

    override fun onReceive(p0: Context?, p1: Intent?) {
        val taskIdString = p1?.getStringExtra(AlarmUtil.TASK_ID)

        if (taskIdString != null) {
            changeTaskInDatabase(taskIdString, p0)
            playSound(p0)
        }

    }

    private fun changeTaskInDatabase(taskIdString: String?, p0: Context?) {
        val taskId = UUID.fromString(taskIdString)

        p0?.let { _ ->
            CoroutineScope(Dispatchers.IO).launch {
                val taskRepository = TaskRepository.get()
                val task = taskRepository.getTask(taskId)
                taskRepository.updateAlarm(task.id, false)
            }
        }
    }

    private fun playSound(p0: Context?) {
        val mediaPlayer = MediaPlayer.create(p0, R.raw.soft_alarm_2010)

        mediaPlayer.start()

        mediaPlayer.setOnCompletionListener {
            mediaPlayer.release()
        }
    }

}