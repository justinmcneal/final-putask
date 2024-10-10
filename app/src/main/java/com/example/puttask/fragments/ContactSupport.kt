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
import com.example.puttask.api.DataManager
import com.example.puttask.api.RetrofitClient
import com.example.puttask.data.ContactRequest
import com.example.puttask.data.UserInfo // Make sure you import the UserInfo data class
import kotlinx.coroutines.launch

class ContactSupport : Fragment(R.layout.fragment_contact_support) {

    private lateinit var btnSubmit: Button
    private lateinit var dataManager: DataManager
    private val contactApiService = RetrofitClient.contactService // Ensure you have contactService set up

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnSubmit = view.findViewById(R.id.btnSubmit)
        dataManager = DataManager(requireContext())

        btnSubmit.setOnClickListener {
            val message = view.findViewById<EditText>(R.id.etMessage).text.toString()

            if (message.isNotEmpty()) {
                Log.d("ContactSupport", "Submit button clicked")
                lifecycleScope.launch {
                    submitContactForm(message)
                }
            } else {
                Toast.makeText(context, "Please enter a message", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun submitContactForm(message: String) {
        try {
            // Retrieve the token
            val token = "Bearer ${dataManager.getToken()}"

            Log.d("ContactSupport", "Retrieving user details $token") // Debug log

            // Fetch user details
            val userResponse = contactApiService.getUserDetails(token)
            if (userResponse.isSuccessful) {
                val user: UserInfo? = userResponse.body() // Change this to match your UserInfo data class
                user?.let {
                    // Proceed to send the contact form with fetched user details
                    sendContactForm(message, it.username, it.email, token)
                } ?: run {
                    Toast.makeText(context, "User details not found.", Toast.LENGTH_SHORT).show()
                }
            } else {
                val errorMessage = userResponse.errorBody()?.string() ?: "Unknown error"
                Log.e("ContactSupport", "Error fetching user details: ${userResponse.code()} ${userResponse.message()}")
                Toast.makeText(context, "Error fetching user: $errorMessage", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error fetching user: ${e.message}", Toast.LENGTH_SHORT).show()
            Log.e("ContactSupport", "Error fetching user: ${e.message}") // Error log
        }
    }

    private suspend fun sendContactForm(
        message: String,
        username: String,
        email: String,
        token: String
    ) {
        try {
            // Create a contact request with message, username, and email
            val contactReq = ContactRequest(message, username, email) // Ensure the order matches your data class
            val response = contactApiService.sendContactForm("Bearer $token", contactReq)

            if (response.isSuccessful) {
                Toast.makeText(context, "Message sent successfully!", Toast.LENGTH_SHORT).show()
                Log.d("ContactSupport", "Message sent successfully") // Debug log
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Unknown error"
                Log.e("ContactSupport", "Error sending message: ${response.code()} ${response.message()}")
                Toast.makeText(context, "Error: $errorMessage", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("ContactSupport", "Error sending message: ${e.message}")
            Toast.makeText(context, "Error sending message: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
