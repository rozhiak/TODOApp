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
    val is_urgent: Int,
    val is_done: Int,
    val is_shared: Int
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
            isUrgent = is_urgent == 1,
            isDone = is_done == 1,
            isShared = is_shared == 1,
            composer = user,
            groupId = group_id,
            serverID = id
        )
    }
}