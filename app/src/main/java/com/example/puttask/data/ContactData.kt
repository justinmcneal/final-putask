package com.example.puttask.data

data class ContactFormData(
    val name: String,
    val email: String,
    val message: String
)

data class ContactRequest(
    val message: String
)

data class ContactResponse(
    val message: String
)