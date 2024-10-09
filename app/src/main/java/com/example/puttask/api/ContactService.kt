package com.example.puttask.api

import com.example.puttask.data.ContactRequest
import com.example.puttask.data.User
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface ContactService {
    @POST("api/send")
    suspend fun sendContactForm(
        @Header("Authorization") token: String, @Body contactRequest: ContactRequest): Response<ResponseBody>

    @GET("api/user")
    suspend fun getUserDetails(@Header("Authorization") token: String): Response<User>
}


