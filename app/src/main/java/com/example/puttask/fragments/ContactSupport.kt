package com.example.puttask.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.puttask.R
import com.example.puttask.api.RetrofitClient
import com.example.puttask.api.ContactRequest
import com.example.puttask.api.ContactResponse
import com.example.puttask.api.DataManager
import com.google.gson.Gson
import kotlinx.coroutines.launch

class ContactSupport : Fragment(R.layout.fragment_contact_support) {

    private lateinit var btnSubmit: Button
    private val contactApiService = RetrofitClient.apiService

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnSubmit = view.findViewById(R.id.btnSubmit)
        btnSubmit.setOnClickListener {
            val message = view.findViewById<EditText>(R.id.etMessage).text.toString()
            if (message.isNotEmpty()) {
                Log.d("ContactSupport", "Submit button clicked")
                submitContactForm(message)
            } else {
                Toast.makeText(context, "Please enter a message", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun submitContactForm(message: String) {
        lifecycleScope.launch {
            try {
                val token = DataManager(requireContext()).getAuthToken()
                Log.d("ContactSupport", "Retrieved Token: $token") // Log the token value
                if (token != null) {
                    val userResponse = contactApiService.getUser("Bearer $token")
                    if (userResponse.isSuccessful) {
                        val user = userResponse.body()
                        if (user != null) {
                            val username = user.username
                            val email = user.email
                            sendContactForm(message, username, email)
                        } else {
                            Toast.makeText(context, "Failed to retrieve user data", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "Failed to get user info: ${userResponse.message()}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "User not authenticated. Please log in.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendContactForm(message: String, username: String, email: String) {
        val contactReq = ContactRequest(message, username, email)
        lifecycleScope.launch {
            try {
                val response = contactApiService.sendContactForm(contactReq)
                if (response.isSuccessful) {
                    // Assuming response.body() is of type ContactResponse
                    val jsonResponse = response.body()
                    if (jsonResponse != null) {
                        Toast.makeText(context, "Message: ${jsonResponse.message}", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Received empty response", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Failed: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }


}
