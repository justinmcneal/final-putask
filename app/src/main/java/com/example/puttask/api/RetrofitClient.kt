package com.example.puttask.api

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import android.content.Context

object RetrofitClient {
    private const val BASE_URL = "http://192.168.100.184:8000"

    // Logging interceptor for seeing request/response in logcat
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Function to build OkHttpClient with the token interceptor
    private fun getClient(context: Context): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                val requestBuilder = chain.request().newBuilder()
                // Retrieve the auth token from DataManager
                DataManager(context).getAuthToken()?.let {
                    // Add the token as a Bearer token in the Authorization header
                    requestBuilder.addHeader("Authorization", "Bearer $it")
                }
                chain.proceed(requestBuilder.build())
            }
            .addInterceptor(loggingInterceptor) // Logging interceptor for debugging
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    var gson = GsonBuilder()
        .setLenient()
        .create()

    // Single APIService instance for all API calls
    fun getApiService(context: Context): APIService {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(getClient(context)) // Use the client with the interceptor
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(APIService::class.java)
    }
}
