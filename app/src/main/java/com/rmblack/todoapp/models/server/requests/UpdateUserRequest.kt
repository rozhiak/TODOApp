package com.rmblack.todoapp.models.server.requests

data class UpdateUserRequest(
    val token: String, val name: String
)