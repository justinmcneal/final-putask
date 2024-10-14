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
import com.example.puttask.api.Task

class Lists : Fragment(R.layout.fragment_lists) {

    private lateinit var listsRecyclerView: RecyclerView
    private lateinit var listsAdapter: ListsAdapter
    private lateinit var tvNoTasks: TextView
    private val taskList = mutableListOf<Task>()
    private val completedTasks = mutableSetOf<Int>() // Set to keep track of completed task IDs

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listsRecyclerView = view.findViewById(R.id.listsrecyclerView)
        tvNoTasks = view.findViewById(R.id.tvNotask)

        listsRecyclerView.layoutManager = LinearLayoutManager(context)
        listsAdapter = ListsAdapter(taskList, { task, isChecked ->
            val index = taskList.indexOf(task)
            if (index != -1) {
                // Update the completion status
                if (isChecked) {
                    completedTasks.add(task.id) // Mark as completed
                } else {
                    completedTasks.remove(task.id) // Unmark as completed
                }
                listsAdapter.notifyItemChanged(index) // Notify adapter about item change
            }
        }, { task ->
            val intent = Intent(requireContext(), TaskViewRecycler::class.java)
            intent.putExtra("TASK_ID", task.id)
            startActivity(intent)
        })

        listsAdapter.setOnDeleteClickListener { task ->
            showDeleteConfirmationDialog(task)
        }

        listsRecyclerView.adapter = listsAdapter
        updateNoTasksMessage()
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

    private fun updateNoTasksMessage() {
        if (taskList.isEmpty()) {
            tvNoTasks.visibility = View.VISIBLE
            listsRecyclerView.visibility = View.GONE
        } else {
            tvNoTasks.visibility = View.GONE
            listsRecyclerView.visibility = View.VISIBLE
        }
    }

    fun updateTaskList(newTasks: List<Task>) {
        taskList.clear()
        taskList.addAll(newTasks)
        listsAdapter.notifyDataSetChanged()
        updateNoTasksMessage()
    }

    fun onTaskCreated(task: Task) {
        taskList.add(task)
        listsAdapter.notifyItemInserted(taskList.size - 1)
        updateNoTasksMessage()
    }

    private fun deleteTask(task: Task) {
        val index = taskList.indexOf(task)
        if (index != -1) {
            taskList.removeAt(index)
            completedTasks.remove(task.id) // Remove from completed tasks if deleted
            listsAdapter.notifyItemRemoved(index)
            updateNoTasksMessage()
            Toast.makeText(context, "Task deleted successfully.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Task not found.", Toast.LENGTH_SHORT).show()
        }
    }

    // Method to check if a task is completed
    fun isTaskCompleted(task: Task): Boolean {
        return completedTasks.contains(task.id)
    }
}
