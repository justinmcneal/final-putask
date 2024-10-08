package com.example.puttask.data

data class OTPRequest(
    val email: String,   // The email address associated with the OTP
    val otp: Int   // The OTP value entered by the user
)

data class OTPResponse(
    val otp_valid: Boolean,   // Indicates whether the OTP is valid
    val message: String       // A message about the OTP verification status (e.g., "OTP Verified", "Invalid OTP")
)


