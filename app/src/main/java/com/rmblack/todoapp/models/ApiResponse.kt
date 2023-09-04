package com.rmblack.todoapp.models

data class ApiResponse(
    val message: String,
    val data: UserData
)

data class UserData(
    val private: List<Task>,
    val shared: List<Task>
)


