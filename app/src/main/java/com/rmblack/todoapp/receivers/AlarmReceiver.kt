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
            showAlarmActivity(p0, taskIdString)
        }

    }

    private fun showAlarmActivity(p0: Context?, taskIDString: String) {
        p0?.let { context ->
            val i = Intent(context, AlarmActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            i.putExtra(AlarmActivity.ALARM_ID, taskIDString)
            context.startActivity(i)
        }
    }

}