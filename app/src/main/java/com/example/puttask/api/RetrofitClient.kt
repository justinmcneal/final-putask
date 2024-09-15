package com.example.puttask.api

import com.example.puttask.AuthService
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "http://192.168.1.9:8000/"

    // Logging interceptor for seeing request/response in logcat
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    val gson = GsonBuilder()
        .setLenient()
        .create()

    // OkHttpClient to attach interceptors
    // Updated OkHttpClient with timeout settings
    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)  // For logging HTTP requests and responses
        .connectTimeout(30, TimeUnit.SECONDS)  // Connection timeout of 30 seconds
        .readTimeout(30, TimeUnit.SECONDS)     // Read timeout of 30 seconds
        .writeTimeout(30, TimeUnit.SECONDS)    // Write timeout of 30 seconds
        .build()


    val authService: AuthService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthService::class.java)
    }
}