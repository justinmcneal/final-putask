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
    val userId: String? = null, // Make userId nullable if it might not always be present
    val username: String,
    val email: String,
    // Add any other fields your API might return
)

