package com.example.puttask.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("http://127.0.0.1:8000/api/tasks")  // Replace with your backend URL
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: TaskApi by lazy {
        retrofit.create(TaskApi::class.java)
    }
}
