package com.example.puttask.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.puttask.ListsAdapter
import com.example.puttask.R
import com.example.puttask.TaskViewRecycler
import com.example.puttask.api.APIService
import com.example.puttask.api.DataManager
import com.example.puttask.api.RetrofitClient
import com.example.puttask.api.Task
import com.example.puttask.api.UserInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okio.IOException
import retrofit2.HttpException

class Lists : Fragment(R.layout.fragment_lists) {

    private lateinit var listsRecyclerView: RecyclerView
    private lateinit var listsAdapter: ListsAdapter
    private lateinit var tvNoTasks: TextView // Declare the TextView for "No tasks created"
    private lateinit var dataManager : DataManager
    private lateinit var tvUsername : TextView
    // This should now be an empty list that will be populated with tasks created in AddTask2
    private val taskList = mutableListOf<Task>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        listsRecyclerView = view.findViewById(R.id.listsrecyclerView)
        tvNoTasks = view.findViewById(R.id.tvNotask) // Initialize the TextView
        tvUsername = view.findViewById(R.id.tvUsername)
        dataManager = DataManager(requireContext()) // Example of initializing DataManager


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

        // Set up the delete click listener
        listsAdapter.setOnDeleteClickListener { task ->
            showDeleteConfirmationDialog(task)
        }

        listsRecyclerView.adapter = listsAdapter

        updateNoTasksMessage() // Update visibility based on task list size

    }
    override fun onResume() {
        super.onResume()
        fetchUserInfo()
    }
    // fetching UserInfo
    private fun fetchUserInfo() {
        lifecycleScope.launch {
            try {
                //getting the token of the user
                val token = "Bearer ${dataManager.getAuthToken()}"
                Log.d("Token", "Using token: $token") // Log the token being used
                val response = RetrofitClient.apiService.getUser(token)

                if (response.isSuccessful) {
                    val userInfo: UserInfo? = response.body()
                    if (userInfo != null) {
                        //changing the textview with the actual username
                        tvUsername.text = userInfo.username
                        Log.d("UserInfo", "Fetched user info: $userInfo") // Log the fetched user info
                    } else {
                        Log.e("Error", "User info is null")
                        showError("Error fetching user info: User data is null")
                    }
                } else {
                    Log.e("Error", "Error fetching user info: ${response.code()} - ${response.message()}")
                    showError("Error fetching user info: ${response.message()}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("Error", "Exception fetching user info", e)
                showError("Error fetching user info")
            }
        }
    }
//    private fun fetchList(){
//        lifecycleScope.launch {
//            try {
//                // Network call should happen on IO thread
//                val apiService = RetrofitClient.create(APIService::class.java)
//                val response = withContext(Dispatchers.IO) {
//                    apiService.getNotes().execute() // Using execute() for synchronous call
//                }
//                if (response.isSuccessful) {
//                    val list = response.body()
//
//                    // Ensure fragment is still attached before updating UI
//                    if (isAdded && list != null) {
//                        listsAdapter = ListsAdapter(requireContext(), list)
//                        listsRecyclerView.adapter = listsAdapter
//                    } else {
//                        if (isAdded) {
//
//                            Toast.makeText(requireContext(), "No notes available", Toast.LENGTH_SHORT).show()
//                        }
//                    }
//                } else {
//                    if (isAdded) {
//                        Toast.makeText(requireContext(), "Failed to fetch notes", Toast.LENGTH_SHORT).show()
//                    }
//                }
//            } catch (e: HttpException) {
//                Toast.makeText(requireContext(), "HTTP error: ${e.message}", Toast.LENGTH_SHORT).show()
//
//            } catch (e: IOException) {
//                Toast.makeText(requireContext(), "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
//                Log.d("addnotes", e.message.toString())
//            }
//        }
//    }

    private suspend fun showError(message: String) {
        withContext(Dispatchers.Main) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
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
