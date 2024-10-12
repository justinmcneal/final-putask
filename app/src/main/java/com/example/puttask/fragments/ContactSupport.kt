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
import retrofit2.HttpException

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
                showToast("Please enter a message")
            }
        }
    }

    private fun submitContactForm(message: String) {
        lifecycleScope.launch {
            try {
                val token = DataManager(requireContext()).getAuthToken()
                Log.d("ContactSupport", "Retrieved Token: $token")

                if (token != null) {
                    // Make sure the getUser API call is defined correctly in your API service
                    val userResponse = contactApiService.getUser("Bearer $token")

                    if (userResponse.isSuccessful && userResponse.body() != null) {
                        val user = userResponse.body()
                        if (user != null) {
                            val username = user.username
                            val email = user.email
                            sendContactForm(message) // Pass username and email if required
                        } else {
                            showToast("Failed to get user data")
                        }
                    } else {
                        showToast("Failed to get user info: ${userResponse.message()}")
                    }
                } else {
                    showToast("User not authenticated. Please log in.")
                }
            } catch (e: Exception) {
                Log.e("ContactSupport", "Error in submitContactForm: ${e.message}", e)
                showToast("Error: ${e.message}")
            }
        }
    }

    private fun sendContactForm(message: String) {
        val contactReq = ContactRequest(message) // Include username and email if required
        lifecycleScope.launch {
            try {
                // Make the API call to send the contact form
                val response = contactApiService.sendContactForm(contactReq)

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        Log.d("ResponseJSON", Gson().toJson(responseBody))
                        val successMessage = responseBody.message // Assuming message is a field in the response
                        Log.d("ContactSupport", "Contact form submitted successfully: $successMessage")
                        showToast(successMessage)
                    } else {
                        Log.e("ContactSupport", "Empty response body")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("ContactSupport", "Error submitting contact form: ${response.message()} - $errorBody")
                    showToast("Error: ${response.message()}")
                }
            } catch (e: HttpException) {
                Log.e("ContactSupport", "HTTP Exception: ${e.message()}")
                showToast("HTTP Error: ${e.message()}")
            } catch (e: Exception) {
                Log.e("ContactSupport", "Error in sendContactForm: ${e.message}", e)
                showToast("Error: ${e.message}")
            }
        }
    }

    // Helper method for displaying Toast messages
    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
