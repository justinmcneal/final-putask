package com.example.puttask.data

data class EmailRequest(
    val email: String
)

data class EmailResponse(
    val message: String,
    val email_exists: Boolean
)