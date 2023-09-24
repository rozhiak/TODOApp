package com.rmblack.todoapp.models.server.success

import com.google.gson.annotations.SerializedName

class UserResponse (
    val message: String,
    @SerializedName("data")
    val user: User
)


data class User(
    val name: String,
    @SerializedName("phone_number")
    val phoneNumber: String,
    @SerializedName("private_tasks_id")
    val privateTasksId: String,
    @SerializedName("shared_tasks_id")
    val sharedTasksId: String,
    val token: String
)