package com.example.puttask.api

import com.example.puttask.data.Task
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface TaskApi {

    @GET("tasks")
    suspend fun getTasks(): Response<List<Task>>

    @POST("tasks")
    suspend fun createTask(@Body task: Task): Response<Task>
}
