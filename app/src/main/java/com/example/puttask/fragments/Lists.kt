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
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.puttask.ListsAdapter
import com.example.puttask.R
import com.example.puttask.api.RetrofitClient
import com.example.puttask.api.Task
import com.example.puttask.api.UpdateRequest
import com.example.puttask.databinding.FragmentListsBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class Lists : Fragment(R.layout.fragment_lists) {

    private var _binding: FragmentListsBinding? = null
    private val binding get() = _binding!!
    private lateinit var listsAdapter: ListsAdapter
    private val taskList = mutableListOf<Task>()
    private lateinit var addTaskLauncher: ActivityResultLauncher<Intent>
    private lateinit var repeatDaysSelected: BooleanArray

    private val repeatDays = arrayOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    private lateinit var popupcardviewLists: CardView
    private lateinit var tvDropdownLists: TextView
    private lateinit var ic_sort: ImageView
    private lateinit var tvOldesttoNewest: TextView
    private lateinit var tvNewesttoOldest: TextView
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
        ic_sort = binding.icSort
        tvDropdownLists = binding.tvDropdownLists
        popupcardviewLists = binding.popupcardviewLists
        tvOldesttoNewest =binding.tvOldesttoNewest
        tvNewesttoOldest = binding.tvNewesttoOldest
        setupRecyclerView()
        setupSwipeRefresh()
        fetchTasks()
        updateNoTasksMessage()
        updateUsernameDisplay()

        val dropdownLists = PopupMenu(requireContext(), tvDropdownLists)
        val menuMap = mapOf(
            R.id.allItems to "All Items",
            R.id.personal to "Personal",
            R.id.work to "Work",
            R.id.school to "School",
            R.id.wishlist to "Wishlist"
        )

        dropdownLists.menuInflater.inflate(R.menu.dropdown_lists, dropdownLists.menu)

        binding.tvDropdownLists.setOnClickListener {
            dropdownLists.setOnMenuItemClickListener { menuItem ->
                menuMap[menuItem.itemId]?.let { selectedCategory ->
                    tvDropdownLists.text = selectedCategory
                    filterTasksByCategory(selectedCategory) // Pass the correct category
                    true
                } ?: false
            }
            dropdownLists.show()
        }
        binding.tvNewesttoOldest.setOnClickListener {
            sortTasksByDateDescending() // Sort from newest to oldest
            visibilityChecker() // Hide the popup after selection
        }

        binding.tvOldesttoNewest.setOnClickListener {
            sortTasksByDateAscending() // Sort from oldest to newest
            visibilityChecker() // Hide the popup after selection
        }

        // Sort options
        ic_sort.setOnClickListener {
            visibilityChecker()
        }

        val sharedPreferences = requireContext().getSharedPreferences("user_prefs", AppCompatActivity.MODE_PRIVATE)
        val username = sharedPreferences.getString("username", "User")  // Default is "User" if not found
        binding.tvUsername.text = "Hi $username!"
    }
    private fun sortTasksByDateDescending() {
        taskList.sortByDescending { parseDate(it.end_date) ?: Date(0) } // Handle null dates
        listsAdapter.notifyDataSetChanged()
    }

    private fun sortTasksByDateAscending() {
        taskList.sortBy { parseDate(it.end_date) ?: Date(Long.MAX_VALUE) }
        listsAdapter.notifyDataSetChanged()
    }

    private fun parseDate(dateString: String): Date? {
        return try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            dateFormat.parse(dateString)
        } catch (e: ParseException) {
            Log.e("ListsFragment", "Error parsing date: $dateString", e)
            null
        }
    }


    private fun filterTasksByCategory(category: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response: Response<List<Task>> = RetrofitClient.getApiService(requireContext()).getAllTasks()
                if (response.isSuccessful) {
                    response.body()?.let { tasks ->
                        // If "All Items" is selected, show all tasks
                        val filteredTasks = if (category == "All Items") {
                            tasks // Show all tasks
                        } else {
                            tasks.filter { it.category == category } // Filter by selected category
                        }

                        withContext(Dispatchers.Main) {
                            taskList.clear()
                            taskList.addAll(filteredTasks)
                            listsAdapter.notifyDataSetChanged()
                            updateNoTasksMessage()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("ListsFragment", "Exception fetching tasks", e)
            }
        }
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
        val tvRepeat = dialogView.findViewById<TextView>(R.id.tvRepeat)
        val btnCategory = dialogView.findViewById<ImageView>(R.id.imListAdd)
        val addDueIcon = dialogView.findViewById<ImageButton>(R.id.addDueIcon)
        val addTimeIcon = dialogView.findViewById<ImageButton>(R.id.addTimeIcon)
        val btnUpdate = dialogView.findViewById<AppCompatButton>(R.id.btnUpdate)
        tvTaskName.text = task.task_name
        tvTaskDescription.text = task.task_description
        tvDueDate.text = task.end_date
        tvTimeReminder.text = task.end_time
        tvCategory.text = task.category
        tvRepeat.text = task.repeat_days?.joinToString(", ") ?: "No repeat days selected"

        btnCategory.setOnClickListener {
            showCategoryPopup(btnCategory, tvCategory)
        }
        addDueIcon.setOnClickListener {
            showDatePicker(tvDueDate)
        }
        addTimeIcon.setOnClickListener {
            showTimePicker(tvTimeReminder, tvDueDate)
        }
        dialogView.findViewById<AppCompatButton>(R.id.btnRepeat).setOnClickListener {
            showRepeatDaysDialog { selectedDays ->
                task.repeat_days = selectedDays // Update the repeat_days in the task
                tvRepeat.text = selectedDays.joinToString(", ")
            }
        }
        btnUpdate.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val fetchResponse: Response<Task> = RetrofitClient.getApiService(requireContext()).getTaskById(task.id)
                    if (fetchResponse.isSuccessful) {
                        val currentTask = fetchResponse.body()

                        // Retrieve input values
                        val newTaskName = tvTaskName.text.toString()
                        val newTaskDescription = tvTaskDescription.text.toString()
                        val newEndDate = tvDueDate.text.toString()
                        Log.d("UpdateTask", "Raw End Date Input: $newEndDate")

                        // Parse the selected date
                        val dateFormatInput = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        val dateFormatAPI = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

                        val selectedDate: Date? = try {
                            dateFormatInput.parse(newEndDate)
                        } catch (e: ParseException) {
                            Log.e("UpdateTask", "Error parsing end date: $newEndDate", e)
                            null
                        }




                        val formattedEndDate = selectedDate?.let { dateFormatAPI.format(it) }
                        val currentDate = dateFormatAPI.parse(dateFormatAPI.format(Date()))
                        Log.d("UpdateTask", "Current Date: ${dateFormatAPI.format(currentDate)}")

                        if (formattedEndDate != null) {
                            Log.d("UpdateTask", "Formatted End Date: $formattedEndDate")
                        } else {
                            Log.e("UpdateTask", "Failed to format end date")
                        }

                        // Parse the end time
                        val newEndTime = tvTimeReminder.text.toString().let {
                            try {
                                val inputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                                val date = inputFormat.parse(it)
                                SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
                            } catch (e: Exception) {
                                Log.e("UpdateTask", "Error parsing time: $it", e)
                                ""
                            }
                        }
                        Log.d("UpdateTask", "New End Time: $newEndTime")

                        // Combine date and time
                        val combinedDateTime = selectedDate?.let {
                            Calendar.getInstance().apply {
                                time = it
                                val timeParts = newEndTime.split(":")
                                if (timeParts.size == 2) {
                                    set(Calendar.HOUR_OF_DAY, timeParts[0].toInt())
                                    set(Calendar.MINUTE, timeParts[1].toInt())
                                }
                            }
                        }

                        // Validate combined date-time
                        if (selectedDate != null) {
                            if (selectedDate.after(currentDate)) {
                                // Valid future date
                            } else {
                                // Check if it's the same day
                                val now = Calendar.getInstance()
                                if (now.get(Calendar.YEAR) == selectedDate.year + 1900 &&
                                    now.get(Calendar.MONTH) == selectedDate.month &&
                                    now.get(Calendar.DAY_OF_MONTH) == selectedDate.date) {
                                    // Validate time
                                    if (combinedDateTime?.before(now) == true) {
                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(requireContext(), "Selected time cannot be in the past", Toast.LENGTH_SHORT).show()
                                        }
                                        return@launch
                                    }
                                }
                            }
                        }

                        val newCategory = tvCategory.text.toString()
                        val newRepeatDays = task.repeat_days

                        val updateRequest = UpdateRequest(
                            task_name = newTaskName.takeIf { it.isNotBlank() } ?: currentTask?.task_name ?: "",
                            task_description = newTaskDescription.takeIf { it.isNotBlank() } ?: currentTask?.task_description ?: "",
                            end_date = formattedEndDate ?: currentTask?.end_date ?: "",
                            end_time = newEndTime.takeIf { it.isNotBlank() } ?: currentTask?.end_time?.split(":")?.take(2)?.joinToString(":") ?: "",
                            repeat_days = newRepeatDays ?: currentTask?.repeat_days ?: emptyList(),
                            category = newCategory.takeIf { it.isNotBlank() } ?: currentTask?.category ?: ""
                        )

                        Log.d("UpdateTask", "Update request: $updateRequest")

                        // Make the API call
                        val updateResponse: Response<Task> = RetrofitClient.getApiService(requireContext()).updateTask(task.id, updateRequest)
                        if (updateResponse.isSuccessful) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(requireContext(), "Task updated successfully", Toast.LENGTH_SHORT).show()
                                fetchTasks() // Refresh task list
                                Log.d("UpdateTask", "Dismissing dialog after successful update")
                            }
                        } else {
                            Log.e("ListsFragment", "Error updating task: ${updateResponse.message()} - Response: ${updateResponse.errorBody()?.string()}")
                            withContext(Dispatchers.Main) {
                                Toast.makeText(requireContext(), "Error updating task", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Log.e("ListsFragment", "Error fetching task: ${fetchResponse.message()}")
                        withContext(Dispatchers.Main) {
                            Toast.makeText(requireContext(), "Error fetching task", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    Log.e("ListsFragment", "Exception updating task", e)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Error updating task", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

// Cancel button
        dialogBuilder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss() // Dismiss the dialog on cancel
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
    private fun visibilityChecker() {
        popupcardviewLists.visibility = if (popupcardviewLists.visibility == View.VISIBLE) View.GONE else View.VISIBLE

    }
}
