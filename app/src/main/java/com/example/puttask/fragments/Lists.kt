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

    private lateinit var btnRepeat: AppCompatButton
    private lateinit var repeatDaysSelected: BooleanArray
    private val repeatDays = arrayOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")


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

        // Initialize all views
        val tvTaskName = dialogView.findViewById<TextView>(R.id.taskname)
        val tvTaskDescription = dialogView.findViewById<TextView>(R.id.taskdescription)
        val tvDueDate = dialogView.findViewById<TextView>(R.id.tvStartDate)
        val tvTimeReminder = dialogView.findViewById<TextView>(R.id.tvEndDate)
        val tvCategory = dialogView.findViewById<TextView>(R.id.tvList)
        val tvRepeat = dialogView.findViewById<TextView>(R.id.tvRepeat)
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
        tvRepeat.text = task.repeat_days?.joinToString(", ") ?: "No repeat days selected"

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

        dialogView.findViewById<AppCompatButton>(R.id.btnRepeat).setOnClickListener {
            showRepeatDaysDialog { selectedDays ->
                task.repeat_days = selectedDays // Update the repeat_days in the task
                tvRepeat.text = selectedDays.joinToString(", ")
            }
        }

        // Handle the Update button click event
        btnUpdate.setOnClickListener {
            // Code to handle the task update logic
            // For example, updating the task on the server
            updateTask(task)
        }

        dialogBuilder.create().show()
    }

    private fun showRepeatDaysDialog(onDaysSelected: (List<String>) -> Unit) {
        repeatDaysSelected = BooleanArray(repeatDays.size)

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Select Repeat Days")
        builder.setMultiChoiceItems(repeatDays, repeatDaysSelected) { _, which, isChecked ->
            repeatDaysSelected[which] = isChecked
        }

        builder.setPositiveButton("OK") { dialog, _ ->
            val selectedDays = repeatDays.filterIndexed { index, _ -> repeatDaysSelected[index] }

            if (selectedDays.isNotEmpty()) {
                onDaysSelected(selectedDays) // Pass selected days to the callback
                Toast.makeText(requireContext(), "Repeats on: ${selectedDays.joinToString(", ")}", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "No repeat days selected", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        builder.create().show()
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

    // Function to show date picker and update the TextView with the selected date
    private fun showDatePicker(tvDueDate: TextView) {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val date = "$dayOfMonth/${month + 1}/$year"
                tvDueDate.text = date
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    // Function to show time picker and update the TextView with the selected time
    private fun showTimePicker(tvTimeReminder: TextView, tvDueDate: TextView) {
        val calendar = Calendar.getInstance()
        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                val time = String.format("%02d:%02d", hourOfDay, minute)
                tvTimeReminder.text = time
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        )
        timePickerDialog.show()
    }

    // Function to show delete confirmation dialog
    private fun showDeleteConfirmationDialog(task: Task) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Task")
            .setMessage("Are you sure you want to delete this task?")
            .setPositiveButton("Yes") { _, _ ->
                deleteTask(task)
            }
            .setNegativeButton("No", null)
            .show()
    }

    // Function to delete the task
    private fun deleteTask(task: Task) {
        // Code to delete the task
        Toast.makeText(requireContext(), "Task deleted successfully", Toast.LENGTH_SHORT).show()
        fetchTasks() // Refresh the task list after deletion
    }

    private fun updateNoTasksMessage() {
        if (taskList.isEmpty()) {
            binding.tvNotask.visibility = View.VISIBLE
            //MAYINALIS ME
        } else {
            binding.tvNotask.visibility = View.GONE
            //MAYINALIS ME
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
