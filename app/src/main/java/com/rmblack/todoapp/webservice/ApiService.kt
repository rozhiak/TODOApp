package com.rmblack.todoapp.webservice

import com.rmblack.todoapp.models.StringResponse
import com.rmblack.todoapp.models.server.requests.AddTaskRequest
import com.rmblack.todoapp.models.server.requests.DeleteTaskRequest
import com.rmblack.todoapp.models.server.requests.EditTaskRequest
import com.rmblack.todoapp.models.server.requests.LoginRequest
import com.rmblack.todoapp.models.server.requests.NewUserRequest
import com.rmblack.todoapp.models.server.requests.ValidateUserRequest
import com.rmblack.todoapp.models.server.success.AllTasksResponse
import com.rmblack.todoapp.models.server.success.TaskResponse
import com.rmblack.todoapp.models.server.success.ValidateUserResponse
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query


interface ApiService {
    @GET("tasks/get/")
    suspend fun getAllTasks(@Query("token") token: String): Response<AllTasksResponse>

    @Headers("Content-Type: application/json", "accept: application/json")
    @POST("tasks/new/")
    suspend fun newTask(@Body task: AddTaskRequest) : Response<TaskResponse>

    @Headers("Content-Type: application/json", "accept: application/json")
    @POST("tasks/edit/")
    suspend fun editTask(@Body body: EditTaskRequest): Response<TaskResponse>

    @Headers("Content-Type: application/json", "accept: application/json")
    @POST("tasks/delete/")
    suspend fun deleteTask(@Body request: DeleteTaskRequest): Response<StringResponse>

    @Headers("accept: application/json", "Content-Type: application/json")
    @POST("users/login/")
    suspend fun loginUser(@Body body: LoginRequest): Response<StringResponse>

    @Headers("accept: application/json", "Content-Type: application/json")
    @POST("users/new/")
    suspend fun newUser(@Body body: NewUserRequest): Response<StringResponse>

    @POST("users/validate/")
    @Headers("accept: application/json", "Content-Type: application/json")
    suspend fun validateUser(@Body body: ValidateUserRequest): Response<ValidateUserResponse>
}
