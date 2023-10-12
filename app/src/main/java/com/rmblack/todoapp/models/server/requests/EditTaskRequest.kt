package com.rmblack.todoapp.models.server.requests

import java.util.UUID

data class EditTaskRequest(
    val token: String,
    val task_id: String,
    val title: String,
    val description: String,
    val deadline: String,
    val is_urgent: Boolean,
    val is_done: Boolean,
    val is_shared: Boolean,
    val localTaskId: UUID
) {
    fun convertToServerEditModel(): ServerEditTaskRequest {
        return ServerEditTaskRequest(
            token,
            task_id,
            title,
            description,
            deadline,
            is_urgent,
            is_done,
            is_shared
        )
    }
}

data class ServerEditTaskRequest(
    val token: String,
    val task_id: String,
    val title: String,
    val description: String,
    val deadline: String,
    val is_urgent: Boolean,
    val is_done: Boolean,
    val is_shared: Boolean,
)