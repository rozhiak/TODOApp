package com.rmblack.todoapp.alarm

import java.util.UUID

interface AlarmUtil {
    fun setAlarm(alarmTime: Long, taskId: UUID): Boolean
    fun cancelAlarm(taskId: UUID)

    companion object {
        const val TASK_ID = "task_id"
    }
}