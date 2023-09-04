package com.rmblack.todoapp.webservice.repository

import com.rmblack.todoapp.webservice.ApiService

class ApiRepository constructor(private val retrofitService: ApiService) {
    suspend fun getAllTasks(token: String) = retrofitService.getAllTasks(token)

    suspend fun addNewTask(
        token: String?,
        title: String?,
        addedTime: String?,
        description: String?,
        deadline: String?,
        isUrgent: String?,
        isDone: String?,
        isShared: String?,
    ) = retrofitService.newTask(
        token,
        title,
        addedTime,
        description,
        deadline,
        isUrgent,
        isDone,
        isShared
    )
}