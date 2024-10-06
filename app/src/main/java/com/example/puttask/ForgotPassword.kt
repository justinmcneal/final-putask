package com.example.puttask

import android.os.Bundle
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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ForgotPassword : AppCompatActivity() {

    private lateinit var etForgotEmail: EditText
    private lateinit var etOTP: EditText
    private lateinit var btnSendOTP: Button
    private lateinit var btnVerifyOTP: Button
    private lateinit var tvResendOTP: TextView

    private var isEmailVerified = false // Track if the email has been verified

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        // Initialize views
        etForgotEmail = findViewById(R.id.etForgotEmail)
        etOTP = findViewById(R.id.etOTP)
        btnSendOTP = findViewById(R.id.btnOTP)
        btnVerifyOTP = findViewById(R.id.btnVerifyOTP)
        tvResendOTP = findViewById(R.id.tvResendOTP)

        // Send OTP button listener
        btnSendOTP.setOnClickListener {
            val email = etForgotEmail.text.toString().trim()

            if (email.isNotEmpty() && isValidEmail(email)) {
                // Call API to check if email exists and send reset instructions
                sendPasswordResetRequest(email)
            } else {
                Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show()
            }
        }

        // Resend OTP button listener
        tvResendOTP.setOnClickListener {
            val email = etForgotEmail.text.toString().trim()
            if (email.isNotEmpty() && isValidEmail(email) && isEmailVerified) {
                sendPasswordResetRequest(email) // Resend OTP
                Toast.makeText(this, "OTP Resent", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Please enter a valid email and verify first", Toast.LENGTH_SHORT).show()
            }
        }

        // Verify OTP button listener
        btnVerifyOTP.setOnClickListener {
            val otp = etOTP.text.toString().trim()

            if (otp.isNotEmpty()) {
                // Call function to verify OTP
                verifyOTP(otp)
            } else {
                Toast.makeText(this, "Please enter the OTP", Toast.LENGTH_SHORT).show()
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

                    if (emailResponse?.email_exists == true) {
                        Toast.makeText(this@ForgotPassword, emailResponse.message, Toast.LENGTH_SHORT).show()

                        // Set email as verified
                        isEmailVerified = true

                        // Show OTP input and buttons once email is verified
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
                }
            }

            override fun onFailure(call: Call<EmailResponse>, t: Throwable) {
                Toast.makeText(this@ForgotPassword, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun sendOTP(emailRequest: EmailRequest) {
        RetrofitClient.authService.sendOTP(emailRequest).enqueue(object : Callback<EmailResponse> {
            override fun onResponse(call: Call<EmailResponse>, response: Response<EmailResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@ForgotPassword, "OTP sent to your email", Toast.LENGTH_SHORT).show()
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    Toast.makeText(this@ForgotPassword, "Error: ${response.message()}\n$errorBody", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<EmailResponse>, t: Throwable) {
                Toast.makeText(this@ForgotPassword, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun verifyOTP(otp: String) {
        // Create OTPRequest with the entered OTP
        val otpRequest = OTPRequest(otp)

        // Call the API to verify the OTP
        RetrofitClient.authService.verifyOTP(otpRequest).enqueue(object : Callback<OTPResponse> {
            override fun onResponse(call: Call<OTPResponse>, response: Response<OTPResponse>) {
                if (response.isSuccessful) {
                    val otpResponse = response.body()

                    if (otpResponse?.otp_valid == true) {
                        Toast.makeText(this@ForgotPassword, "OTP Verified. Proceed to reset password.", Toast.LENGTH_SHORT).show()
                        // Logic to handle successful OTP verification, such as opening reset password screen
                    } else {
                        Toast.makeText(this@ForgotPassword, "Invalid OTP", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    Toast.makeText(this@ForgotPassword, "Error: ${response.message()}\n$errorBody", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<OTPResponse>, t: Throwable) {
                Toast.makeText(this@ForgotPassword, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
