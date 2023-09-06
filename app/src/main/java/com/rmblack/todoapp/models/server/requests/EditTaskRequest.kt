package com.rmblack.todoapp.models.server.requests

data class EditTaskRequest(
    val token: String,
    val task_id: String,
    val title: String,
    val deadline: String,
    val is_urgent: Boolean,
    val is_done: Boolean,
    val is_shared: Boolean
)