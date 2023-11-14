package com.rmblack.todoapp.webservice

import com.rmblack.todoapp.models.StringResponse
import com.rmblack.todoapp.models.server.requests.ConnectUserRequest
import com.rmblack.todoapp.models.server.requests.DeleteTaskRequest
import com.rmblack.todoapp.models.server.requests.DisconnectUserRequest
import com.rmblack.todoapp.models.server.requests.LoginRequest
import com.rmblack.todoapp.models.server.requests.NewUserRequest
import com.rmblack.todoapp.models.server.requests.ServerAddTaskRequest
import com.rmblack.todoapp.models.server.requests.ServerEditTaskRequest
import com.rmblack.todoapp.models.server.requests.UpdateUserRequest
import com.rmblack.todoapp.models.server.requests.ValidateUserRequest
import com.rmblack.todoapp.models.server.success.AllTasksResponse
import com.rmblack.todoapp.models.server.success.ConnectedPhonesResponse
import com.rmblack.todoapp.models.server.success.TaskResponse
import com.rmblack.todoapp.models.server.success.UpdateUserResponse
import com.rmblack.todoapp.models.server.success.UserResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
    @GET("tasks/get/")
    suspend fun getAllTasks(@Query("token") token: String): Response<AllTasksResponse>

    @Headers("Content-Type: application/json", "accept: application/json")
    @POST("tasks/new/")
    suspend fun newTask(@Body task: ServerAddTaskRequest): Response<TaskResponse>

    @Headers("Content-Type: application/json", "accept: application/json")
    @POST("tasks/edit/")
    suspend fun editTask(@Body body: ServerEditTaskRequest): Response<TaskResponse>

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
    suspend fun validateUser(@Body body: ValidateUserRequest): Response<UserResponse>

    @Headers("accept: application/json", "Content-Type: application/json")
    @POST("users/connect/")
    suspend fun connectUser(@Body request: ConnectUserRequest): Response<StringResponse>

    @Headers("accept: application/json", "Content-Type: application/json")
    @POST("users/disconnect/")
    suspend fun disconnectUser(@Body request: DisconnectUserRequest): Response<UserResponse>

    @Headers("Accept: application/json", "Content-Type: application/json")
    @POST("users/update/")
    suspend fun updateUser(@Body request: UpdateUserRequest): Response<UpdateUserResponse>

    @GET("users/connected_users/")
    suspend fun getConnectedPhones(@Query("token") token: String): Response<ConnectedPhonesResponse>
}
