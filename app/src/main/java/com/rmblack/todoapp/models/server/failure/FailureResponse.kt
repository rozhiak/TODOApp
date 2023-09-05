package com.rmblack.todoapp.models.server.failure

data class FailureResponse(
    val detail: String
)

data class ErrorDetail(
    val loc: List<String>,
    val msg: String,
    val type: String
)