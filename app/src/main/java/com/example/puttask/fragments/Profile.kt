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
    private lateinit var changePasswordTextView: TextView
    private lateinit var dataManager: DataManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        usernameTextView = view.findViewById(R.id.etUsername)
        emailTextView = view.findViewById(R.id.tvEmail)
        changePasswordTextView = view.findViewById(R.id.tvChangePassword)
        dataManager = DataManager(requireContext())

        changePasswordTextView.setOnClickListener {
            val intent = Intent(requireContext(), ForgotPassword::class.java)
            startActivity(intent)
        }
        return view
    }

    override fun onResume() {
        super.onResume()
        fetchUserInfo()
    }

    private fun fetchUserInfo() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getUser("Bearer ${dataManager.getAuthToken()}")
                if (response.isSuccessful && response.body() != null) {
                    val userInfo: UserInfo = response.body()!!
                    usernameTextView.text = userInfo.username
                    emailTextView.text = userInfo.email
                    changePasswordTextView.text = userInfo.password // Display the actual password
                } else {
                    showError("Error fetching user info")
                }
            } catch (e: Exception) {
                e.printStackTrace()
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
