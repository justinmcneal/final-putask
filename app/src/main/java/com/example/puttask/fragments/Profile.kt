import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.puttask.R
import com.example.puttask.api.AuthService
import com.example.puttask.data.UserInfo
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class Profile : Fragment() {

    private lateinit var passwordTextView: TextView
    private lateinit var authService: AuthService

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        passwordTextView = view.findViewById(R.id.passwordTextView)

        // Initialize Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl("https://yourapiurl.com/") // Replace with your API base URL
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        authService = retrofit.create(AuthService::class.java)

        // Fetch user information
        fetchUserInfo()

        return view
    }

    private fun fetchUserInfo() {
        lifecycleScope.launch {
            try {
                // Directly call the suspend function
                val userInfo: UserInfo = authService.getUser() // Fetch user info

                // Set the TextView to display asterisks based on password length
                passwordTextView.text = "*".repeat(userInfo.password.length)
            } catch (e: Exception) {
                // Handle errors (e.g., show a toast or log the error)
                e.printStackTrace()
            }
        }
    }
}
