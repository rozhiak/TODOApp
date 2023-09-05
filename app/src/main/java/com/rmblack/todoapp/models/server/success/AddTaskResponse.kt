package com.rmblack.todoapp.models.server.success

data class AddTaskResponse(
    val message: String,
    val data: TaskResponse
)

data class TaskResponse(
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
)