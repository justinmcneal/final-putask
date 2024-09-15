package com.example.puttask

import com.example.puttask.api.LoginRequest
import com.example.puttask.api.LoginResponse
import com.example.puttask.api.RegistrationRequest
import com.example.puttask.api.RegistrationResponse
import com.example.puttask.api.User
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthService {
    @FormUrlEncoded
    @POST("api/loginPost")  // Match the route for loginPost
    fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<LoginResponse>

    @POST("api/registrationPost")
    fun register(@Body registrationRequest: RegistrationRequest): Call<RegistrationResponse>

    @GET("api/user")
    fun getUser(): Call<User>
}