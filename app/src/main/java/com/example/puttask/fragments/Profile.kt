package com.example.puttask.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.puttask.ForgotPassword
import com.example.puttask.R
import com.example.puttask.api.APIService
import com.example.puttask.api.DataManager
import com.example.puttask.api.RetrofitClient
import com.example.puttask.api.UpdateUsernameRequest
import kotlinx.coroutines.launch

class Profile : Fragment() {
    private lateinit var dataManager: DataManager
    private lateinit var apiService: APIService
    private lateinit var usernameTextView: TextView
    private lateinit var emailTextView: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        dataManager = DataManager(requireContext())
        apiService = RetrofitClient.getApiService(requireContext())
        usernameTextView = view.findViewById(R.id.etUsername)
        emailTextView = view.findViewById(R.id.tvEmail)
        view.findViewById<TextView>(R.id.tvChangePassword).setOnClickListener {
            startActivity(Intent(requireContext(), ForgotPassword::class.java))
        }
        view.findViewById<TextView>(R.id.btnSave).setOnClickListener {
            updateUsername(usernameTextView.text.toString())
        }
        loadUserProfile()
        return view
    }
    override fun onResume() {
        super.onResume()
        loadUserProfile()
    }
    private fun loadUserProfile() {
        lifecycleScope.launch {
            val token = "Bearer ${dataManager.getAuthToken()}"
            apiService.getUser(token).body()?.let { userInfo ->
                usernameTextView.text = userInfo.username
                emailTextView.text = userInfo.email
            }
        }
    }
    private fun updateUsername(newUsername: String) {
        if (newUsername.isNotBlank() && newUsername.length <= 50) {
            lifecycleScope.launch {
                val token = "Bearer ${dataManager.getAuthToken()}"
                apiService.updateUsername(token, UpdateUsernameRequest(username = newUsername)).takeIf { it.isSuccessful }?.let {
                    Toast.makeText(requireContext(), "Username changed.", Toast.LENGTH_SHORT).show()
                    requireContext().getSharedPreferences("user_prefs", AppCompatActivity.MODE_PRIVATE)
                        .edit().putString("username", newUsername).apply()
                    loadUserProfile()
                }
            }
        }
    }
}
