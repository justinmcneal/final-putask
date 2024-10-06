package com.example.puttask.api

import com.example.puttask.data.ContactRequest
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ContactApiService {

    @POST("contact-form")
    fun sendContactForm(@Body contactRequest: ContactRequest): Call<ResponseBody> // Change this to accept ContactRequest
}
