package com.example.puttask.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface APIService {

    // User Registration
    @POST("api/registration")
    suspend fun register(@Body registrationRequest: RegistrationRequest): Response<RegistrationResponse>

    // Get User Info
    @GET("api/user")
    suspend fun getUser(@Header("Authorization") token: String): Response<UserInfo>

    @PUT("api/user/update-username")
    suspend fun updateUsername(@Header("Authorization") token: String, @Body username: UpdateUsernameRequest): Response<UpdateUsernameResponse> // Assuming the response is just a success indicator

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



    // CRUD

    // Get all tasks
    @GET("api/tasks")
    suspend fun getAllTasks(): Response<List<Task>>

    // Get a specific task by ID
    @GET("api/tasks/{id}")
    suspend fun getTaskById(@Path("id") id: String): Response<Task>

    // Create Task
    @POST("api/tasks")
    suspend fun createTask(@Body createRequest: CreateRequest): Response<Task>

    // Update Task
    @PUT("api/tasks/{id}")
    suspend fun updateTask(@Path("id") id: String, @Body updateRequest: UpdateRequest): Response<Task>

    // Delete Task
    @DELETE("api/tasks/{id}")
    suspend fun deleteTask(@Path("id") id: String): Response<DeleteResponse>

    @PATCH("tasks/{id}/complete")
    suspend fun completeTask(@Header("Authorization") token: String, @Path("id") taskId: String): Response<String> // Change to Response<String>

}
