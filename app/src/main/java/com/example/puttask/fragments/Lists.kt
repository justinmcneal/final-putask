package com.example.puttask.fragments

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.Fragment
import com.example.puttask.R

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
    }


}
