package com.example.puttask.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.puttask.ListsAdapter
import com.example.puttask.R
import com.example.puttask.TaskViewRecycler
import com.example.puttask.api.Task

class Lists : Fragment(R.layout.fragment_lists) {

    private lateinit var listsRecyclerView: RecyclerView
    private lateinit var listsAdapter: ListsAdapter
    private lateinit var tvNoTasks: TextView // Declare the TextView for "No tasks created"

    // Task list that will be populated dynamically
    private val taskList = mutableListOf<Task>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        listsRecyclerView = view.findViewById(R.id.listsrecyclerView)
        tvNoTasks = view.findViewById(R.id.tvNotask) // Initialize the TextView

        // Set up RecyclerView
        listsRecyclerView.layoutManager = LinearLayoutManager(context)
        listsAdapter = ListsAdapter(
            taskList = taskList,
            onTaskCheckedChange = { task, isChecked ->
                // Update the task's checked state
                val index = taskList.indexOf(task)
                if (index != -1) {
                    taskList[index] = task.copy(isChecked = isChecked)
                }
            },
            onItemClick = { task ->
                // Handle item click and navigate to TaskViewRecycler
                val intent = Intent(requireContext(), TaskViewRecycler::class.java)
                intent.putExtra("TASK_ID", task.id)
                startActivity(intent)
            },
            onDeleteTask = { taskId ->
                // Handle task deletion by task ID
                val taskToDelete = taskList.find { it.id == taskId }
                taskToDelete?.let { showDeleteConfirmationDialog(it) }
            }
        )

        listsRecyclerView.adapter = listsAdapter
        updateNoTasksMessage() // Update visibility based on task list size
    }

    private fun showDeleteConfirmationDialog(task: Task) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Delete Task")
        builder.setMessage("Are you sure you want to delete this task?")

        builder.setPositiveButton("Delete") { _, _ ->
            deleteTask(task)
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        builder.create().show()
    }

    private fun deleteTask(task: Task) {
        taskList.remove(task)
        listsAdapter.notifyDataSetChanged()
        updateNoTasksMessage() // Update the message after deletion
    }

    private fun updateNoTasksMessage() {
        if (taskList.isEmpty()) {
            tvNoTasks.visibility = View.VISIBLE // Show the "No tasks created" message
            listsRecyclerView.visibility = View.GONE // Hide the RecyclerView
        } else {
            tvNoTasks.visibility = View.GONE // Hide the message
            listsRecyclerView.visibility = View.VISIBLE // Show the RecyclerView
        }
    }

    // Method to update the task list (should be called from AddTask2 or other sources)
    fun updateTaskList(newTasks: List<Task>) {
        taskList.clear()
        taskList.addAll(newTasks)
        listsAdapter.notifyDataSetChanged()
        updateNoTasksMessage() // Update visibility based on the new task list
    }
}
