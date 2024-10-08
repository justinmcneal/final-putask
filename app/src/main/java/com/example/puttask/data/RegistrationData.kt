package com.example.puttask.data

data class RegistrationRequest(
    val username: String,
    val email: String,
    val password: String,
    val password_confirmation: String,
    val accept_terms: Boolean = false // Optional field for terms acceptance
)

data class RegistrationResponse(
    val message: String,
    val token: String,
    val userId: String? = null, // Optional user ID
    val username: String? = null // Optional username
)
