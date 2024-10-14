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
import com.example.puttask.api.APIService
import com.example.puttask.api.RetrofitClient
import com.example.puttask.api.ContactRequest
import com.example.puttask.api.DataManager
import kotlinx.coroutines.launch

class ContactSupport : Fragment(R.layout.fragment_contact_support) {

    private lateinit var btnSubmit: Button
    private lateinit var etMessage: EditText
    private lateinit var contactApiService: APIService

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etMessage = view.findViewById(R.id.etMessage)
        btnSubmit = view.findViewById(R.id.btnSubmit)

        // Initialize the API service
        contactApiService = RetrofitClient.getApiService(requireContext())

        btnSubmit.setOnClickListener {
            val message = etMessage.text.toString().trim()
            if (message.isNotEmpty()) {
                Log.d("ContactSupport", "Submit button clicked")
                submitContactForm(message)
            } else {
                Toast.makeText(requireContext(), "Please enter a message", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun submitContactForm(message: String) {
        lifecycleScope.launch {
            try {
                val token = DataManager(requireContext()).getAuthToken()
                Log.d("ContactSupport", "Retrieved Token: $token")

                if (token != null) {
                    val userResponse = contactApiService.getUser("Bearer $token")

                    if (userResponse.isSuccessful) {
                        val user = userResponse.body()
                        if (user != null) {
                            val username = user.username
                            val email = user.email
                            sendContactForm(message, username, email)
                        } else {
                            Toast.makeText(requireContext(), "Failed to get user data", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(requireContext(), "Failed to get user info: ${userResponse.message()}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "User not authenticated. Please log in.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("ContactSupport", "Exception: ${e.message}", e)
            }
        }
    }

    private fun sendContactForm(message: String, username: String, email: String) {
        val contactReq = ContactRequest(message, username, email) // Make sure to include username and email in the request
        lifecycleScope.launch {
            try {
                val response = contactApiService.sendContactForm(contactReq)

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        val successMessage = responseBody.message
                        Toast.makeText(requireContext(), successMessage, Toast.LENGTH_SHORT).show()
                        Log.d("ContactSupport", "Contact submitted successfully: $successMessage")
                    } else {
                        Toast.makeText(requireContext(), "Empty response from server", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to send contact form: ${response.message()}", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("ContactSupport", "Error sending contact form: ${e.message}", e)
            }
        }
    }
}
