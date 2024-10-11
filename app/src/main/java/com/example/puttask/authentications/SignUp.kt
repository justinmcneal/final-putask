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
import com.example.puttask.MainActivity
import com.example.puttask.R
import com.example.puttask.api.RetrofitClient
import com.example.puttask.api.RegistrationRequest
import com.example.puttask.api.RegistrationResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignUp : AppCompatActivity() {

    private lateinit var btnSign: Button
    private lateinit var etUsername: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var ivTogglePasswordVisibility: ImageView
    private lateinit var ivToggleConfirmPasswordVisibility: ImageView
    private var isPasswordVisible: Boolean = false
    private var isConfirmPasswordVisible: Boolean = false


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



        //alternate if successful, ito ikekeep
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

        // Set click listener for the login redirect
        findViewById<TextView>(R.id.othersLog).setOnClickListener {
            startActivity(Intent(this, LogIn::class.java))
        }
        // Set an onClickListener on the eye icon forpassword
        ivTogglePasswordVisibility.setOnClickListener {
            if (isPasswordVisible) {
                // Hide password
                etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                ivTogglePasswordVisibility.setImageResource(R.drawable.hide)
            } else {
                // Show password
                etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                ivTogglePasswordVisibility.setImageResource(R.drawable.view)
            }

            // Move the cursor to the end of the text
            etPassword.setSelection(etPassword.text.length)

            // Toggle the boolean value
            isPasswordVisible = !isPasswordVisible
        }
        // Set an onClickListener on the eye icon forpassword
        ivToggleConfirmPasswordVisibility.setOnClickListener {
            if (isConfirmPasswordVisible) {
                // Hide password
                etConfirmPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                ivToggleConfirmPasswordVisibility.setImageResource(R.drawable.hide)
            } else {
                // Show password
                etConfirmPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                ivToggleConfirmPasswordVisibility.setImageResource(R.drawable.view)
            }

            // Move the cursor to the end of the text
            etConfirmPassword.setSelection(etPassword.text.length)

            // Toggle the boolean value
            isConfirmPasswordVisible = !isConfirmPasswordVisible
        }
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

        RetrofitClient.authService.register(registrationRequest).enqueue(object : Callback<RegistrationResponse> {
            override fun onResponse(call: Call<RegistrationResponse>, response: Response<RegistrationResponse>) {
                val message = if (response.isSuccessful) {
                    response.body()?.message ?: "Registration successful"
                } else {
                    response.errorBody()?.string() ?: "Unknown error"
                }

                showToast(message)
                if (response.isSuccessful) startActivity(Intent(this@SignUp, MainActivity::class.java))
            }
            override fun onFailure(call: Call<RegistrationResponse>, t: Throwable) {
                Log.e("SignUpError", "Network Error: ${t.message}", t)
                showToast("Network Error: ${t.localizedMessage}")
            }
        })
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
