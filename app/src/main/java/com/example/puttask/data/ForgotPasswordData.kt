package com.example.puttask.data

data class ForgotPasswordRequest(
    val email: String // The email of the user who forgot the password
)

data class ForgotPasswordResponse(
    val email_exists: Boolean,  // Indicates if the email exists in the system
    val message: String         // A message about the status of the forgot password request
)

data class ResetPasswordRequest(
    val password: String,
    val password_confirmation: String
)


data class ResetPasswordResponse(
    val success: Boolean,
    val message: String
)
