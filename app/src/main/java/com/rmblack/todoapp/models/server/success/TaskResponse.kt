package com.rmblack.todoapp.models.server.success

import com.aminography.primecalendar.persian.PersianCalendar
import com.rmblack.todoapp.models.local.Task
import java.util.UUID

data class TaskResponse(
    val message: String,
    val data: ServerTask
)

data class ServerTask(
    val title: String,
    val id: String,
    val added_time: String,
    val user: String,
    val group_id: String,
    val description: String,
    val deadline: String,
    val is_urgent: Boolean,
    val is_done: Boolean,
    val is_shared: Boolean
) {
    fun convertToTask(): Task {
        val addedTime = PersianCalendar()
        addedTime.timeInMillis = added_time.toLong()
        val deadLine = PersianCalendar()
        deadLine.timeInMillis = deadline.toLong()

        return Task(
            title = title,
            id = UUID.randomUUID(),
            description = description,
            addedTime = addedTime,
            deadLine = deadLine,
            isUrgent = is_urgent,
            isDone = is_done,
            isShared = is_shared,
            composer = user,
            groupId = group_id,
            serverID = id
        )
    }
}