package com.rmblack.todoapp.webservice.repository

import com.rmblack.todoapp.models.server.requests.AddTaskRequest
import com.rmblack.todoapp.models.server.requests.DeleteTaskRequest
import com.rmblack.todoapp.models.server.requests.EditTaskRequest
import com.rmblack.todoapp.models.server.requests.LoginRequest
import com.rmblack.todoapp.webservice.ApiService

class ApiRepository constructor(private val retrofitService: ApiService) {
    suspend fun getAllTasks(token: String) = retrofitService.getAllTasks(token)

    suspend fun addNewTask(body: AddTaskRequest) = retrofitService.newTask(body)

    suspend fun deleteTask(body: DeleteTaskRequest) = retrofitService.deleteTask(body)

    suspend fun editTask(body: EditTaskRequest) = retrofitService.editTask(body)

    suspend fun loginUser(body: LoginRequest) = retrofitService.loginUser(body)
}