package com.rmblack.todoapp.webservice

import com.rmblack.todoapp.models.server.ServerTask
import com.rmblack.todoapp.models.server.success.AddTaskResponse
import com.rmblack.todoapp.models.server.success.AllTasksResponse
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path


interface ApiService {

    @GET("tasks/get/{token}")
    suspend fun getAllTasks(@Path("token") token: String) : Response<AllTasksResponse>

    @Headers("Content-Type: application/json", "accept: application/json")
    @POST("tasks/new/")
    suspend fun newTask(@Body task: ServerTask) : Response<AddTaskResponse>

    companion object {
        var apiService: ApiService? = null
        fun getInstance() : ApiService {
            if (apiService == null) {
                val retrofit = Retrofit.Builder()
                    .baseUrl("https://todo-test-h7ld.onrender.com/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                apiService = retrofit.create(ApiService::class.java)
            }
            return apiService!!
        }
    }
}
