package com.example.puttask

import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract.Profile
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.puttask.api.DataManager
import com.example.puttask.api.RetrofitClient
import com.example.puttask.api.OTPRequest
import com.example.puttask.api.ResetPasswordRequest
import com.example.puttask.api.EmailRequest
import kotlinx.coroutines.launch

class ChangePassword : AppCompatActivity() {

    private lateinit var email: String
    private lateinit var otpEditText: EditText
    private lateinit var newPasswordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var verifyOtpButton: Button
    private lateinit var resetPasswordButton: Button
    private lateinit var sendOtpButton: Button
    private lateinit var tvResendOtp: TextView
    private lateinit var dataManager: DataManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.change_password)

        dataManager = DataManager(this)
        otpEditText = findViewById(R.id.etOTP)
        newPasswordEditText = findViewById(R.id.etNewPassword)
        confirmPasswordEditText = findViewById(R.id.etConfirmNewPassword)
        verifyOtpButton = findViewById(R.id.btnVerifyOTP)
        resetPasswordButton = findViewById(R.id.btnResetPassword)
        sendOtpButton = findViewById(R.id.btnSendOTP)
        tvResendOtp = findViewById(R.id.tvResendOTP)

        sendOtpButton.setOnClickListener {
            sendOtp()
        }
        verifyOtpButton.setOnClickListener {
            verifyOtp()
        }
        resetPasswordButton.setOnClickListener {
            resetPassword()
        }
        tvResendOtp.setOnClickListener{
            resendOtp()
        }
        fetchUserEmail()
    }

    private fun fetchUserEmail() {
        val token = dataManager.getAuthToken()

        if (token.isNullOrEmpty()) {
            Toast.makeText(this, "User is not authenticated.", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.getApiService(this@ChangePassword).getUser("Bearer $token")
                if (response.isSuccessful && response.body() != null) {
                    email = response.body()?.email ?: ""
                    Log.d("UserProfile", "Email fetched: $email")
                } else {
                    Log.e("FetchEmailError", "Error fetching email: ${response.errorBody()?.string()}")
                    Toast.makeText(this@ChangePassword, "Failed to fetch email.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("FetchEmailError", "Error: ${e.message}")
                Toast.makeText(this@ChangePassword, "Error fetching email.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun onOtpSent() {
        sendOtpButton.visibility = View.GONE // Hide Send OTP button
        verifyOtpButton.visibility = View.VISIBLE // Show Verify OTP button
        tvResendOtp.visibility = View.VISIBLE // Show Resend OTP TextView
    }

    // Resend OTP method
    private fun resendOtp() {
        lifecycleScope.launch {
            try {
                val emailRequest = EmailRequest(email)
                Log.d("ResendOtp", "Resending OTP to email: $email")
                val response = RetrofitClient.getApiService(this@ChangePassword).sendOTP(emailRequest)

                if (response.isSuccessful) {
                    val emailResponse = response.body()
                    Log.d("ResendOtp", "OTP resent successfully. Response: $emailResponse")
                    Toast.makeText(this@ChangePassword, "OTP resent successfully.", Toast.LENGTH_LONG).show()
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Unknown error occurred."
                    Log.e("ResendOtpError", "Error resending OTP: $errorMessage")
                    Toast.makeText(this@ChangePassword, "Failed to resend OTP: $errorMessage", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e("ResendOtpError", "Error: ${e.message}")
                Toast.makeText(this@ChangePassword, "Error resending OTP: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun sendOtp() {
        lifecycleScope.launch {
            try {
                val emailRequest = EmailRequest(email)
                Log.d("SendOtp", "Sending OTP to email: $email")
                val response = RetrofitClient.getApiService(this@ChangePassword).sendOTP(emailRequest)

                if (response.isSuccessful) {
                    val emailResponse = response.body()
                    onOtpSent()
                    Log.d("SendOtp", "OTP sent successfully. Response: $emailResponse")
                    val message = emailResponse?.message ?: "OTP sent successfully."
                    Toast.makeText(this@ChangePassword, message, Toast.LENGTH_LONG).show()
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Unknown error occurred."
                    Log.e("SendOtpError", "Error sending OTP: $errorMessage")
                    Toast.makeText(this@ChangePassword, "Failed to send OTP: $errorMessage", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e("SendOtpError", "Error: ${e.message}")
                Toast.makeText(this@ChangePassword, "Error sending OTP: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun verifyOtp() {
        val otp = otpEditText.text.toString()
        if (otp.isEmpty()) {
            Toast.makeText(this, "Please enter the OTP.", Toast.LENGTH_SHORT).show()
            return
        }
        // Convert OTP to Integer
        val otpInt = otp.toIntOrNull()
        if (otpInt == null) {
            Toast.makeText(this, "Invalid OTP format.", Toast.LENGTH_SHORT).show()
            return
        }
        lifecycleScope.launch {
            try {
                val otpRequest = OTPRequest(email, otpInt)
                val response = RetrofitClient.getApiService(this@ChangePassword).verifyOTP(otpRequest)

                if (response.isSuccessful) {
                    val otpResponse = response.body()
                    if (otpResponse != null && otpResponse.otp_valid) {
                        Toast.makeText(this@ChangePassword, "OTP verified successfully.", Toast.LENGTH_SHORT).show()

                        // Hide OTP EditText and Buttons
                        otpEditText.visibility = View.GONE
                        verifyOtpButton.visibility = View.GONE
                        tvResendOtp.visibility = View.GONE

                        // Show New Password Fields and Reset Button
                        newPasswordEditText.visibility = View.VISIBLE
                        confirmPasswordEditText.visibility = View.VISIBLE
                        resetPasswordButton.visibility = View.VISIBLE
                    } else {
                        Toast.makeText(this@ChangePassword, otpResponse?.message ?: "Failed to verify OTP.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("VerifyOtpError", "Error verifying OTP: ${response.errorBody()?.string()}")
                    Toast.makeText(this@ChangePassword, "Failed to verify OTP.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("VerifyOtpError", "Error: ${e.message}")
                Toast.makeText(this@ChangePassword, "Error verifying OTP.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun resetPassword() {
        val newPassword = newPasswordEditText.text.toString()
        val confirmPassword = confirmPasswordEditText.text.toString()

        if (newPassword != confirmPassword) {
            Toast.makeText(this, "New password and confirmation do not match.", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val resetPasswordRequest = ResetPasswordRequest(email, newPassword, confirmPassword)
                val response = RetrofitClient.getApiService(this@ChangePassword).resetPassword(resetPasswordRequest)

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null && responseBody.success) {
                        Toast.makeText(this@ChangePassword, "Password reset successfully.", Toast.LENGTH_SHORT).show()

                        // Navigate back to Profile activity
                        val intent = Intent(this@ChangePassword, com.example.puttask.fragments.Profile::class.java)
                        startActivity(intent)
                        finish() // Finish ChangePassword activity
                    } else {
                        Toast.makeText(this@ChangePassword, responseBody?.message ?: "Failed to reset password.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("ResetPasswordError", "Error resetting password: ${response.errorBody()?.string()}")
                    Toast.makeText(this@ChangePassword, "Failed to reset password.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("ResetPasswordError", "Error: ${e.message}")
                Toast.makeText(this@ChangePassword, "Error resetting password.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
