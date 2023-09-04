package com.rmblack.todoapp.webservice

import com.rmblack.todoapp.models.server.success.AllTasksResponse
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path


interface ApiService {
    @GET("tasks/get/{token}")
    suspend fun getAllTasks(@Path("token") token: String) : Response<AllTasksResponse>

    @FormUrlEncoded
    @POST("tasks/new/")
    suspend fun newTask(
        @Field("token") token: String?,
        @Field("title") title: String?,
        @Field("added_time") addedTime: String?,
        @Field("description") description: String?,
        @Field("deadline") deadline: String?,
        @Field("is_urgent") isUrgent: String?,
        @Field("is_done") isDone: String?,
        @Field("is_shared") isShared: String?,
    ) : Response<String>

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
