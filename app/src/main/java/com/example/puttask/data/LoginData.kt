package com.example.puttask.data

data class LoginRequest(
    val email: String,
    val password: String,
    val rememberMe: Boolean? = false // Optional field for remember me feature
)

data class LoginResponse(
    val message: String,
    val token: String,
    val userId: String? = null, // Optional user ID
    val username: String? = null // Optional username if needed
)
