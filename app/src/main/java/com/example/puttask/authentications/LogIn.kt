package com.example.puttask.authentications

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.puttask.ForgotPassword
import com.example.puttask.MainActivity
import com.example.puttask.R
import com.example.puttask.api.RetrofitClient
import com.example.puttask.api.LoginRequest
import com.example.puttask.api.DataManager
import com.example.puttask.api.LoginResponse
import kotlinx.coroutines.launch

class LogIn : AppCompatActivity() {

    private lateinit var btnLogin: Button
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var ivTogglePasswordVisibility: ImageView
    private lateinit var forgotPassTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)
        btnLogin = findViewById(R.id.btnLog)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        ivTogglePasswordVisibility = findViewById(R.id.ivTogglePasswordVisibility)
        forgotPassTextView = findViewById(R.id.tvForgotPassword)
        btnLogin.setOnClickListener { handleLogin() }
        forgotPassTextView.setOnClickListener { navigateToForgotPassword() }
        findViewById<TextView>(R.id.othersSign).setOnClickListener { navigateToSignUp() }
        ivTogglePasswordVisibility.setOnClickListener { togglePasswordVisibility() }
    }

    private fun handleLogin() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (validateInputs(email, password)) {
            loginUser(email, password)
        }
    }

    private fun navigateToForgotPassword() {
        val intent = Intent(this, ForgotPassword::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToSignUp() {
        startActivity(Intent(this, SignUp::class.java))
    }

    private fun togglePasswordVisibility() {
        val currentInputType = etPassword.inputType
        if (currentInputType == InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
            etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            ivTogglePasswordVisibility.setImageResource(R.drawable.hide)
        } else {
            etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            ivTogglePasswordVisibility.setImageResource(R.drawable.view)
        }
        etPassword.setSelection(etPassword.text.length)
    }

    private fun validateInputs(email: String, password: String): Boolean {
        return when {
            email.isEmpty() -> showError("Please enter your email")
            !isValidEmail(email) -> showError("Please enter a valid email")
            password.isEmpty() -> showError("Please enter your password")
            else -> true
        }
    }

    private fun showError(message: String): Boolean {
        showToast(message)
        return false
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun loginUser(email: String, password: String) {
        val loginRequest = LoginRequest(email, password)

        lifecycleScope.launch {
            val response = RetrofitClient.getApiService(this@LogIn).login(loginRequest)
            if (response.isSuccessful) {
                handleLoginSuccess(response.body())
            } else {
                showToast("Wrong Email or Password!")
            }
        }
    }

    private fun handleLoginSuccess(loginResponse: LoginResponse?) {
        val message = loginResponse?.message ?: "Login successful"

        // Save the authentication token using DataManager
        loginResponse?.token?.let {
            DataManager(this).saveAuthToken(it)

            // Save username and token in SharedPreferences
            val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
            sharedPreferences.edit().apply {
                clear() // Clear old data
                putString("username", loginResponse.user?.username) // Save username
                putString("token", it) // Save token
                apply() // Apply changes
            }
        }

        showToast(message)
        startActivity(Intent(this, MainActivity::class.java))
        finish() // Close the LogIn screen
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
