package com.rmblack.todoapp.models.server.success

data class AllTasksResponse(
    val message: String, val data: Tasks
)

data class Tasks(
    val private: List<ServerTask>, val shared: List<ServerTask>
)


