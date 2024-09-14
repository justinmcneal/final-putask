package com.example.puttask

import com.example.puttask.api.LoginRequest
import com.example.puttask.api.LoginResponse
import com.example.puttask.api.RegistrationRequest
import com.example.puttask.api.RegistrationResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {
    @POST("/loginPost")  // Match the route for loginPost
    fun login(@Body loginRequest: LoginRequest): Call<LoginResponse>

    @POST("/registrationPost")  // Match the route for registrationPost
    fun register(@Body registrationRequest: RegistrationRequest): Call<RegistrationResponse>
}
