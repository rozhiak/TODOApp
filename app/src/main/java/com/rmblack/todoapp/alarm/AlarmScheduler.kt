package com.rmblack.todoapp.alarm

import java.util.UUID

interface AlarmScheduler {
    fun schedule(alarmTime: Long, taskId: UUID)
    fun cancel(taskID: UUID)
}