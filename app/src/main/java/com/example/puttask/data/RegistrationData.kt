package com.example.puttask.data

data class RegistrationRequest(
    val username: String,
    val email: String,
    val password: String,
    val password_confirmation: String
)

data class RegistrationResponse(
    val success: Boolean,
    val message: String
)