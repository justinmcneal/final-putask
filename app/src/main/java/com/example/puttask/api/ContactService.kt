package com.example.puttask.api

import com.example.puttask.data.ContactRequest
import com.example.puttask.data.User
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ContactService {
    @POST("api/send")
    fun sendContactForm(@Body contactRequest: ContactRequest): Call<ResponseBody>

    @GET("user")
    fun getUserDetails(): Call<User>

}

