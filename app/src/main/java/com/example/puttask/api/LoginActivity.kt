package com.example.putask.api

import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response



class LoginActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)

        // Initialize EditText fields
        val usernameEditText = findViewById<EditText>(R.id.etEmail) // Casting to EditText
        val passwordEditText = findViewById<EditText>(R.id.etPassword) // Casting to EditText

        // Get the input values
        val username = usernameEditText.text.toString()
        val password = passwordEditText.text.toString()

        // Pass them to the login function
        loginUser(username, password)
    }

    private fun loginUser(username: String, password: String) {
        val authService = RetrofitClient.instance.create(AuthService::class.java)
        val loginRequest = LoginRequest(username, password)

        authService.login(loginRequest).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    loginResponse?.let {
                        Toast.makeText(this@LoginActivity, "Login Complete", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@LoginActivity, "Login failed", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Toast.makeText(this@LoginActivity, "Network Error", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
