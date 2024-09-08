package com.example.putask.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.Response



object RetrofitClient {

    private const val BASE_URL = "http://127.0.0.1:8000/api/"

    interface ApiService {
        @POST("login")
        suspend fun loginUser(@Body LoginRequest: LoginRequest): Response<LoginResponse>
    }

    val instance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
