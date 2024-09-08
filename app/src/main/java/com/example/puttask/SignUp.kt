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
import com.example.puttask.api.RegistrationRequest
import com.example.puttask.api.RegistrationResponse
import com.example.puttask.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignUp : AppCompatActivity() {

    private lateinit var btnSign: Button
    private lateinit var othersLog: TextView
    private lateinit var etUsername: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btnSign = findViewById(R.id.btnSign)
        othersLog = findViewById(R.id.othersLog)
        etUsername = findViewById(R.id.etUsername)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)

        // Handle registration on sign-up button click
        btnSign.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            if (validateInputs(username, email, password, confirmPassword)) {
                registerUser(username, email, password)
            }
        }

        // Redirect to log in
        othersLog.setOnClickListener {
            val intent = Intent(this, LogIn::class.java)
            startActivity(intent)
        }
    }

    // Input validation logic
    private fun validateInputs(username: String, email: String, password: String, confirmPassword: String): Boolean {
        return when {
            username.isEmpty() -> {
                Toast.makeText(this, "Please enter a username", Toast.LENGTH_SHORT).show()
                false
            }
            email.isEmpty() -> {
                Toast.makeText(this, "Please enter an email", Toast.LENGTH_SHORT).show()
                false
            }
            !isValidEmail(email) -> {
                Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show()
                false
            }
            password.isEmpty() || password.length < 8 -> {
                Toast.makeText(this, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show()
                false
            }
            confirmPassword.isEmpty() || password != confirmPassword -> {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                false
            }
            else -> true
        }
    }

    // Email validation method
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // Register user using Retrofit
    private fun registerUser(username: String, email: String, password: String) {
        val authService = RetrofitClient.instance.create(AuthService::class.java)
        val registrationRequest = RegistrationRequest(username, email, password)

        authService.register(registrationRequest).enqueue(object : Callback<RegistrationResponse> {
            override fun onResponse(call: Call<RegistrationResponse>, response: Response<RegistrationResponse>) {
                if (response.isSuccessful) {
                    val registrationResponse = response.body()
                    registrationResponse?.let {
                        Toast.makeText(this@SignUp, it.message, Toast.LENGTH_SHORT).show()
                        val token = it.token
                        // Optionally store the token if needed for future requests
                    }
                    // Proceed to the main activity after successful registration
                    val intent = Intent(this@SignUp, MainActivity::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(this@SignUp, "Registration failed", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<RegistrationResponse>, t: Throwable) {
                Toast.makeText(this@SignUp, "Network Error", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
