package com.example.puttask.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.puttask.ListsAdapter
import com.example.puttask.R
import com.example.puttask.TaskViewRecycler
import com.example.puttask.api.Task
import com.example.puttask.databinding.FragmentListsBinding

class Lists : Fragment(R.layout.fragment_lists) {

    // View Binding
    private var _binding: FragmentListsBinding? = null
    private val binding get() = _binding!!

    private lateinit var listsAdapter: ListsAdapter
    private val taskList = mutableListOf<Task>()
    private val completedTasks = mutableSetOf<String>() // Assuming task ID is an Integer

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView() // Initialize RecyclerView
        updateNoTasksMessage() // Update UI message if no tasks are available
    }

    private fun setupRecyclerView() {
        binding.listsrecyclerView.layoutManager = LinearLayoutManager(context)
        listsAdapter = ListsAdapter(taskList, { task, isChecked ->
            handleCheckboxToggle(task, isChecked)
        }, { task ->
            handleTaskClick(task)
        })

        listsAdapter.setOnDeleteClickListener { task ->
            showDeleteConfirmationDialog(task) // Show a confirmation dialog when deleting
        }

        binding.listsrecyclerView.adapter = listsAdapter // Set adapter to RecyclerView
    }

    private fun handleCheckboxToggle(task: Task, isChecked: Boolean) {
        val index = taskList.indexOf(task)
        if (index != -1) {
            if (isChecked) {
                completedTasks.add(task.id) // Mark task as completed
            } else {
                completedTasks.remove(task.id) // Unmark task
            }
            listsAdapter.notifyItemChanged(index) // Notify the adapter to refresh this item
        }
    }

    private fun handleTaskClick(task: Task) {
        val intent = Intent(requireContext(), TaskViewRecycler::class.java)
        intent.putExtra("TASK_ID", task.id) // Pass the task ID to the next activity
        startActivity(intent)
    }

    private fun showDeleteConfirmationDialog(task: Task) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Delete Task")
            .setMessage("Are you sure you want to delete this task?")
            .setPositiveButton("Delete") { _, _ -> deleteTask(task) }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .create()
            .show() // Show the dialog
    }

    private fun updateNoTasksMessage() {
        if (taskList.isEmpty()) {
            binding.tvNotask.visibility = View.VISIBLE
            binding.listsrecyclerView.visibility = View.GONE
        } else {
            binding.tvNotask.visibility = View.GONE
            binding.listsrecyclerView.visibility = View.VISIBLE
        }
    }

    fun onTaskCreated(task: Task) {
        taskList.add(task)
        listsAdapter.notifyItemInserted(taskList.size - 1) // Notify adapter about the new task
        updateNoTasksMessage() // Update the visibility message
    }

    fun updateTask(updatedTask: Task) {
        val index = taskList.indexOfFirst { it.id == updatedTask.id }
        if (index != -1) {
            taskList[index] = updatedTask // Update the task in the list
            listsAdapter.notifyItemChanged(index) // Notify adapter to refresh this specific item
        }
    }

    private fun deleteTask(task: Task) {
        val index = taskList.indexOf(task)
        if (index != -1) {
            taskList.removeAt(index) // Remove the task from the list
            listsAdapter.notifyItemRemoved(index) // Notify adapter about the removal
            completedTasks.remove(task.id) // Remove from completed tasks if it was marked as completed
            updateNoTasksMessage() // Check if the "No tasks" message should be shown
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Clean up binding reference to prevent memory leaks
    }
}
