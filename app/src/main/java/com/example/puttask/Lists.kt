package com.example.puttask

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Spinner
import androidx.fragment.app.Fragment

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
            val popupSort = PopupMenu(requireContext(), imageSort) // Use requireContext()

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
    }
}
