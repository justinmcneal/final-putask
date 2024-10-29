package com.example.puttask.api

data class RegistrationRequest(
    val username: String,
    val email: String,
    val password: String,
    val password_confirmation: String)
data class RegistrationResponse(
    val message: String,
    val token: String,
    val user: UserInfo  // Initialize user here
)


//login
data class LoginRequest(
    val email: String,
    val password: String)
data class LoginResponse(
    val message: String,
    val token: String,
    val user: UserInfo
)

data class UpdateUsernameRequest(
    val username: String)
data class UpdateUsernameResponse(
    val message: String,
    val user: UserInfo)

data class UserInfo (
    val id: Int,
    val username: String,
    val email: String,
    val email_verified_at: String?,
    val created_at: String,
    val updated_at: String,
    val password: String,
    val password_confirmation: String)

data class EmailRequest(
    val email: String)
data class EmailResponse(
    val message: String,
    val email_exists: Boolean)
data class OTPRequest(
    val email: String,
    val otp: Int)
data class OTPResponse(
    val otp_valid: Boolean,
    val message: String)
data class ForgotPasswordRequest(
    val email: String)
data class ForgotPasswordResponse(
    val email_exists: Boolean,
    val message: String)
data class ResetPasswordRequest(
    val email: String,
    val password: String,
    val password_confirmation: String)
data class ResetPasswordResponse(
    val success: Boolean,
    val message: String)

data class ContactRequest(
    val message: String,
    val username: String,
    val email: String)
data class ContactResponse(
    val message: String,
    val data: UserInfo)



