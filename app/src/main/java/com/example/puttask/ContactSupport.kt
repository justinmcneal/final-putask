package com.example.puttask

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.puttask.api.ContactRequest
import com.example.puttask.api.ContactResponse
import com.example.puttask.api.RetrofitClient
import com.google.gson.Gson
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ContactSupport : Fragment(R.layout.fragment_contact_support) {

    private lateinit var btnSubmit: Button
    private val contactApiService = RetrofitClient.contactApiService // Fixed variable name

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Check if the user is logged in
        val sharedPreferences = requireContext().getSharedPreferences("user_prefs", MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)

        if (!isLoggedIn) {
            // Redirect to login page if not logged in
            val intent = Intent(requireContext(), LogIn::class.java)
            startActivity(intent)
            requireActivity().finish() // End current activity
        }

        // Initialize the submit button
        btnSubmit = view.findViewById(R.id.btnSubmit)

        // Set the click listener for the submit button
        btnSubmit.setOnClickListener {
            // Get input data from EditText fields
            val username = view.findViewById<EditText>(R.id.etUsername).text.toString()
            val email = view.findViewById<EditText>(R.id.etEmail).text.toString()
            val message = view.findViewById<EditText>(R.id.etMessage).text.toString()

            // Call the sendContactForm function with the input data
            sendContactForm(username, email, message)
        }
    }

    private fun sendContactForm(username: String, email: String, message: String) {
        val contactReq = ContactRequest(username, email, message)

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
