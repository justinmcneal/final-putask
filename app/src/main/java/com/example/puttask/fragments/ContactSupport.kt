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
import com.example.puttask.data.ContactRequest
import kotlinx.coroutines.launch

class ContactSupport : Fragment(R.layout.fragment_contact_support) {

    private lateinit var btnSubmit: Button
    private val contactApiService = RetrofitClient.contactService
    private val authToken = "Bearer your_auth_token" // Replace with actual token management

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnSubmit = view.findViewById(R.id.btnSubmit)

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
            // Fetch user details using coroutine
            val userResponse = contactApiService.getUserDetails(authToken)

            if (userResponse.isSuccessful) {
                val user = userResponse.body()
                user?.let {
                    // Send the contact form after fetching user details
                    sendContactForm(message, it.username, it.email)
                } ?: run {
                    Toast.makeText(context, "Failed to retrieve user data", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Failed to get user info: ${userResponse.message()}", Toast.LENGTH_SHORT).show()
            }

        } catch (e: Exception) {
            Toast.makeText(context, "Error fetching user: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private suspend fun sendContactForm(message: String, username: String, email: String) {
        try {
            val contactReq = ContactRequest(message, username, email)
            val response = contactApiService.sendContactForm(authToken, contactReq)

            if (response.isSuccessful) {
                val responseBody = response.body()?.string()
                responseBody?.let {
                    Toast.makeText(context, "Message sent successfully!", Toast.LENGTH_SHORT).show()
                } ?: run {
                    Toast.makeText(context, "No response body", Toast.LENGTH_SHORT).show()
                }
            } else {
                val errorMessage = response.errorBody()?.string()
                Toast.makeText(context, "Error: $errorMessage", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error sending message: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
