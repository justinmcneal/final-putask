package com.example.puttask.api

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT

interface APIService {

    // User Registration
    @POST("api/registration")
    suspend fun register(@Body registrationRequest: RegistrationRequest): Response<RegistrationResponse>

    // Get User Info
    @GET("api/user")
    suspend fun getUser(@Header("Authorization") token: String): Response<UserInfo>

    // Update User Info
    @PUT("api/user/update")
    suspend fun updateUsername(@Header("Authorization") token: String, @Body username: String): Response<Unit> // Assuming the response is just a success indicator

    // User Login
    @POST("api/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>

    // Forgot Password - Validate Email
    @POST("api/check-email")
    suspend fun checkEmail(@Body forgotPasswordRequest: ForgotPasswordRequest): Response<ForgotPasswordResponse>

    // Send OTP
    @POST("api/send-otp")
    suspend fun sendOTP(@Body emailRequest: EmailRequest): Response<EmailResponse>

    // Verify the OTP
    @POST("api/verify-otp")
    suspend fun verifyOTP(@Body otpRequest: OTPRequest): Response<OTPResponse>

    // Reset Password
    @POST("api/password/reset")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<ResetPasswordResponse>

    // Send Contact Form
    @POST("/api/contact/send") // Update this path to match your API endpoint
    suspend fun sendContactForm(@Body contactRequest: ContactRequest): Response<ContactResponse>

    // If you need to keep another method, make sure to distinguish their purposes
    // For example, you might want to use `submitContactForm` for a different endpoint or functionality

    // Create Task
    // @POST("api/tasks")
    // suspend fun createTask(@Body createRequest: CreateRequest): Response<CreateResponse>

    // Update Task
    // @PUT("api/tasks/{id}")
    // suspend fun updateTask(@Path("id") id: Int, @Body updateRequest: UpdateRequest): Response<UpdateResponse>

    // Delete Task
    // @DELETE("api/tasks/{id}")
    // suspend fun deleteTask(@Path("id") id: Int): Response<DeleteResponse>
}
