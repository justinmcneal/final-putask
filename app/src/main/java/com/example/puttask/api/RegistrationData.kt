package com.example.puttask.api

class Registration(
    val username: String,
    val email: String,
    val password: Any,
)

data class RegistrationRequest(
    val username: String,
    val email: String,
    val password: String
)

data class RegistrationResponse(
    val message: String,
    val token: String
)
