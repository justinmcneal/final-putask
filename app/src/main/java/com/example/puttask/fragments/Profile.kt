package com.example.puttask.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.puttask.ForgotPassword
import com.example.puttask.R
import com.example.puttask.api.DataManager
import com.example.puttask.api.RetrofitClient
import com.example.puttask.api.UserInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Profile : Fragment() {

    private lateinit var usernameTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var passwordTextView: TextView
    private lateinit var changePasswordTextView: TextView
    private lateinit var dataManager: DataManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        usernameTextView = view.findViewById(R.id.tvUsername)
        emailTextView = view.findViewById(R.id.tvEmail)
        passwordTextView = view.findViewById(R.id.tvChangePassword)
        changePasswordTextView = view.findViewById(R.id.tvChangePassword)

        // Initialize DataManager
        dataManager = DataManager(requireContext())

        // Set up click listener for the Change Password TextView
        changePasswordTextView.setOnClickListener {
            // Start the ForgotPassword activity
            val intent = Intent(requireContext(), ForgotPassword::class.java)
            startActivity(intent)
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        fetchUserInfo() // Fetch user information whenever the fragment is visible
    }

    private fun fetchUserInfo() {
        lifecycleScope.launch {
            try {
                // Call the suspend function with the token from DataManager
                val response = RetrofitClient.apiService.getUser("Bearer ${dataManager.getAuthToken()}")

                // Check for a successful response
                if (response.isSuccessful && response.body() != null) {
                    val userInfo: UserInfo = response.body()!!

                    // Update the UI after fetching the user info
                    usernameTextView.text = userInfo.username
                    emailTextView.text = userInfo.email
                    passwordTextView.text = "*".repeat(userInfo.password.length) // Hide the password
                } else {
                    showError("Error fetching user info")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Handle errors and show the toast message
                showError("Error fetching user info")
            }
        }
    }

    private suspend fun showError(message: String) {
        withContext(Dispatchers.Main) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }
}
