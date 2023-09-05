package com.rmblack.todoapp.webservice.repository

import com.rmblack.todoapp.models.server.ServerTask
import com.rmblack.todoapp.webservice.ApiService

class ApiRepository constructor(private val retrofitService: ApiService) {
    suspend fun getAllTasks(token: String) = retrofitService.getAllTasks(token)

    suspend fun addNewTask(task: ServerTask) = retrofitService.newTask(task)
}