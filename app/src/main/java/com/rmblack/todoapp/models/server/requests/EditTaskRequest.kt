package com.rmblack.todoapp.models.server.requests

import java.util.UUID

data class EditTaskRequest(
    val token: String,
    val taskId: String,
    val title: String,
    val description: String,
    val deadline: String,
    val isUrgent: Boolean,
    val isDone: Boolean,
    val isShared: Boolean,
    val localTaskId: UUID
) {
    fun convertToServerEditModel(): ServerEditTaskRequest {
        return ServerEditTaskRequest(
            token,
            taskId,
            title,
            description,
            deadline,
            isUrgent,
            isDone,
            isShared
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