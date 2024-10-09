package com.example.puttask.authentications

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.puttask.ForgotPassword
import com.example.puttask.MainActivity
import com.example.puttask.R
import com.example.puttask.api.DataManager
import com.example.puttask.api.RetrofitClient
import com.example.puttask.data.LoginRequest
import com.example.puttask.data.LoginResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LogIn : AppCompatActivity() {

    private lateinit var btnLog: Button
    private lateinit var othersSign: TextView
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var ivTogglePasswordVisibility: ImageView
    private lateinit var tvForgotPassword: TextView
    private lateinit var dataManager: DataManager
    private lateinit var rememberMeCheckBox: CheckBox
    private var isPasswordVisible: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_log_in)

        // Initialize DataManager
        dataManager = DataManager(this)

        btnLog = findViewById(R.id.btnLog)
        othersSign = findViewById(R.id.othersSign)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        ivTogglePasswordVisibility = findViewById(R.id.ivTogglePasswordVisibility)
        tvForgotPassword = findViewById(R.id.tvForgotPassword)
        rememberMeCheckBox = findViewById(R.id.rememberMeCheckBox)

        // Load saved email and password (if any)
        loadSavedCredentials()

        // Login Button
        btnLog.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            // Validation
            if (validateInputs(email, password)) {
                val loginRequest = LoginRequest(email, password, rememberMeCheckBox.isChecked)

                performLogin(loginRequest)
            }
        }

        othersSign.setOnClickListener {
            startActivity(Intent(this, SignUp::class.java))
        }

        tvForgotPassword.setOnClickListener {
            // Start Forgot Password Activity
            startActivity(Intent(this, ForgotPassword::class.java))
        }

        ivTogglePasswordVisibility.setOnClickListener {
            togglePasswordVisibility()
        }
    }

    private fun togglePasswordVisibility() {
        if (isPasswordVisible) {
            // Hide password
            etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            ivTogglePasswordVisibility.setImageResource(R.drawable.hide) // Change to hide icon
        } else {
            // Show password
            etPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            ivTogglePasswordVisibility.setImageResource(R.drawable.show) // Change to view icon
        }

        // Move cursor to the end of the password field
        etPassword.setSelection(etPassword.text.length)

        // Toggle the boolean value
        isPasswordVisible = !isPasswordVisible
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

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

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun loadSavedCredentials() {
        // Load saved email and password using DataManager
        val savedEmail = dataManager.getSavedEmail()
        val savedPassword = dataManager.getSavedPassword()
        etEmail.setText(savedEmail)
        etPassword.setText(savedPassword)

        // Check if credentials are saved and set checkbox accordingly
        rememberMeCheckBox.isChecked = savedEmail.isNotEmpty()
    }

    private fun performLogin(loginRequest: LoginRequest) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Make the login request
                val response = RetrofitClient.authService.login(loginRequest)

                withContext(Dispatchers.Main) {
                    // Check if the response is successful
                    if (response.isSuccessful) {
                        response.body()?.let { loginResponse ->
                            Log.d("LogIn", "Login successful, user: ${loginResponse.user.username}, email: ${loginResponse.user.email}")

                            // Save the token and user details
                            dataManager.saveToken(loginResponse.token)
                            Log.d("DataManager", "Token saved: ${dataManager.getToken()}") // Correctly access getToken

                            dataManager.saveUsername(loginResponse.user.username)
                            dataManager.saveEmail(loginResponse.user.email) // Ensure email is saved

                            // Remember Me feature
                            if (rememberMeCheckBox.isChecked) {
                                dataManager.saveCredentials(loginRequest.email, loginRequest.password)
                            } else {
                                dataManager.clearCredentials() // Clear saved credentials if not checked
                            }

                            showToast("Login Successful")
                            startActivity(Intent(this@LogIn, MainActivity::class.java))
                            finish()
                        } ?: run {
                            showToast("Login failed: Empty response body")
                        }
                    } else {
                        // Handle unsuccessful login
                        val errorMsg = response.errorBody()?.string() ?: "Unknown error"
                        showToast("Login failed: $errorMsg")
                        Log.e("LogIn", "Login failed: $errorMsg")
                    }
                }
            } catch (e: Exception) {
                // Handle error
                withContext(Dispatchers.Main) {
                    showToast("Login failed: ${e.message}")
                    Log.e("LogIn", "Login error: ${e.message}", e)
                }
            }
        }
    }
}
