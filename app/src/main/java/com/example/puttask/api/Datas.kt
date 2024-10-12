package com.example.puttask.api

data class RegistrationRequest(
    val username: String,
    val email: String,
    val password: String,
    val password_confirmation: String
)

data class RegistrationResponse(
    val message: String,
    val token: String
)



data class UserInfo (
    val id: Int,
    val username: String,
    val email: String,
    val email_verified_at: String?,
    val created_at: String,
    val updated_at: String,
    val password: String,
    val password_confirmation: String
)


data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val message: String,
    val token: String
)


// this is for forgot password and shit
data class EmailRequest(
    val email: String
)

data class EmailResponse(
    val message: String,
    val email_exists: Boolean
)



data class OTPRequest(
    val email: String,   // The email address associated with the OTP
    val otp: Int   // The OTP value entered by the user
)

data class OTPResponse(
    val otp_valid: Boolean,   // Indicates whether the OTP is valid
    val message: String       // A message about the OTP verification status (e.g., "OTP Verified", "Invalid OTP")
)



data class ForgotPasswordRequest(
    val email: String // The email of the user who forgot the password
)

data class ForgotPasswordResponse(
    val email_exists: Boolean,  // Indicates if the email exists in the system
    val message: String         // A message about the status of the forgot password request
)



data class ResetPasswordRequest(
    val email: String, // Add this line
    val password: String,
    val password_confirmation: String
)

data class ResetPasswordResponse(
    val success: Boolean,
    val message: String
)



data class ContactRequest(
    val message: String,
    val username: String,
    val email: String
)

data class ContactResponse(
    val message: String,
    val username: String,
    val email: String
)



data class User(
    val username: String,
    val email: String
)


