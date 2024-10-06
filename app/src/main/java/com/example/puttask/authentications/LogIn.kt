package com.example.puttask.authentications

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
import com.example.puttask.MainActivity
import com.example.puttask.R
import com.example.puttask.api.DataManager
import com.example.puttask.api.RetrofitClient
import com.example.puttask.data.LoginRequest
import com.example.puttask.data.LoginResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LogIn : AppCompatActivity() {

    private lateinit var btnLog: Button
    private lateinit var othersSign: TextView
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var tvForgotPassword: TextView
    private lateinit var dataManager: DataManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_log_in)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize DataManager
        dataManager = DataManager(this)

        btnLog = findViewById(R.id.btnLog)
        othersSign = findViewById(R.id.othersSign)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        tvForgotPassword = findViewById(R.id.tvForgotPassword)

        // Login Button
        btnLog.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            // Validation and Retrofit
            if (validateInputs(email, password)) {
                val authService = RetrofitClient.authService
                val loginRequest = LoginRequest(email, password)

                authService.login(loginRequest).enqueue(object : Callback<LoginResponse> {
                    override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                        if (response.isSuccessful) {
                            response.body()?.let {
                                val token = it.token

                                // Save token using DataManager
                                dataManager.saveToken(token) // Save token in SharedPreferences

                                showToast("Login Successful")
                                startActivity(Intent(this@LogIn, MainActivity::class.java))
                                finish()
                            }
                        } else {
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

        othersSign.setOnClickListener {
            startActivity(Intent(this, SignUp::class.java))
        }

        tvForgotPassword.setOnClickListener {
            // Start Forgot Password Activity
            startActivity(Intent(this, ForgotPassword::class.java))
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun validateInputs(email: String, password: String): Boolean {
        return when {
            email.isEmpty() -> "Please enter an email"
            !isValidEmail(email) -> "Please enter a valid email"
            password.isEmpty() -> "Please enter a password"
            else -> null
        }?.apply {
            showToast(this)
        } == null
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
