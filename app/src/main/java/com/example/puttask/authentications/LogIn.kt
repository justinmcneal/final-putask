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
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.puttask.MainActivity
import com.example.puttask.R
import com.example.puttask.api.RetrofitClient
import com.example.puttask.api.LoginRequest
import com.example.puttask.api.DataManager
import kotlinx.coroutines.launch
import retrofit2.HttpException

class LogIn : AppCompatActivity() {

    private lateinit var btnLogin: Button
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var ivTogglePasswordVisibility: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)

        // Initialize views
        btnLogin = findViewById(R.id.btnLog)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        ivTogglePasswordVisibility = findViewById(R.id.ivTogglePasswordVisibility)

        // Set click listener for the login button
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (validateInputs(email, password)) {
                loginUser(email, password)
            }
        }

        // Set click listener for signup link
        findViewById<TextView>(R.id.othersSign).setOnClickListener {
            startActivity(Intent(this, SignUp::class.java))
        }

        // Toggle password visibility
        ivTogglePasswordVisibility.setOnClickListener {
            togglePasswordVisibility(etPassword, ivTogglePasswordVisibility)
        }
    }

    private fun togglePasswordVisibility(editText: EditText, imageView: ImageView) {
        if (editText.inputType == InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
            // Hide password
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            imageView.setImageResource(R.drawable.hide)
        } else {
            // Show password
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            imageView.setImageResource(R.drawable.view)
        }
        // Move cursor to the end of the text
        editText.setSelection(editText.text.length)
    }

    private fun validateInputs(email: String, password: String): Boolean {
        return when {
            email.isEmpty() -> showError("Please enter your email")
            !isValidEmail(email) -> showError("Please enter a valid email")
            password.isEmpty() -> showError("Please enter your password")
            password.length < 8 -> showError("Password must be at least 8 characters")
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
            try {
                val response = RetrofitClient.getApiService(this@LogIn).login(loginRequest)
                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    val message = loginResponse?.message ?: "Login successful"

                    // Save the authentication token using DataManager
                    loginResponse?.token?.let {
                        DataManager(this@LogIn).saveAuthToken(it)
                    }

                    showToast(message)
                    startActivity(Intent(this@LogIn, MainActivity::class.java))
                    finish() // Close the LogIn screen
                } else {
                    val errorBody = response.errorBody()?.string()
                    showToast(errorBody ?: "Login failed")
                }
            } catch (e: HttpException) {
                Log.e("LogInError", "HTTP Error: ${e.message()}", e)
                showToast("HTTP Error: ${e.message()}")
            } catch (e: Throwable) {
                Log.e("LogInError", "Network Error: ${e.message}", e)
                showToast("Network Error: ${e.localizedMessage}")
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
