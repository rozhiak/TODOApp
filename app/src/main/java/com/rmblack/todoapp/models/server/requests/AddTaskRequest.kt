package com.rmblack.todoapp.models.server.requests

data class AddTaskRequest(
    val token: String,
    val title: String,
    val added_time: String,
    val description: String,
    val deadline: String,
    val is_urgent: Boolean,
    val is_done: Boolean,
    val is_shared: Boolean
)


