package com.example.puttask

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.puttask.api.RetrofitClient
import com.example.puttask.api.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Lists : Fragment(R.layout.fragment_lists) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Find the Spinner by ID
        val spinner: Spinner = view.findViewById(R.id.spinner)

        // List of items for the Spinner
        val items = listOf("All Items", "Today", "Personal", "School", "Work", "Social")

        // Create an ArrayAdapter using the custom item layout
        val adapter = ArrayAdapter(requireContext(), R.layout.spinner_dropdown_item, items)

        // Apply the adapter to the Spinner
        spinner.adapter = adapter

        // Set a custom background for the Spinner dropdown
        spinner.setPopupBackgroundResource(R.drawable.spinnerbg)

        // Find the ImageView for sorting
        val imageSort: ImageView = view.findViewById(R.id.imageSort)

        imageSort.setOnClickListener {
            // Create a PopupMenu
            val popupSort = PopupMenu(requireContext(), imageSort)

            // Inflate the PopupMenu from the menu resource
            popupSort.menuInflater.inflate(R.menu.popup_sort, popupSort.menu)

            // Set click listener for the PopupMenu items
            popupSort.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.sort_newest -> {
                        // Handle "Newest" sort option
                        true
                    }
                    R.id.sort_oldest -> {
                        // Handle "Oldest" sort option
                        true
                    }
                    else -> false
                }
            }
            popupSort.show()
        }

        // Find the TextView by ID
        val tvUsername: TextView = view.findViewById(R.id.tvUsername)

        // Call the API to get user data
        getUserData(tvUsername)
    }

    private fun getUserData(tvUsername: TextView) {
        // Assuming you have a method like 'getUser' in AuthService
        RetrofitClient.authService.getUser().enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    // Get the user object from the response
                    val user = response.body()
                    // Set the username in the TextView
                    user?.let {
                        tvUsername.text = "Hi ${it.username}!"
                    }
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                // Handle failure
                tvUsername.text = "Error loading username"
            }
        })
    }
}
