package com.example.puttask

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.puttask.api.ContactApiService
import com.example.puttask.api.ContactFormData
import com.example.puttask.api.ContactRequest
import com.example.puttask.api.ContactResponse
import com.example.puttask.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import okhttp3.ResponseBody

class ContactSupport : Fragment(R.layout.fragment_contact_support) {

    private lateinit var btnSubmit: Button
    private val contactApiService: ContactApiService by lazy {
        RetrofitClient.ContactApiService
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

// Initialize the submit button
        btnSubmit = view.findViewById(R.id.btnSubmit)

        // Set the click listener for the submit button
        btnSubmit.setOnClickListener {
            // Get input data from EditText fields
            val name = view.findViewById<EditText>(R.id.etUsername).text.toString()
            val email = view.findViewById<EditText>(R.id.etEmail).text.toString()
            val message = view.findViewById<EditText>(R.id.etMessage).text.toString()

            // Call the sendContactForm function with the input data
            sendContactForm(name, email, message)
        }
    }

    private fun sendContactForm(name: String, email: String, message: String) {
        val contactReq = ContactRequest(name, email, message)

        contactApiService.sendContactForm(contactReq).enqueue(object : Callback<ContactResponse> {
            override fun onResponse(call: Call<ContactResponse>, response: Response<ContactResponse>) {
                if (response.isSuccessful) {
                    // Handle success response
                    context?.let {
                        Toast.makeText(it, "Message sent successfully!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Handle error response
                    context?.let {
                        Toast.makeText(it, "Failed to send message: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<ContactResponse>, t: Throwable) {
                // Handle failure
                context?.let {
                    Toast.makeText(it, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
}
