package com.example.puttask.api

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface APIService {

    // User Registration
    @POST("api/registration")
    suspend fun register(@Body registrationRequest: RegistrationRequest): Response<RegistrationResponse>

    // Get User Info
    @GET("api/user")
    suspend fun getUser(@Header("Authorization") token: String): Response<UserInfo>

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

    // Send Contacts
    @POST("api/contact/send")
    suspend fun sendContactForm(@Body contactRequest: ContactRequest): Response<ContactResponse>

//    // Create Task
//    @POST("api/tasks")
//    suspend fun createTask(@Body createRequest: CreateRequest): Response<CreateResponse>
//
//    // Update Task
//    @PUT("api/tasks/{id}")
//    suspend fun updateTask(@Path("id") id: Int, @Body updateRequest: UpdateRequest): Response<UpdateResponse>
//
//    // Delete Task
//    @DELETE("api/tasks/{id}")
//    suspend fun deleteTask(@Path("id") id: Int): Response<DeleteResponse>
}
