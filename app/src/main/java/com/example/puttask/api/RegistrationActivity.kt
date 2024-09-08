package com.example.putask.api

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class  RegistrationActivity : AppCompatActivity() {

    private lateinit var usernameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_signin)

        // Initialize EditText fields
        usernameEditText = findViewById(R.id.etUsername)
        emailEditText = findViewById(R.id.etEmail)
        passwordEditText = findViewById(R.id.etPassword)
        confirmPasswordEditText = findViewById(R.id.etConfirmPassword)

        // Set up registration action (trigger this from a button click)
        val username = usernameEditText.text.toString()
        val email = emailEditText.text.toString()
        val password = passwordEditText.text.toString()
        val confirmPassword = confirmPasswordEditText.text.toString()

        // Perform registration
        registerUser(username, email, password, confirmPassword)
    }

    private fun registerUser(username: String, email: String, password: String, confirmPassword: String) {
        val authService = RetrofitClient.instance.create(AuthService::class.java)
        val registrationRequest = RegistrationRequest(username, email, password, confirmPassword)

        authService.register(registrationRequest).enqueue(object : Callback<RegistrationResponse> {
            override fun onResponse(call: Call<RegistrationResponse>, response: Response<RegistrationResponse>) {
                if (response.isSuccessful) {
                    val registrationResponse = response.body()
                    registrationResponse?.let {
                        Toast.makeText(this@RegistrationActivity, it.message, Toast.LENGTH_SHORT).show()
                        // Store the token if needed
                        val token = it.token
                        // Store the token for future authenticated requests
                    }
                } else {
                    Toast.makeText(this@RegistrationActivity, "Registration failed", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<RegistrationResponse>, t: Throwable) {
                Toast.makeText(this@RegistrationActivity, "Network Error", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
