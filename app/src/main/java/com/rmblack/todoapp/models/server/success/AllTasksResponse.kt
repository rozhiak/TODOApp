package com.rmblack.todoapp.models.server.success

import com.rmblack.todoapp.models.Task

data class AllTasksResponse(
    val message: String,
    val data: Tasks
)

data class Tasks(
    val private: List<Task>,
    val shared: List<Task>
)


