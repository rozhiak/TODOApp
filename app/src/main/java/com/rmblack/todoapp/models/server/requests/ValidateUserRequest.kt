package com.rmblack.todoapp.models.server.requests

data class ValidateUserRequest(
    val phone_number: String, val code: Int
)