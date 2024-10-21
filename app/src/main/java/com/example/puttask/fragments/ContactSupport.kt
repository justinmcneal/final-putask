package com.example.puttask.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.puttask.R
import com.example.puttask.api.APIService
import com.example.puttask.api.RetrofitClient
import com.example.puttask.api.ContactRequest
import com.example.puttask.api.DataManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ContactSupport : Fragment(R.layout.fragment_contact_support) {

    private lateinit var btnSubmit: Button
    private lateinit var etMessage: EditText
    private lateinit var contactApiService: APIService

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etMessage = view.findViewById(R.id.etMessage)
        btnSubmit = view.findViewById(R.id.btnSubmit)
        contactApiService = RetrofitClient.getApiService(requireContext())

        btnSubmit.setOnClickListener {
            val message = etMessage.text.toString().trim()
            if (message.isNotEmpty()) {
                submitContactForm(message)
            } else {
                Toast.makeText(requireContext(), "Please enter a message", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun submitContactForm(message: String) {
        val progressBar = requireView().findViewById<ProgressBar>(R.id.progressBar)
        progressBar.visibility = View.VISIBLE // Show the progress bar

        lifecycleScope.launch {
            val token = DataManager(requireContext()).getAuthToken()

            if (token != null) {
                val userResponse = contactApiService.getUser("Bearer $token")
                val user = userResponse.body()

                if (user != null) {
                    sendContactForm(message, user.username, user.email, progressBar)
                }
            }

            // Hide progress bar in sendContactForm
        }
    }

    private fun sendContactForm(message: String, username: String, email: String, progressBar: ProgressBar) {
        val contactReq = ContactRequest(message, username, email)

        lifecycleScope.launch {
            progressBar.visibility = View.VISIBLE

            val response = contactApiService.sendContactForm(contactReq)
            val responseBody = response.body()

            if (responseBody != null) {
                Toast.makeText(requireContext(), responseBody.message, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Empty response from server", Toast.LENGTH_SHORT).show()
            }

            delay(1000) // Show the progress bar for 1 second
            progressBar.visibility = View.GONE
        }
    }
}
