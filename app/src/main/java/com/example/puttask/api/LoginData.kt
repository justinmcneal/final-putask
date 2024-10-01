package com.example.puttask.api

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val message: String
)