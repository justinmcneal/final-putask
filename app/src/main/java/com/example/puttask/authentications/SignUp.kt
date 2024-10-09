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
import com.example.puttask.api.DataManager
import com.example.puttask.api.RetrofitClient
import com.example.puttask.data.RegistrationRequest
import kotlinx.coroutines.launch
import retrofit2.HttpException

class SignUp : AppCompatActivity() {

    private lateinit var btnSign: Button
    private lateinit var etUsername: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var ivTogglePasswordVisibility: ImageView
    private lateinit var ivToggleConfirmPasswordVisibility: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        btnSign = findViewById(R.id.btnSign)
        etUsername = findViewById(R.id.etUsername)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        ivTogglePasswordVisibility = findViewById(R.id.ivTogglePasswordVisibility)
        ivToggleConfirmPasswordVisibility = findViewById(R.id.ivToggleConfirmPasswordVisibility)

        btnSign.setOnClickListener {
            val (username, email, password, confirmPassword) = listOf(
                etUsername.text.toString().trim(),
                etEmail.text.toString().trim(),
                etPassword.text.toString().trim(),
                etConfirmPassword.text.toString().trim()
            )

            if (validateInputs(username, email, password, confirmPassword)) {
                registerUser(username, email, password, confirmPassword)
            }
        }

        findViewById<TextView>(R.id.othersLog).setOnClickListener {
            startActivity(Intent(this, LogIn::class.java))
        }

        ivTogglePasswordVisibility.setOnClickListener {
            togglePasswordVisibility(etPassword, ivTogglePasswordVisibility)
        }

        ivToggleConfirmPasswordVisibility.setOnClickListener {
            togglePasswordVisibility(etConfirmPassword, ivToggleConfirmPasswordVisibility)
        }
    }

    private fun togglePasswordVisibility(editText: EditText, imageView: ImageView) {
        if (editText.inputType == InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD) {
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            imageView.setImageResource(R.drawable.show)
        } else {
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            imageView.setImageResource(R.drawable.hide)
        }
        editText.setSelection(editText.text.length)
    }

    private fun validateInputs(
        username: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        return when {
            username.isEmpty() -> showError("Please enter a username")
            email.isEmpty() -> showError("Please enter an email")
            !isValidEmail(email) -> showError("Please enter a valid email")
            password.length < 8 -> showError("Password must be at least 8 characters")
            password != confirmPassword -> showError("Passwords do not match")
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

    private fun registerUser(
        username: String,
        email: String,
        password: String,
        confirmPassword: String
    ) {
        val registrationRequest = RegistrationRequest(username, email, password, confirmPassword)

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.authService.register(registrationRequest)

                Log.d("SignUp", "Response Code: ${response.code()}")
                Log.d("SignUp", "Response Body: ${response.body()}")

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        // Save the username in DataManager
                        val dataManager = DataManager(this@SignUp)
                        dataManager.saveUsername(username) // Save the username here
                        Log.d("SignUp", "Saved username: $username") // Log for debugging

                        showToast(responseBody.message)
                        startActivity(Intent(this@SignUp, MainActivity::class.java))
                    } else {
                        showToast("Registration successful, but no message received.")
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    Log.e("SignUpError", "Error Response: $errorBody")
                    showToast("Registration failed: $errorBody")
                }
            } catch (e: HttpException) {
                Log.e("SignUpError", "HttpException: ${e.message}", e)
                showToast("Registration failed: ${e.message}")
            } catch (e: Exception) {
                Log.e("SignUpError", "Network Error: ${e.message}", e)
                showToast("Network Error: ${e.localizedMessage}")
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
