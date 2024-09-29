package com.example.puttask.api

import com.example.puttask.data.LoginRequest
import com.example.puttask.data.LoginResponse
import com.example.puttask.data.RegistrationRequest
import com.example.puttask.data.RegistrationResponse
import com.example.puttask.data.UserData
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthService {
    @POST("api/loginPost")
    fun login(@Body loginRequest: LoginRequest): Call<LoginResponse>


    @POST("api/registrationPost")
    fun register(@Body registrationRequest: RegistrationRequest): Call<RegistrationResponse>

//    I keep muna ito
//    @GET("api/user")
//    fun getUser(): Call<UserData>
}