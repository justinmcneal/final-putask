package com.example.puttask.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.puttask.ListsAdapter
import com.example.puttask.R
import com.example.puttask.TaskViewRecycler
import com.example.puttask.api.RetrofitClient
import com.example.puttask.api.Task
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response

class Lists : Fragment(R.layout.fragment_lists) {

    private lateinit var listsRecyclerView: RecyclerView
    private lateinit var listsAdapter: ListsAdapter
    private lateinit var tvNoTasks: TextView
    private val taskList = mutableListOf<Task>()
    private val completedTasks = mutableSetOf<Int>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listsRecyclerView = view.findViewById(R.id.listsrecyclerView)
        tvNoTasks = view.findViewById(R.id.tvNotask)

        listsRecyclerView.layoutManager = LinearLayoutManager(context)
        listsAdapter = ListsAdapter(taskList, { task, isChecked ->
            // Handle checkbox toggle
            val index = taskList.indexOf(task)
            if (index != -1) {
                if (isChecked) {
                    completedTasks.add(task.id)
                } else {
                    completedTasks.remove(task.id)
                }
                listsAdapter.notifyItemChanged(index)
            }
        }, { task ->
            // Handle item click to view task details
            val intent = Intent(requireContext(), TaskViewRecycler::class.java)
            intent.putExtra("TASK_ID", task.id) // Pass the task ID
            startActivity(intent)
        })

        listsAdapter.setOnDeleteClickListener { task ->
            showDeleteConfirmationDialog(task) // Show a confirmation dialog when deleting

        listsRecyclerView.adapter = listsAdapter
        fetchTasks()  // Call the function to fetch tasks
        updateNoTasksMessage()

        }


    }

    private fun fetchTasks() {
        CoroutineScope(Dispatchers.IO).launch {
            val response: Response<List<Task>> = RetrofitClient.getApiService(requireContext()).getAllTasks()
            if (response.isSuccessful && response.body() != null) {
                taskList.clear()
                taskList.addAll(response.body()!!)

                // Update the RecyclerView on the main thread
                requireActivity().runOnUiThread {
                    listsAdapter.notifyDataSetChanged()
                    updateNoTasksMessage()
                }
            } else {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Failed to load tasks", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun fetchTaskById(taskId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val response: Response<Task> = RetrofitClient.getApiService(requireContext()).getTaskById(taskId)
            if (response.isSuccessful && response.body() != null) {
                val task = response.body()!!

                // Use the task object as needed, e.g., navigate to a details page
                requireActivity().runOnUiThread {
                    showTaskDetails(task)
                }
            } else {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Failed to load task details", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showTaskDetails(task: Task) {
        val intent = Intent(requireContext(), TaskViewRecycler::class.java).apply {
            putExtra("TASK_ID", task.id)
            putExtra("TASK_NAME", task.task_name)
            putExtra("TASK_DESCRIPTION", task.task_description)
            putExtra("END_DATE", task.end_date)
            putExtra("END_TIME", task.end_time)
            // Pass other relevant task details if needed
        }
        startActivity(intent)
    }

    private fun showDeleteConfirmationDialog(task: Task) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Delete Task")
        builder.setMessage("Are you sure you want to delete this task?")
        builder.setPositiveButton("Delete") { _, _ -> deleteTask(task) }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
        builder.create().show() // Show the dialog
    }

    private fun updateNoTasksMessage() {
        if (taskList.isEmpty()) {
            tvNoTasks.visibility = View.VISIBLE
            listsRecyclerView.visibility = View.GONE
        } else {
            tvNoTasks.visibility = View.GONE
            listsRecyclerView.visibility = View.VISIBLE
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
        listsAdapter.deleteTask(task) // Delete the task using the adapter
        completedTasks.remove(task.id) // Remove from completed tasks if it was marked as completed
        updateNoTasksMessage() // Check if the "No tasks" message should be shown
    }
}
