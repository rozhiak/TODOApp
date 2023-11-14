package com.rmblack.todoapp.models.server.requests

data class ConnectUserRequest(
    val token: String, val new_phone_number: String
)