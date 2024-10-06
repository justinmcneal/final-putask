package com.example.puttask.api

import com.example.puttask.data.LoginRequest
import com.example.puttask.data.LoginResponse
import com.example.puttask.data.RegistrationRequest
import com.example.puttask.data.RegistrationResponse
import com.example.puttask.data.EmailRequest
import com.example.puttask.data.EmailResponse
import com.example.puttask.data.UserInfo
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthService {

    @POST("api/loginPost")
    fun login(@Body loginRequest: LoginRequest): Call<LoginResponse>

    @POST("api/registrationPost")
    fun register(@Body registrationRequest: RegistrationRequest): Call<RegistrationResponse>

    @GET("api/user")
    suspend fun getUser(
        @Header("Authorization") token: String // Pass the auth token in the header
    ): UserInfo

    @POST("api/forgotPassword") // Add forgot password endpoint
    fun checkEmail(@Body emailRequest: EmailRequest): Call<EmailResponse>
}
