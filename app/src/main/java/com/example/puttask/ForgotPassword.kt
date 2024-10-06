package com.example.puttask

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

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
                // Handle password reset logic (e.g., call to API)
                Toast.makeText(this, "Password reset instructions sent to $email", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
