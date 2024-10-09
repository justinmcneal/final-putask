package com.example.puttask

import android.os.Bundle
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
import com.example.puttask.data.ForgotPasswordRequest
import com.example.puttask.data.ForgotPasswordResponse
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
    private lateinit var etNewPassword: EditText
    private lateinit var etConfirmNewPassword: EditText
    private lateinit var btnSendOTP: Button
    private lateinit var btnVerifyOTP: Button
    private lateinit var btnResetPassword: Button
    private lateinit var tvResendOTP: TextView

    private var isEmailVerified = false
    private var retryCount = 0
    private var lastFailedAttemptTime: Long = 0
    private val cooldownTimeMillis: Long = 60_000 // 1 minute cooldown
    private val timeoutDurationMillis: Long = 30 * 60 * 1000 // 30 minutes timeout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        // Initialize views
        etForgotEmail = findViewById(R.id.etForgotEmail)
        etOTP = findViewById(R.id.etOTP)
        etNewPassword = findViewById(R.id.etNewPassword)
        etConfirmNewPassword = findViewById(R.id.etConfirmNewPassword)
        btnSendOTP = findViewById(R.id.btnOTP)
        btnVerifyOTP = findViewById(R.id.btnVerifyOTP)
        btnResetPassword = findViewById(R.id.btnResetPassword)
        tvResendOTP = findViewById(R.id.tvResendOTP)

        // Send OTP button listener
        btnSendOTP.setOnClickListener {
            val email = etForgotEmail.text.toString().trim()

            if (email.isNotEmpty() && isValidEmail(email)) {
                Log.d("EmailInput", "Email entered: $email")
                sendPasswordResetRequest(email)
            } else {
                Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show()
            }
        }

        // Resend OTP button listener
        tvResendOTP.setOnClickListener {
            val email = etForgotEmail.text.toString().trim()
            if (email.isNotEmpty() && isValidEmail(email) && isEmailVerified) {
                sendOTP(EmailRequest(email))
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
                verifyOTP(email, otp)
            } else {
                Toast.makeText(this, "Please enter the OTP", Toast.LENGTH_SHORT).show()
            }
        }

        // Reset Password button listener
        btnResetPassword.setOnClickListener {
            val newPassword = etNewPassword.text.toString()
            val confirmPassword = etConfirmNewPassword.text.toString()
            val email = etForgotEmail.text.toString().trim() // Get the email from the EditText

            // Check if passwords are not empty and match
            if (newPassword.isNotEmpty() && newPassword == confirmPassword) {
                resetPassword(email, newPassword, confirmPassword) // Pass email, new password, and confirm password
            } else {
                Toast.makeText(this, "Passwords do not match or are empty", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun sendPasswordResetRequest(email: String) {
        val forgotPasswordRequest = ForgotPasswordRequest(email)

        RetrofitClient.authService.checkEmail(forgotPasswordRequest).enqueue(object : Callback<ForgotPasswordResponse> {
            override fun onResponse(call: Call<ForgotPasswordResponse>, response: Response<ForgotPasswordResponse>) {
                if (response.isSuccessful) {
                    val emailResponse = response.body()
                    val jsonResponse = Gson().toJson(emailResponse)
                    Log.d("EmailCheckResponse", "JSON Response: $jsonResponse")

                    if (emailResponse?.email_exists == true) {
                        Toast.makeText(this@ForgotPassword, emailResponse.message, Toast.LENGTH_SHORT).show()
                        isEmailVerified = true
                        etOTP.visibility = View.VISIBLE
                        btnVerifyOTP.visibility = View.VISIBLE
                        tvResendOTP.visibility = View.VISIBLE

                        // Create EmailRequest from the email
                        val emailRequest = EmailRequest(email) // Create EmailRequest from the email
                        sendOTP(emailRequest) // Send OTP after verifying email exists
                    } else {
                        Toast.makeText(this@ForgotPassword, "Email not found in the system", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    Toast.makeText(this@ForgotPassword, "Error: ${response.message()}\n$errorBody", Toast.LENGTH_SHORT).show()
                    Log.d("Error", "Error: ${response.message()}\n$errorBody")
                }
            }

            override fun onFailure(call: Call<ForgotPasswordResponse>, t: Throwable) {
                Toast.makeText(this@ForgotPassword, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("EmailCheckFailure", "Error: ${t.message}", t)
            }
        })
    }

    private fun sendOTP(emailRequest: EmailRequest) {
        if (retryCount >= 3 && System.currentTimeMillis() - lastFailedAttemptTime < timeoutDurationMillis) {
            Toast.makeText(this, "Too many attempts. Please try again later.", Toast.LENGTH_SHORT).show()
            return
        }

        RetrofitClient.authService.sendOTP(emailRequest).enqueue(object : Callback<EmailResponse> {
            override fun onResponse(call: Call<EmailResponse>, response: Response<EmailResponse>) {
                if (response.isSuccessful) {
                    val jsonResponse = Gson().toJson(response.body())
                    Log.d("SendOTPResponse", "JSON Response: $jsonResponse")

                    retryCount = 0
                    lastFailedAttemptTime = 0
                    Toast.makeText(this@ForgotPassword, "OTP sent to your email", Toast.LENGTH_SHORT).show()
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    Toast.makeText(this@ForgotPassword, "Error: ${response.message()}\n$errorBody", Toast.LENGTH_SHORT).show()

                    retryCount++
                    lastFailedAttemptTime = System.currentTimeMillis()

                    if (retryCount < 3) {
                        startCooldown(emailRequest)
                    } else {
                        Toast.makeText(this@ForgotPassword, "Maximum attempts reached. Please wait 30 minutes.", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<EmailResponse>, t: Throwable) {
                val errorMessage = t.message ?: "Network error"
                Toast.makeText(this@ForgotPassword, "Error: $errorMessage", Toast.LENGTH_SHORT).show()

                retryCount++
                lastFailedAttemptTime = System.currentTimeMillis()

                if (retryCount < 3) {
                    startCooldown(emailRequest)
                } else {
                    Toast.makeText(this@ForgotPassword, "Maximum attempts reached. Please wait 30 minutes.", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun startCooldown(emailRequest: EmailRequest) {
        Toast.makeText(this@ForgotPassword, "Please wait for 1 minute before retrying...", Toast.LENGTH_SHORT).show()
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            retryCount = 0
            sendOTP(emailRequest)
        }, cooldownTimeMillis)
    }

    private fun verifyOTP(email: String, otp: Int) {
        val otpRequest = OTPRequest(email, otp)

        RetrofitClient.authService.verifyOTP(otpRequest).enqueue(object : Callback<OTPResponse> {
            override fun onResponse(call: Call<OTPResponse>, response: Response<OTPResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@ForgotPassword, "OTP verified successfully", Toast.LENGTH_SHORT).show()
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

    private fun resetPassword(email: String, newPassword: String, confirmPassword: String) {
        val resetPasswordRequest = ResetPasswordRequest(email, newPassword, confirmPassword)

        Log.d("ResetPasswordInput", "Email: $email, New Password: $newPassword, Confirm Password: $confirmPassword")

        RetrofitClient.authService.resetPassword(resetPasswordRequest).enqueue(object : Callback<ResetPasswordResponse> {
            override fun onResponse(call: Call<ResetPasswordResponse>, response: Response<ResetPasswordResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@ForgotPassword, "Password reset successfully", Toast.LENGTH_SHORT).show()
                    finish() // Close activity after successful reset
                } else {
                    Log.e("ResetPasswordError", "Error Code: ${response.code()}, Error Message: ${response.message()}")
                    Toast.makeText(this@ForgotPassword, "Failed to reset password: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResetPasswordResponse>, t: Throwable) {
                Toast.makeText(this@ForgotPassword, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("PasswordResetFailure", "Error: ${t.message}", t)
            }
        })
    }


    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
