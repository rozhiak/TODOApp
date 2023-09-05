package com.rmblack.todoapp.models.local

import kotlinx.serialization.Serializable

@Serializable
data class User(
    var name: String,
    val phoneNumber: String,
    val privateTasksId: String,
    var sharedTasksId: String
)
