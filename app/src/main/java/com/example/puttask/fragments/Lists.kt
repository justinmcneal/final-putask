package com.example.puttask.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.puttask.ListsAdapter
import com.example.puttask.R
import com.example.puttask.TaskViewRecycler
import com.example.puttask.api.RetrofitClient
import com.example.puttask.api.Task
import com.example.puttask.databinding.FragmentListsBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response

class Lists : Fragment(R.layout.fragment_lists) {

    private var _binding: FragmentListsBinding? = null
    private val binding get() = _binding!!

    private lateinit var listsAdapter: ListsAdapter
    private val taskList = mutableListOf<Task>()

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

        setupRecyclerView()
        fetchTasks()
        updateNoTasksMessage()
    }

    private fun setupRecyclerView() {
        binding.listsrecyclerView.layoutManager = LinearLayoutManager(context)

        // Initialize adapter
        listsAdapter = ListsAdapter(taskList) { task ->
            handleTaskClick(task)
        }

        listsAdapter.setOnDeleteClickListener { task ->
            showDeleteConfirmationDialog(task)
        }

        binding.listsrecyclerView.adapter = listsAdapter
    }

    private fun fetchTasks() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response: Response<List<Task>> = RetrofitClient.getApiService(requireContext()).getAllTasks()
                if (response.isSuccessful) {
                    response.body()?.let { tasks ->
                        Log.d("ListsFragment", "Fetched tasks: ${tasks.size}")

                        // Clear and update task list on the main thread
                        withContext(Dispatchers.Main) {
                            taskList.clear()
                            taskList.addAll(tasks)
                            listsAdapter.notifyDataSetChanged()
                            updateNoTasksMessage()
                        }
                    }
                } else {
                    Log.e("ListsFragment", "Error fetching tasks: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("ListsFragment", "Exception fetching tasks", e)
            }
        }
    }

    private fun handleTaskClick(task: Task) {
        val intent = Intent(requireContext(), TaskViewRecycler::class.java)
        intent.putExtra("TASK_ID", task.id)
        startActivity(intent)
    }

    private fun showDeleteConfirmationDialog(task: Task) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Delete Task")
            .setMessage("Are you sure you want to delete this task?")
            .setPositiveButton("Delete") { _, _ -> deleteTask(task) }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
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

    private fun deleteTask(task: Task) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.getApiService(requireContext()).deleteTask(task.id)
                if (response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        val index = taskList.indexOf(task)
                        if (index != -1) {
                            taskList.removeAt(index)
                            listsAdapter.notifyItemRemoved(index)
                            updateNoTasksMessage()
                        }
                    }
                } else {
                    Log.e("ListsFragment", "Error deleting task: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("ListsFragment", "Exception deleting task", e)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
