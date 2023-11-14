package com.rmblack.todoapp.models.server.requests

import java.util.UUID

data class AddTaskRequest(
    val token: String,
    val title: String,
    val addedTime: String,
    val description: String,
    val deadline: String,
    val isUrgent: Boolean,
    val isDone: Boolean,
    val isShared: Boolean,
    val localTaskID: UUID
) {
    fun convertToServerAddModel(): ServerAddTaskRequest {
        return ServerAddTaskRequest(
            token, title, addedTime, description, deadline, isUrgent, isDone, isShared
        )
    }
}

data class ServerAddTaskRequest(
    val token: String,
    val title: String,
    val added_time: String,
    val description: String,
    val deadline: String,
    val is_urgent: Boolean,
    val is_done: Boolean,
    val is_shared: Boolean
)


