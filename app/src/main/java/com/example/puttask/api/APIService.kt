package com.example.puttask.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface APIService {

    @POST("api/registration")
    suspend fun register(@Body registrationRequest: RegistrationRequest): Response<RegistrationResponse>
    @GET("api/user")
    suspend fun getUser(@Header("Authorization") token: String): Response<UserInfo>
    @PUT("api/user/update-username")
    suspend fun updateUsername(@Header("Authorization") token: String, @Body username: UpdateUsernameRequest): Response<UpdateUsernameResponse> // Assuming the response is just a success indicator
    @POST("api/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>
    @POST("api/check-email")
    suspend fun checkEmail(@Body forgotPasswordRequest: ForgotPasswordRequest): Response<ForgotPasswordResponse>

    @POST("api/send-otp")
    suspend fun sendOTP(@Body emailRequest: EmailRequest): Response<EmailResponse>
    @POST("api/verify-otp")
    suspend fun verifyOTP(@Body otpRequest: OTPRequest): Response<OTPResponse>
    @POST("api/password/reset")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<ResetPasswordResponse>

    @POST("api/contact/send")
    suspend fun sendContactForm(@Body contactRequest: ContactRequest): Response<ContactResponse>

    @GET("api/tasks")
    suspend fun getAllTasks(): Response<List<Task>>
    @GET("api/tasks/{id}")
    suspend fun getTaskById(@Path("id") id: String): Response<Task>
    @POST("api/tasks")
    suspend fun createTask(@Body createRequest: CreateRequest): Response<Task>
    @PUT("api/tasks/{id}")
    suspend fun updateTask(@Path("id") id: String, @Body updateRequest: UpdateRequest): Response<Task>
    @DELETE("api/tasks/{id}")
    suspend fun deleteTask(@Path("id") id: String): Response<DeleteResponse>
    @PUT("api/tasks/{id}/complete")
    suspend fun markTaskComplete(@Path("id") id: String, @Body request: CompleteTaskRequest): Response<Task>
}
