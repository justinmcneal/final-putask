package com.example.puttask.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import okhttp3.ResponseBody

// Data class for the contact form
data class ContactFormData(
    val name: String,
    val email: String,
    val message: String
)


