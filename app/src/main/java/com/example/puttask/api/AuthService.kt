package com.example.puttask.api

import com.example.puttask.data.*
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthService {

    // User Login
    @POST("api/loginPost")
    fun login(@Body loginRequest: LoginRequest): Call<LoginResponse>

    // User Registration
    @POST("api/registrationPost")
    fun register(@Body registrationRequest: RegistrationRequest): Call<RegistrationResponse>

    @GET("api/user")
    suspend fun getUser(@Header("Authorization") token: String): UserInfo





    // Forgot Password - Validate Email (this could potentially send OTP)
    @POST("api/check-email")
    fun checkEmail(@Body emailRequest: EmailRequest): Call<EmailResponse>

    // Send OTP separately (if needed, but often combined with forgotPassword)
    @POST("api/send-otp")
    fun sendOTP(@Body emailRequest: EmailRequest): Call<EmailResponse>

    // Verify the OTP entered by the user
    @POST("api/verify-otp")
    fun verifyOTP(@Body otpRequest: OTPRequest): Call<OTPResponse>

    @POST("/reset-password")
    fun resetPassword(@Body resetRequest: ForgotPasswordRequest): Call<ForgotPasswordResponse>
}
