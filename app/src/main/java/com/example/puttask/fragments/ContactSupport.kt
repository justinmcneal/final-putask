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

                    if (userResponse.isSuccessful && userResponse.body() != null) {
                        val user = userResponse.body()

                        if (user != null) {
                            // Extract username and email from the user object
                            val username = user.username
                            val email = user.email
                            sendContactForm(message, username, email) // Proceed to send the contact form
                        } else {
                            Toast.makeText(context, "Failed to get user data", Toast.LENGTH_SHORT).show()
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
        val contactReq = ContactRequest(message) // Create a ContactRequest with the user's message
        lifecycleScope.launch {
            try {
                // Make the API call to send the contact form
                val response = contactApiService.sendContactForm(contactReq)

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        val jsonString = Gson().toJson(responseBody)
                        Log.d("ResponseJSON", jsonString)

                        // Parse the JSON string into a ContactResponse object
                        val contactResponse = Gson().fromJson(jsonString, ContactResponse::class.java)

                        // Now you can access the message and data properties
                        val successMessage = contactResponse.message
                        val user = contactResponse.data // This should contain the UserInfo object

                        // Log the entire user object for debugging
                        Log.d("ContactSupportNigga1", "User")
                    }
                }

            }  catch (e: Exception) {
                // Catch any exceptions and log the error message
                Toast.makeText(context, "Error:Nigga6 ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("ErrorNigga7", "Error: ${e.message}")
        }
        }
    }
}

