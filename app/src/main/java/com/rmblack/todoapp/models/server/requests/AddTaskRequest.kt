package com.rmblack.todoapp.models.server.requests

import java.util.UUID

data class AddTaskRequest(
    val token: String,
    val title: String,
    val added_time: String,
    val description: String,
    val deadline: String,
    val is_urgent: Boolean,
    val is_done: Boolean,
    val is_shared: Boolean,
    val localTaskID: UUID
) {
    fun convertToServerAddModel(): ServerAddTaskRequest {
        return ServerAddTaskRequest(
            token,
            title,
            added_time,
            description,
            deadline,
            is_urgent,
            is_done,
            is_shared
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
    val is_shared: Boolean,
)


