package com.example.puttask.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
    private lateinit var dataManager: DataManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        passwordTextView = view.findViewById(R.id.tvChangePassword)

        // Initialize DataManager
        dataManager = DataManager(requireContext())

        return view
    }

    override fun onResume() {
        super.onResume()
        fetchUserInfo() // Fetch user information whenever the fragment is visible
    }

    private fun fetchUserInfo() {
        lifecycleScope.launch {
            try {
                // Make the network call to fetch user info using the token from DataManager
                val response = RetrofitClient.authService.getUser("Bearer ${dataManager.getToken()}")

                if (response.isSuccessful) {
                    // Get the user info from the response body
                    val userInfo = response.body()

                    // Check if the userInfo is not null before updating the UI
                    if (userInfo != null) {
                        // Update the UI with the user's password (masked with *)
                        passwordTextView.text = "*".repeat(userInfo.password.length)
                    } else {
                        // Handle case where userInfo is null
                        Toast.makeText(requireContext(), "User info is missing", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Handle unsuccessful response
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    Toast.makeText(requireContext(), "Error: ${response.message()}\n$errorBody", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Show an error message if fetching user info fails
                Toast.makeText(requireContext(), "Error fetching user info: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

}
