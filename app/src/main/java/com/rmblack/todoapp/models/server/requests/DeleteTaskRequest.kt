package com.rmblack.todoapp.models.server.requests

data class DeleteTaskRequest(
    val token: String, val task_id: String
)