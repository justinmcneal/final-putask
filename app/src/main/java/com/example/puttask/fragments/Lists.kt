package com.example.puttask.fragments

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.puttask.ListsAdapter
import com.example.puttask.R
import com.example.puttask.api.RetrofitClient
import com.example.puttask.api.Task
import com.example.puttask.databinding.FragmentListsBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.util.Calendar

class Lists : Fragment(R.layout.fragment_lists) {

    private var _binding: FragmentListsBinding? = null
    private val binding get() = _binding!!

    private lateinit var listsAdapter: ListsAdapter
    private val taskList = mutableListOf<Task>()
    private lateinit var addTaskLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Register the ActivityResultLauncher
        addTaskLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                // Refresh the task list after returning from AddTask2 activity
                fetchTasks()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSwipeRefresh()
        fetchTasks()
        updateNoTasksMessage()
        updateUsernameDisplay()


        // Fetch and display the username from SharedPreferences
        val sharedPreferences = requireContext().getSharedPreferences("user_prefs", AppCompatActivity.MODE_PRIVATE)
        val username = sharedPreferences.getString("username", "User")  // Default is "User" if not found
        binding.tvUsername.text = "Hi $username!"
    }

    private fun updateUsernameDisplay() {
        val sharedPreferences = requireContext().getSharedPreferences("user_prefs", AppCompatActivity.MODE_PRIVATE)
        val username = sharedPreferences.getString("username", "User")  // Default is "User" if not found
        binding.tvUsername.text = "Hi $username!"
    }

    private fun setupRecyclerView() {
        binding.listsrecyclerView.layoutManager = LinearLayoutManager(context)
        listsAdapter = ListsAdapter(taskList) { task ->
            handleTaskClick(task)
        }
        listsAdapter.setOnDeleteClickListener { task ->
            showDeleteConfirmationDialog(task)
        }
        binding.listsrecyclerView.adapter = listsAdapter
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            fetchTasks()
        }
    }

    private fun fetchTasks() {
        // Show the loading indicator
        binding.swipeRefreshLayout.isRefreshing = true

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
            } finally {
                // Hide the loading indicator
                withContext(Dispatchers.Main) {
                    binding.swipeRefreshLayout.isRefreshing = false
                }
            }
        }
    }


    private fun handleTaskClick(task: Task) {
        // Create and show a dialog to display task details
        val dialogView = layoutInflater.inflate(R.layout.activity_task_view_recycler, null)
        val dialogBuilder = AlertDialog.Builder(requireContext())
            .setView(dialogView)

        val tvTaskName = dialogView.findViewById<TextView>(R.id.taskname)
        val tvTaskDescription = dialogView.findViewById<TextView>(R.id.taskdescription)
        val tvDueDate = dialogView.findViewById<TextView>(R.id.tvStartDate)
        val tvTimeReminder = dialogView.findViewById<TextView>(R.id.tvEndDate)
        val tvCategory = dialogView.findViewById<TextView>(R.id.tvList)
        val btnCategory = dialogView.findViewById<ImageView>(R.id.imListAdd)
        val addDueIcon = dialogView.findViewById<ImageButton>(R.id.addDueIcon)
        val addTimeIcon = dialogView.findViewById<ImageButton>(R.id.addTimeIcon)
        val btnUpdate = dialogView.findViewById<AppCompatButton>(R.id.btnUpdate)

        // Set task details in the dialog
        tvTaskName.text = task.task_name
        tvTaskDescription.text = task.task_description
        tvDueDate.text = task.end_date
        tvTimeReminder.text = task.end_time
        tvCategory.text = task.category

        // When the ImageView button (btnCategory) is clicked, show the popup menu
        btnCategory.setOnClickListener {
            showCategoryPopup(btnCategory, tvCategory)
        }

        // Show date picker when the add due icon is clicked
        addDueIcon.setOnClickListener {
            showDatePicker(tvDueDate) // Pass the TextView to update with the selected date
        }

        // Show time picker when the add time icon is clicked
        addTimeIcon.setOnClickListener {
            showTimePicker(tvTimeReminder, tvDueDate) // Pass the TextView for date validation
        }

        // Handle the Update button click event
        btnUpdate.setOnClickListener {
            // Code to handle the task update logic
            // For example, updating the task on the server
            updateTask(task)
        }

        dialogBuilder.create().show()
    }

    // Function to update task
    private fun updateTask(task: Task) {
        // Code to update task details
        Toast.makeText(requireContext(), "Task updated successfully", Toast.LENGTH_SHORT).show()
    }


    // Function to show the popup menu below the ImageView button and update the category TextView
    private fun showCategoryPopup(anchorView: View, categoryTextView: TextView) {
        PopupMenu(requireContext(), anchorView).apply {
            menuInflater.inflate(R.menu.popup_categories, menu)
            setOnMenuItemClickListener { menuItem ->
                categoryTextView.text = when (menuItem.itemId) {
                    R.id.personal -> "Personal"
                    R.id.work -> "Work"
                    R.id.school -> "School"
                    R.id.wishlist -> "Wishlist"
                    else -> ""
                }
                true
            }
            show()
        }
    }

    // Function to show DatePickerDialog
    private fun showDatePicker(dateTextView: TextView) {
        Calendar.getInstance().let { calendar ->
            DatePickerDialog(requireContext(), { _, year, month, day ->
                val selectedDateCalendar = Calendar.getInstance().apply {
                    set(year, month, day)
                }

                if (selectedDateCalendar.before(Calendar.getInstance())) {
                    Toast.makeText(requireContext(), "Selected date cannot be in the past", Toast.LENGTH_SHORT).show()
                } else {
                    dateTextView.text = String.format("%04d/%02d/%02d", year, month + 1, day)
                }
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }
    }

    // Function to show TimePickerDialog
    private fun showTimePicker(tvTimeReminder: TextView, tvDueDate: TextView) {
        Calendar.getInstance().let { calendar ->
            TimePickerDialog(requireContext(), { _, hourOfDay, minute ->
                val selectedTimeCalendar = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hourOfDay)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                }

                // Compare selected time with current time and date
                if (selectedTimeCalendar.before(Calendar.getInstance()) && tvDueDate.text.isNotEmpty()) {
                    Toast.makeText(requireContext(), "Selected time cannot be in the past", Toast.LENGTH_SHORT).show()
                } else {
                    tvTimeReminder.text = String.format("%02d:%02d", hourOfDay, minute)
                }
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show()
        }
    }


    private fun showDeleteConfirmationDialog(task: Task) {
        AlertDialog.Builder(requireContext()).apply {
            setTitle("Delete Task")
            setMessage("Are you sure you want to delete this task?")
            setPositiveButton("Delete") { _, _ -> deleteTask(task) }
            setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            create()
            show()
        }
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
