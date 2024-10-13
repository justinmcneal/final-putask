package com.example.puttask.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
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

    // This should now be an empty list that will be populated with tasks created in AddTask2
    private val taskList = mutableListOf<Task>()

    private val ADD_TASK_REQUEST_CODE = 100

    private lateinit var addTaskLauncher: ActivityResultLauncher<Intent>


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        listsRecyclerView = view.findViewById(R.id.listsrecyclerView)
        tvNoTasks = view.findViewById(R.id.tvNotask) // Initialize the TextView

        // Set up RecyclerView
        listsRecyclerView.layoutManager = LinearLayoutManager(context)
        listsAdapter = ListsAdapter(taskList, { task, isChecked ->
            // Update the task state here if needed
            val index = taskList.indexOf(task)
            if (index != -1) {
                taskList[index] = task.copy(isChecked = isChecked)
            }
        }, { task ->
            // Handle navigation when an item is clicked
            val intent = Intent(requireContext(), TaskViewRecycler::class.java)
            intent.putExtra("TASK_ID", task.id) // Pass the task ID or any necessary data
            startActivity(intent)
        })

        addTaskLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                val newTask = result.data?.getSerializableExtra("NEW_TASK") as? Task
                newTask?.let {
                    taskList.add(it) // Add the new task to the list
                    listsAdapter.notifyDataSetChanged() // Notify adapter about the new task
                    updateNoTasksMessage() // Update UI accordingly
                }
            }
        }

        // Set up the delete click listener
        listsAdapter.setOnDeleteClickListener { task ->
            showDeleteConfirmationDialog(task)
        }

        listsRecyclerView.adapter = listsAdapter

        updateNoTasksMessage() // Update visibility based on task list size

    }

    private fun startAddTaskActivity() {
        val intent = Intent(requireContext(), AddTask2::class.java)
        addTaskLauncher.launch(intent) // Use the new launcher
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

    // Method to update the task list (should be called from AddTask2)
    fun updateTaskList(newTasks: List<Task>) {
        taskList.clear()
        taskList.addAll(newTasks)
        listsAdapter.notifyDataSetChanged()
        updateNoTasksMessage() // Update visibility based on the new task list
    }
}
