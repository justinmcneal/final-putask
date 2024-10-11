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
import com.example.puttask.api.UserInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
                // Call the suspend function with the token from DataManager
                val userInfo: UserInfo = RetrofitClient.authService.getUser("Bearer ${dataManager.getToken()}")

                // Update the UI after fetching the user info
                passwordTextView.text = "*".repeat(userInfo.password.length)
            } catch (e: Exception) {
                e.printStackTrace()
                // Handle errors and show the toast message
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Error fetching user info", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
