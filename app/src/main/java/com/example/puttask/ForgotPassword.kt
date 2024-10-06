package com.example.puttask.authentications

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.puttask.R
import com.example.puttask.api.RetrofitClient
import com.example.puttask.data.EmailRequest
import com.example.puttask.data.EmailResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ForgotPassword : AppCompatActivity() {

    private lateinit var etForgotEmail: EditText
    private lateinit var btnResetPassword: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        etForgotEmail = findViewById(R.id.etForgotEmail)
        btnResetPassword = findViewById(R.id.btnOTP)

        btnResetPassword.setOnClickListener {
            val email = etForgotEmail.text.toString().trim()

            if (email.isNotEmpty() && isValidEmail(email)) {
                // Call API to check if email exists and send reset instructions
                sendPasswordResetRequest(email)
            } else {
                Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show()
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

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
