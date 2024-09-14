package com.example.puttask

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.puttask.api.RegistrationRequest
import com.example.puttask.api.RegistrationResponse
import com.example.puttask.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignUp : AppCompatActivity() {

    private lateinit var btnSign: Button
    private lateinit var etUsername: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        btnSign = findViewById(R.id.btnSign)
        etUsername = findViewById(R.id.etUsername)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)

        btnSign.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            if (validateInputs(username, email, password, confirmPassword)) {
                registerUser(username, email, password)
            }
        }

        findViewById<TextView>(R.id.othersLog).setOnClickListener {
            startActivity(Intent(this, LogIn::class.java))
        }
    }

    private fun validateInputs(username: String, email: String, password: String, confirmPassword: String): Boolean {
        return when {
            username.isEmpty() -> {
                showToast("Please enter a username")
                false
            }
            email.isEmpty() -> {
                showToast("Please enter an email")
                false
            }
            !isValidEmail(email) -> {
                showToast("Please enter a valid email")
                false
            }
            password.isEmpty() || password.length < 8 -> {
                showToast("Password must be at least 8 characters")
                false
            }
            confirmPassword.isEmpty() || password != confirmPassword -> {
                showToast("Passwords do not match")
                false
            }
            else -> true
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun registerUser(username: String, email: String, password: String) {
        val registrationRequest = RegistrationRequest(username = username, email= email, password = password)
        RetrofitClient.authService.register(registrationRequest).enqueue(object : Callback<RegistrationResponse> {
            override fun onResponse(call: Call<RegistrationResponse>, response: Response<RegistrationResponse>) {
                if (response.isSuccessful) {
                    val message = response.body()?.message ?: "Registration successful"
                    showToast(message)
                    startActivity(Intent(this@SignUp, MainActivity::class.java))
                } else {
                    showToast("Registration failed")
                }
            }

            override fun onFailure(call: Call<RegistrationResponse>, t: Throwable) {
                // Log detailed error information
                Log.e("SignUpError", "Network Error: ${t.message}", t)

                // Show a user-friendly message
                showToast("Network Error: ${t.localizedMessage}")
            }

        })
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
