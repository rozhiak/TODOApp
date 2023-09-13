package com.rmblack.todoapp.models.server.requests

data class NewUserRequest (
    val phone_number: String,
    val name: String
)