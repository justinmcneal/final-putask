package com.example.puttask.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.puttask.R
import com.example.puttask.api.DataManager
import com.example.puttask.api.RetrofitClient
import com.example.puttask.data.UserInfo
import kotlinx.coroutines.launch

class Profile : Fragment() {

    private lateinit var passwordTextView: TextView
    private lateinit var loadingIndicator: ProgressBar
    private lateinit var dataManager: DataManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        passwordTextView = view.findViewById(R.id.tvChangePassword)
        loadingIndicator = view.findViewById(R.id.loadingIndicator)

        // Initialize DataManager
        dataManager = DataManager(requireContext())

        // Fetch user information
        fetchUserInfo()

        return view
    }

    private fun fetchUserInfo() {
        loadingIndicator.visibility = View.VISIBLE // Show loading indicator

        lifecycleScope.launch {
            try {
                // Call the suspend function with the token from DataManager
                val userInfo: UserInfo = RetrofitClient.authService.getUser("Bearer ${dataManager.getToken()}")

                // Set the TextView to display asterisks based on password length
                passwordTextView.text = "*".repeat(userInfo.password.length)
            } catch (e: Exception) {
                // Handle errors (e.g., show a toast or log the error)
                e.printStackTrace()
                Toast.makeText(requireContext(), "Error fetching user info", Toast.LENGTH_SHORT).show()
            } finally {
                loadingIndicator.visibility = View.GONE // Hide loading indicator
            }
        }
    }
}
