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
import com.example.puttask.api.RegistrationRequest
import com.example.puttask.api.DataManager
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
            val username = etUsername.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

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

    private fun validateInputs(username: String, email: String, password: String, confirmPassword: String): Boolean {
        val maxUsernameLength = 50 // Set the maximum username length

        return when {
            username.isEmpty() -> showError("Please enter a username")
            username.length > maxUsernameLength -> showError("Username cannot exceed $maxUsernameLength characters")
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

    private fun registerUser(username: String, email: String, password: String, confirmPassword: String) {
        val registrationRequest = RegistrationRequest(username, email, password, confirmPassword)

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.getApiService(this@SignUp).register(registrationRequest)
                if (response.isSuccessful) {
                    val registrationResponse = response.body()
                    val message = registrationResponse?.message ?: "Registration successful"

                    // Save the authentication token using DataManager
                    registrationResponse?.token?.let {
                        DataManager(this@SignUp).saveAuthToken(it)

                        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
                        val editor = sharedPreferences.edit()

                        // Clear old data
                        editor.clear()

                        // Assuming `registrationResponse` contains a `user` object with the new username
                        val username = registrationResponse?.user?.username
                        editor.putString("username", username)  // Save new username
                        editor.putString("token", it)  // Save new token
                        editor.apply()  // Apply changes
                    }

                    showToast(message)
                    startActivity(Intent(this@SignUp, MainActivity::class.java))
                    finish()
                } else {
                    val errorBody = response.errorBody()?.string()
                    showToast(errorBody ?: "Registration failed")
                }
            } catch (e: HttpException) {
                Log.e("SignUpError", "HTTP Error: ${e.message()}", e)
                showToast("HTTP Error: ${e.message()}")
            } catch (e: Throwable) {
                Log.e("SignUpError", "Network Error: ${e.message}", e)
                showToast("Network Error: ${e.localizedMessage}")
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
