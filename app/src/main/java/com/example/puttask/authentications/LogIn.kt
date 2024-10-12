package com.example.puttask.authentications

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.puttask.ForgotPassword
import com.example.puttask.MainActivity
import com.example.puttask.R
import com.example.puttask.api.DataManager
import com.example.puttask.api.RetrofitClient
import com.example.puttask.api.LoginRequest
import kotlinx.coroutines.launch
import retrofit2.HttpException

class LogIn : AppCompatActivity() {

    private lateinit var btnLog: Button
    private lateinit var othersSign: TextView
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var ivTogglePasswordVisibility: ImageView
    private lateinit var tvForgotPassword: TextView
    private lateinit var dataManager: DataManager
    private var isPasswordVisible: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_log_in)

        // Initialize DataManager
        dataManager = DataManager(this)

        // Initialize views
        btnLog = findViewById(R.id.btnLog)
        othersSign = findViewById(R.id.othersSign)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        ivTogglePasswordVisibility = findViewById(R.id.ivTogglePasswordVisibility)
        tvForgotPassword = findViewById(R.id.tvForgotPassword)

        // Login Button
        btnLog.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            // Validation and Retrofit
            if (validateInputs(email, password)) {
                performLogin(email, password)
            }
        }

        // Sign-up redirection
        othersSign.setOnClickListener {
            startActivity(Intent(this, SignUp::class.java))
        }

        // Forgot password redirection
        tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPassword::class.java))
        }

        // Toggle password visibility
        ivTogglePasswordVisibility.setOnClickListener {
            togglePasswordVisibility()
        }
    }

    private fun performLogin(email: String, password: String) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.login(LoginRequest(email, password))
                if (response.isSuccessful) {
                    response.body()?.let {
                        val token = it.token // Retrieve the token
                        if (token.isNotEmpty()) { // Ensure the token is not empty
                            dataManager.saveAuthToken(token) // Save token in SharedPreferences
                            Log.d("LogIn", "Token saved: $token") // Log the token after saving
                            showToast("Login Successful")
                            redirectToMainActivity()
                        } else {
                            showToast("Login successful but no token found.")
                        }
                    } ?: run {
                        showToast("Login response is null.")
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    Log.e("LogIn", "Login failed: ${response.code()} ${response.message()}\n$errorBody")
                    showToast("Login failed: ${response.message()}")
                }
            } catch (e: HttpException) {
                Log.e("LogIn", "Login failed: ${e.message}", e)
                showToast("Login failed: ${e.message}")
            } catch (e: Throwable) {
                Log.e("LogIn", "Login failed: ${e.message}", e)
                showToast("Login failed: ${e.message}")
            }
        }
    }

    private fun redirectToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // Close the login screen
    }

    private fun togglePasswordVisibility() {
        if (isPasswordVisible) {
            etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            ivTogglePasswordVisibility.setImageResource(R.drawable.hide) // Change to hide icon
        } else {
            etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            ivTogglePasswordVisibility.setImageResource(R.drawable.view) // Change to view icon
        }
        isPasswordVisible = !isPasswordVisible
        etPassword.setSelection(etPassword.text.length) // Move cursor to end
    }

    private fun validateInputs(email: String, password: String): Boolean {
        return when {
            email.isEmpty() -> showError("Please enter an email")
            !isValidEmail(email) -> showError("Please enter a valid email")
            password.isEmpty() -> showError("Please enter a password")
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

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
