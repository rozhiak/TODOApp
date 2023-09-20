package com.rmblack.todoapp.webservice.repository

import com.rmblack.todoapp.models.server.requests.AddTaskRequest
import com.rmblack.todoapp.models.server.requests.DeleteTaskRequest
import com.rmblack.todoapp.models.server.requests.EditTaskRequest
import com.rmblack.todoapp.models.server.requests.LoginRequest
import com.rmblack.todoapp.models.server.requests.NewUserRequest
import com.rmblack.todoapp.models.server.requests.ValidateUserRequest
import com.rmblack.todoapp.webservice.ApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiRepository {

    suspend fun getAllTasks(token: String) = getInstance().getAllTasks(token)

    suspend fun addNewTask(body: AddTaskRequest) = getInstance().newTask(body)

    suspend fun deleteTask(body: DeleteTaskRequest) = getInstance().deleteTask(body)

    suspend fun editTask(body: EditTaskRequest) = getInstance().editTask(body)

    suspend fun loginUser(body: LoginRequest) = getInstance().loginUser(body)

    suspend fun newUser(body: NewUserRequest) = getInstance().newUser(body)

    suspend fun validateUser(body: ValidateUserRequest) = getInstance().validateUser(body)

    companion object {
        private var apiService: ApiService? = null
        private fun getInstance() : ApiService {
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