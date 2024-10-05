package com.example.puttask.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.puttask.R
import com.example.puttask.api.RetrofitClient
import com.example.puttask.data.ContactRequest
import com.example.puttask.data.ContactResponse
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
                // Call the sendContactForm function with the message
                sendContactForm(message)
            } else {
                Toast.makeText(context, "Please enter a message", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Function to send the message to the API
    private fun sendContactForm(message: String) {
        val contactReq = ContactRequest(message) // Only passing the message
        

        contactApiService.sendContactForm(contactReq).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val jsonString = response.body()?.string() // Get the raw response
                    try {
                        val jsonResponse = Gson().fromJson(jsonString, ContactResponse::class.java)
                        Toast.makeText(context, "Message: ${jsonResponse.message}", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        // Handle parsing errors if it's not JSON
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
