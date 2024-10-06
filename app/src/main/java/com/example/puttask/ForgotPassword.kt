package com.example.puttask

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.puttask.api.RetrofitClient
import com.example.puttask.data.EmailRequest
import com.example.puttask.data.EmailResponse
import com.example.puttask.data.OTPRequest
import com.example.puttask.data.OTPResponse
import com.example.puttask.data.ResetPasswordRequest
import com.example.puttask.data.ResetPasswordResponse
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ForgotPassword : AppCompatActivity() {

    private lateinit var etForgotEmail: EditText
    private lateinit var etOTP: EditText
    private lateinit var etNewPassword: EditText // Declare new password field
    private lateinit var etConfirmNewPassword: EditText // Declare confirm password field
    private lateinit var btnSendOTP: Button
    private lateinit var btnVerifyOTP: Button
    private lateinit var btnResetPassword: Button // Declare reset password button
    private lateinit var tvResendOTP: TextView

    private var isEmailVerified = false // Track if the email has been verified
    private var retryCount = 0 // Retry count for sending OTP
    private var lastFailedAttemptTime: Long = 0 // Track the time of the last failed attempt
    private val cooldownTimeMillis: Long = 60_000 // 1 minute cooldown
    private val timeoutDurationMillis: Long = 30 * 60 * 1000 // 30 minutes timeout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        // Initialize views
        etForgotEmail = findViewById(R.id.etForgotEmail)
        etOTP = findViewById(R.id.etOTP)
        etNewPassword = findViewById(R.id.etNewPassword) // Initialize new password field
        etConfirmNewPassword = findViewById(R.id.etConfirmNewPassword) // Initialize confirm password field
        btnSendOTP = findViewById(R.id.btnOTP)
        btnVerifyOTP = findViewById(R.id.btnVerifyOTP)
        btnResetPassword = findViewById(R.id.btnResetPassword) // Initialize reset password button
        tvResendOTP = findViewById(R.id.tvResendOTP)

        // Send OTP button listener
        btnSendOTP.setOnClickListener {
            val email = etForgotEmail.text.toString().trim()

            if (email.isNotEmpty() && isValidEmail(email)) {
                Log.d("EmailInput", "Email entered: $email") // Log email
                sendPasswordResetRequest(email)
            } else {
                Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show()
            }
        }


        // Resend OTP button listener
        tvResendOTP.setOnClickListener {
            val email = etForgotEmail.text.toString().trim()
            if (email.isNotEmpty() && isValidEmail(email) && isEmailVerified) {
                sendOTP(EmailRequest(email)) // Resend OTP
                Toast.makeText(this, "OTP Resent", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Please enter a valid email and verify first", Toast.LENGTH_SHORT).show()
            }
        }

        // Verify OTP button listener
        btnVerifyOTP.setOnClickListener {
            val otp = etOTP.text.toString().toIntOrNull()
            val email = etForgotEmail.text.toString()
            if (otp != null) {
                // Call function to verify OTP
                verifyOTP(email, otp)
            } else {
                Toast.makeText(this, "Please enter the OTP", Toast.LENGTH_SHORT).show()
            }
        }

        // Reset Password button listener
        btnResetPassword.setOnClickListener {
            val newPassword = etNewPassword.text.toString()
            val confirmPassword = etConfirmNewPassword.text.toString()

            if (newPassword.isNotEmpty() && newPassword == confirmPassword) {
                // If passwords match, call the resetPassword function
                resetPassword(etForgotEmail.text.toString().trim(), newPassword)
            } else {
                // Show an error if passwords don't match
                Toast.makeText(this, "Passwords do not match or are empty", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun sendPasswordResetRequest(email: String) {
        val emailRequest = EmailRequest(email)

        // Call the API to check if the email exists
        RetrofitClient.authService.checkEmail(emailRequest).enqueue(object : Callback<EmailResponse> {
            override fun onResponse(call: Call<EmailResponse>, response: Response<EmailResponse>) {
                if (response.isSuccessful) {
                    val emailResponse = response.body()

                    // Log the JSON response for debugging
                    val jsonResponse = Gson().toJson(emailResponse)
                    Log.d("EmailCheckResponse", "JSON Response: $jsonResponse")

                    if (emailResponse?.email_exists == true) {
                        Toast.makeText(this@ForgotPassword, emailResponse.message, Toast.LENGTH_SHORT).show()
                        isEmailVerified = true
                        etOTP.visibility = View.VISIBLE
                        btnVerifyOTP.visibility = View.VISIBLE
                        tvResendOTP.visibility = View.VISIBLE

                        // Call to send OTP
                        sendOTP(emailRequest)
                    } else {
                        Toast.makeText(this@ForgotPassword, "Email not found in the system", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    Toast.makeText(this@ForgotPassword, "Error: ${response.message()}\n$errorBody", Toast.LENGTH_SHORT).show()
                    Log.d("Error", "Error: ${response.message()}\n$errorBody")
                }
            }

            override fun onFailure(call: Call<EmailResponse>, t: Throwable) {
                Toast.makeText(this@ForgotPassword, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("EmailCheckFailure", "Error: ${t.message}", t)
            }
        })
    }

    private fun sendOTP(emailRequest: EmailRequest) {
        // Check if we are in a timeout period
        if (retryCount >= 3 && System.currentTimeMillis() - lastFailedAttemptTime < timeoutDurationMillis) {
            Toast.makeText(this, "Too many attempts. Please try again later.", Toast.LENGTH_SHORT).show()
            return
        }

        // Call the API to send OTP
        RetrofitClient.authService.sendOTP(emailRequest).enqueue(object : Callback<EmailResponse> {
            override fun onResponse(call: Call<EmailResponse>, response: Response<EmailResponse>) {
                if (response.isSuccessful) {
                    // Log the JSON response for debugging
                    val jsonResponse = Gson().toJson(response.body())
                    Log.d("SendOTPResponse", "JSON Response: $jsonResponse")

                    // Reset retry count on successful OTP send
                    retryCount = 0
                    lastFailedAttemptTime = 0
                    Toast.makeText(this@ForgotPassword, "OTP sent to your email", Toast.LENGTH_SHORT).show()
                } else {
                    // Handle failed response
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    Toast.makeText(this@ForgotPassword, "Error: ${response.message()}\n$errorBody", Toast.LENGTH_SHORT).show()

                    retryCount++
                    lastFailedAttemptTime = System.currentTimeMillis() // Update last failed attempt time

                    if (retryCount < 3) {
                        // Start cooldown timer before the next attempt
                        startCooldown(emailRequest)
                    } else {
                        Toast.makeText(this@ForgotPassword, "Maximum attempts reached. Please wait 30 minutes.", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<EmailResponse>, t: Throwable) {
                // Handle network failure
                val errorMessage = t.message ?: "Network error"
                Toast.makeText(this@ForgotPassword, "Error: $errorMessage", Toast.LENGTH_SHORT).show()

                retryCount++
                lastFailedAttemptTime = System.currentTimeMillis() // Update last failed attempt time

                if (retryCount < 3) {
                    // Start cooldown timer before the next attempt
                    startCooldown(emailRequest)
                } else {
                    Toast.makeText(this@ForgotPassword, "Maximum attempts reached. Please wait 30 minutes.", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun startCooldown(emailRequest: EmailRequest) {
        Toast.makeText(this@ForgotPassword, "Please wait for 1 minute before retrying...", Toast.LENGTH_SHORT).show()
        Handler(Looper.getMainLooper()).postDelayed({
            retryCount = 0 // Reset retry count after cooldown
            sendOTP(emailRequest) // Allow retry after cooldown
        }, cooldownTimeMillis)
    }

    private fun verifyOTP(email: String, otp: Int) {
        val otpRequest = OTPRequest(email, otp)

        // Call the API to verify OTP
        RetrofitClient.authService.verifyOTP(otpRequest).enqueue(object : Callback<OTPResponse> {
            override fun onResponse(call: Call<OTPResponse>, response: Response<OTPResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@ForgotPassword, "OTP verified successfully", Toast.LENGTH_SHORT).show()

                    // Proceed to reset password
                    etNewPassword.visibility = View.VISIBLE
                    etConfirmNewPassword.visibility = View.VISIBLE
                    btnResetPassword.visibility = View.VISIBLE
                } else {
                    Toast.makeText(this@ForgotPassword, "Invalid OTP. Please try again.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<OTPResponse>, t: Throwable) {
                Toast.makeText(this@ForgotPassword, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("OTPVerificationFailure", "Error: ${t.message}", t)
            }
        })
    }

    private fun resetPassword(newPassword: String, confirmPassword: String) {
        val resetPasswordRequest = ResetPasswordRequest(newPassword, confirmPassword)

        // Log the new password and confirmation before making the API call
        Log.d("ResetPasswordInput", "New Password: $newPassword, Confirm Password: $confirmPassword")

        RetrofitClient.authService.resetPassword(resetPasswordRequest).enqueue(object : Callback<ResetPasswordResponse> {
            override fun onResponse(call: Call<ResetPasswordResponse>, response: Response<ResetPasswordResponse>) {
                if (response.isSuccessful) {
                    Log.d("ResetPassword", "Success: ${response.body()?.message}")
                    Toast.makeText(this@ForgotPassword, "Password reset successfully", Toast.LENGTH_SHORT).show()
                    finish() // Close the activity
                } else {
                    // Log the full error response from the backend
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    Log.e("ResetPasswordError", "Error resetting password: Code: ${response.code()} - Message: ${response.message()} - Error Body: $errorBody")

                    // Show a toast to notify the user
                    Toast.makeText(this@ForgotPassword, "Error resetting password: ${response.message()}\n$errorBody", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResetPasswordResponse>, t: Throwable) {
                Toast.makeText(this@ForgotPassword, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("ResetPasswordFailure", "Error: ${t.message}", t)
            }
        })
    }








    private fun isValidEmail(email: String): Boolean {
        // Simple email validation
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
