package com.rmblack.todoapp.webservice.repository

import com.rmblack.todoapp.models.server.requests.AddTaskRequest
import com.rmblack.todoapp.models.server.requests.DeleteTaskRequest
import com.rmblack.todoapp.models.server.requests.EditTaskRequest
import com.rmblack.todoapp.models.server.requests.LoginRequest
import com.rmblack.todoapp.webservice.ApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiRepository constructor(private val retrofitService: ApiService) {
    suspend fun getAllTasks(token: String) = retrofitService.getAllTasks(token)

    suspend fun addNewTask(body: AddTaskRequest) = retrofitService.newTask(body)

    suspend fun deleteTask(body: DeleteTaskRequest) = retrofitService.deleteTask(body)

    suspend fun editTask(body: EditTaskRequest) = retrofitService.editTask(body)

    suspend fun loginUser(body: LoginRequest) = retrofitService.loginUser(body)

    companion object {
        var apiService: ApiService? = null
        fun getInstance() : ApiService {
            if (apiService == null) {
                val retrofit = Retrofit.Builder()
                    .baseUrl("https://amirh.pythonanywhere.com/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                apiService = retrofit.create(ApiService::class.java)
            }
            return apiService!!
        }
    }
}