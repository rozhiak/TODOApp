package com.rmblack.todoapp.models.server.failure

data class ErrorResponse(
    val detail: List<ErrorDetail>
)

data class ErrorDetail(
    val loc: List<String>,
    val msg: String,
    val type: String
)