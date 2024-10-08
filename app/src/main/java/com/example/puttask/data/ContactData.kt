package com.example.puttask.data

data class ContactRequest(
    val message: String,
    val username: String,
    val email: String
)

data class ContactResponse(
    val message: String
)

data class User(
    val username: String,
    val email: String
)
