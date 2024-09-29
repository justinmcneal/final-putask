package com.example.puttask

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.puttask.api.LoginResponse
import com.example.puttask.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LogIn : AppCompatActivity() {

    private lateinit var btnLog: Button
    private lateinit var othersSign: TextView
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_log_in)

        // Set up window insets for edge-to-edge layout
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize UI components
        btnLog = findViewById(R.id.btnLog)
        othersSign = findViewById(R.id.othersSign)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)

        // Set up login button click listener
        btnLog.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (validateInputs(email, password)) {
                loginUser(email, password)
            }
        }

        // Redirect to sign-up screen
        othersSign.setOnClickListener {
            startActivity(Intent(this, SignUp::class.java))
        }
    }

    // Validate email and password inputs
    private fun validateInputs(email: String, password: String): Boolean {
        return when {
            email.isEmpty() -> {
                showToast("Please enter an email")
                false
            }

            !isValidEmail(email) -> {
                showToast("Please enter a valid email")
                false
            }

            password.isEmpty() -> {
                showToast("Please enter a password")
                false
            }

            else -> true
        }
    }

    // Check if email is valid
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // Show toast message
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    // Perform login using Retrofit
    private fun loginUser(email: String, password: String) {
        val authService = RetrofitClient.authService

        authService.login(email, password).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
              password























                if (response.isSuccessful) {
                    response.body()?.let {
                        val token = it.token

                        // Save token and login status in SharedPreferences
                        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
                        val editor = sharedPreferences.edit()
                        editor.putString("auth_token", token) // Save the token
                        editor.putBoolean(
                            "isLoggedIn",
                            true
                        ) // Save a boolean flag indicating the user is logged in
                        editor.apply()

                        showToast("Login Successful")
                        startActivity(Intent(this@LogIn, MainActivity::class.java))
                        finish() // Optionally close the login activity
                    }
                } else {
                    // Handle non-200 responses
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    showToast("Login failed: ${response.code()} ${response.message()}\n$errorBody")
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                showToast("Login failed: ${t.message}")
            }
        })
    }
}
