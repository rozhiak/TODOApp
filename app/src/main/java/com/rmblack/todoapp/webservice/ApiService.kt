package com.rmblack.todoapp.webservice

import com.rmblack.todoapp.models.server.requests.AddTaskRequest
import com.rmblack.todoapp.models.server.requests.DeleteTaskRequest
import com.rmblack.todoapp.models.server.requests.EditTaskRequest
import com.rmblack.todoapp.models.server.success.TaskResponse
import com.rmblack.todoapp.models.server.success.AllTasksResponse
import com.rmblack.todoapp.models.server.success.SuccessResponse
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
    suspend fun newTask(@Body task: AddTaskRequest) : Response<TaskResponse>







    //Start fom here
    @Headers("Content-Type: application/json", "accept: application/json")
    @POST("tasks/edit/")
    suspend fun editTask(@Body body: EditTaskRequest): Response<TaskResponse>
    //




    @Headers("Content-Type: application/json", "accept: application/json")
    @POST("tasks/delete/")
    suspend fun deleteTask(@Body request: DeleteTaskRequest): Response<SuccessResponse>

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
