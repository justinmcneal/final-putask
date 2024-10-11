package com.example.puttask.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.puttask.R
import com.example.puttask.api.RetrofitClient
import com.example.puttask.api.ContactRequest
import com.example.puttask.api.ContactResponse
import com.example.puttask.api.User
import com.google.gson.Gson
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ContactSupport : Fragment(R.layout.fragment_contact_support) {

    private lateinit var btnSubmit: Button
    private val contactApiService = RetrofitClient.contactService

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize the submit button
        btnSubmit = view.findViewById(R.id.btnSubmit)

        // Set the click listener for the submit button
        btnSubmit.setOnClickListener {
            // Get input data from the message EditText field
            val message = view.findViewById<EditText>(R.id.etMessage).text.toString()

            // Validate input
            if (message.isNotEmpty()) {
                // Add a log to ensure the function is called
                Log.d("ContactSupport", "Submit button clicked")
                submitContactForm(message)
            } else {
                Toast.makeText(context, "Please enter a message", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun submitContactForm(message: String) {
        contactApiService.getUserDetails().enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    val user = response.body()
                    if (user != null) {
                        // Successfully retrieved user, now send the contact form
                        sendContactForm(message, user.username, user.email)
                    } else {
                        // Handle null user case
                        Toast.makeText(context, "Failed to retrieve user data", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Handle non-successful API response
                    Toast.makeText(context, "Failed to get user info: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                // Handle failure to communicate with the API
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun sendContactForm(message: String, username: String, email: String) {
        val contactReq = ContactRequest(message, username, email)

        contactApiService.sendContactForm(contactReq).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val jsonString = response.body()?.string()
                    try {
                        val jsonResponse = Gson().fromJson(jsonString, ContactResponse::class.java)
                        Toast.makeText(context, "Message: ${jsonResponse.message}", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Response was not JSON: $jsonString", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Failed: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

}
