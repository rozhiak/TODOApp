package com.rmblack.todoapp.models.server.success

data class UpdateUserResponse(
    val message: String, val data: UserData
)

data class UserData(
    val name: String,
    val phone_number: String,
    val private_tasks_id: String,
    val shared_tasks_id: String,
    val token: String
)