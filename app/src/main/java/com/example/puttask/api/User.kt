package com.example.puttask.api

data class User(
    val id: Int,
    val username: String,
    val email: String,
    val email_verified_at: String?,
    val created_at: String,
    val updated_at: String,
    val password: String,
    val password_confirmation: String
)