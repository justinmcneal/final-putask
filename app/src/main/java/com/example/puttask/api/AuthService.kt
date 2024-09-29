package com.example.puttask.api

import com.example.puttask.data.LoginRequest
import com.example.puttask.data.LoginResponse
import com.example.puttask.data.RegistrationRequest
import com.example.puttask.data.RegistrationResponse
import com.example.puttask.data.UserInfo
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthService {
    @POST("api/loginPost")
    fun login(@Body loginRequest: LoginRequest): Call<LoginResponse>

    @POST("api/registrationPost")
    fun register(@Body registrationRequest: RegistrationRequest): Call<RegistrationResponse>

    @GET("api/user")
    fun getUser(): Call<UserInfo>
}