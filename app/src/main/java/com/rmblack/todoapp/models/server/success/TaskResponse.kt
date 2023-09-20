package com.rmblack.todoapp.models.server.success

import com.aminography.primecalendar.persian.PersianCalendar
import com.rmblack.todoapp.models.local.Task
import java.util.UUID

data class TaskResponse(
    val message: String,
    val data: ServerTask
)


//TODO Here I changed booleans to int and vise versa, maybe it cause some issues (the debug should be done in server.) I
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
    fun convertToLocalTask(): Task {
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

    fun checkEquality(localTask: Task): Boolean {
        if (title != localTask.title) return false
        else if (description != localTask.description) return false
        else if (is_done != localTask.isDone) return false
        else if (is_shared != localTask.isShared) return false
        else if (is_urgent != localTask.isUrgent) return false
        else if (added_time.toLong() != localTask.addedTime.timeInMillis) return false
        else if (deadline.toLong() != localTask.deadLine.timeInMillis) return false
        else if (id != localTask.serverID) return false
        else if (user != localTask.composer) return false
        else if (group_id != localTask.groupId) return false
        return true
    }
}