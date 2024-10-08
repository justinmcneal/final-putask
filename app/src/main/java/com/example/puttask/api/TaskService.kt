package com.example.puttask.api

import com.example.puttask.data.*
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface TaskService {

    //CREATE
    @POST("api/tasks")
    fun createTask(@Body createRequest: CreateRequest): Call<CreateResponse>

    //UPDATE
    @PUT("api/tasks/{id}") // Assuming you use the task ID in the URL to update a specific task
    fun updateTask(@Path("id") id: Int, @Body updateRequest: UpdateRequest): Call<UpdateResponse>

    //DELETE
    @DELETE("api/tasks/{id}") // Assuming you use the task ID in the URL to delete a specific task
    fun deleteTask(@Path("id") id: Int): Call<DeleteResponse>
}
