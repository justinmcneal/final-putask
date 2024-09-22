package com.example.puttask.api

data class RegistrationRequest(
    val username: String,
    val email: String,
    val password: String,
    val password_confirmation: String
)