package com.example.putask.api

data class RegistrationResponse(
    val message: String,
    val user: User,
    val token: String
)

data class User(
    val id: Int,
    val username: String,
    val email: String
)
