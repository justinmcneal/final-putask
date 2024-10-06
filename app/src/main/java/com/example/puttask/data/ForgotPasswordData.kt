package com.example.puttask.data

data class ForgotPasswordRequest(
    val email: String // The email of the user who forgot the password
)

data class ForgotPasswordResponse(
    val email_exists: Boolean,  // Indicates if the email exists in the system
    val message: String         // A message about the status of the forgot password request
)