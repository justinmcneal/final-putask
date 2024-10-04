package com.example.puttask.fragments

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.puttask.ListsAdapter
import com.example.puttask.R
import com.example.puttask.data.Task

class Lists : Fragment(R.layout.fragment_lists) {

    private lateinit var listsrecyclerView: RecyclerView
    private lateinit var listsAdapter: ListsAdapter

    private val taskList = mutableListOf(
        Task(1, "Title 1", "Description 1", "10:00 AM", "11:00 AM", null, false),
        Task(2, "Title 2", "Description 2", "12:00 PM", "1:00 PM", null, true),
        Task(3, "Title 3", "Description 3", "3:00 PM", "4:00 PM", null, false),
        Task(4, "Title 4", "Description 4", "10:00 AM", "11:00 AM", null, false),
        Task(5, "Title 5", "Description 5", "10:00 AM", "11:00 AM", null, false),
        Task(6, "Title 6", "Description 6", "10:00 AM", "11:00 AM", null, false)
    )

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

        // Set up the RecyclerView
        listsrecyclerView = view.findViewById(R.id.listsrecyclerView)
        listsrecyclerView.layoutManager = LinearLayoutManager(context)

        // Initialize the adapter and set it to RecyclerView
        listsAdapter = ListsAdapter(taskList) { task, isChecked ->
            // Update the task state here if needed
            val index = taskList.indexOf(task)
            if (index != -1) {
                taskList[index] = task.copy(isChecked = isChecked)
            }
        }
        listsrecyclerView.adapter = listsAdapter
    }
}
