package com.example.puttask.fragments

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
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
import com.example.puttask.api.CompleteTaskRequest
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
    private val repeatDays = arrayOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    private var repeatDaysSelected = BooleanArray(repeatDays.size)
    private lateinit var tvDropdownLists: TextView
    private lateinit var ic_sort: ImageView
    private var selectedCategory = "All Items"
    private var isAscendingOrder = true // Default sorting order


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addTaskLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                fetchTasks()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ic_sort = binding.icSort
        tvDropdownLists = binding.tvDropdownLists
        setupRecyclerView()
        setupSwipeRefresh()
        fetchTasks()
        updateNoTasksMessage()
        updateUsernameDisplay()
        setupUIElements()

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
                menuMap[menuItem.itemId]?.let { selected ->
                    selectedCategory = selected
                    tvDropdownLists.text = selectedCategory
                    applyCategoryAndSort() // Apply both filter and sorting
                    true
                } ?: false
            }
            dropdownLists.show()
        }

        ic_sort.setOnClickListener {
            showSortOptionsDialog()
        }

        listsAdapter.onTaskCheckedChangeListener = { task, isChecked ->
            markTaskAsComplete(task, isChecked)
        }

        val sharedPreferences = requireContext().getSharedPreferences("user_prefs", AppCompatActivity.MODE_PRIVATE)
        val username = sharedPreferences.getString("username", "User")
        binding.tvUsername.text = "Hi $username!"
    }

    private fun setupUIElements() {
        binding.icSort.setOnClickListener {
            showSortOptionsDialog()
        }
    }

    private fun showSortOptionsDialog() {
        val dialogBuilder = AlertDialog.Builder(requireContext())
        dialogBuilder.setTitle("Sort By")
        dialogBuilder.setItems(arrayOf("Ascending Order", "Descending Order")) { _, which ->
            isAscendingOrder = which == 0
            applyCategoryAndSort() // Apply sorting after selecting order
            val order = if (isAscendingOrder) "Ascending" else "Descending"
            Toast.makeText(requireContext(), "Sorted in $order Order", Toast.LENGTH_SHORT).show()
        }
        dialogBuilder.setNegativeButton("Close") { dialog, _ -> dialog.dismiss() }

        val dialog = dialogBuilder.create()
        dialog.setOnShowListener {
            val textView = dialog.findViewById<TextView>(android.R.id.title)
            textView?.gravity = Gravity.CENTER
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(resources.getColor(R.color.very_blue))
        }
        dialog.show()
    }

    private fun applyCategoryAndSort() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.getApiService(requireContext()).getAllTasks()
                if (response.isSuccessful) {
                    response.body()?.let { tasks ->
                        val filteredTasks = if (selectedCategory == "All Items") {
                            tasks.filter { !it.isChecked }
                        } else {
                            tasks.filter { it.category == selectedCategory && !it.isChecked }
                        }

                        val sortedTasks = if (isAscendingOrder) {
                            filteredTasks.sortedBy { parseDate(it.end_date) ?: Date(Long.MAX_VALUE) }
                        } else {
                            filteredTasks.sortedByDescending { parseDate(it.end_date) ?: Date(0) }
                        }

                        withContext(Dispatchers.Main) {
                            taskList.clear()
                            taskList.addAll(sortedTasks)
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
                        val filteredTasks = if (category == "All Items") {
                            tasks.filter { !it.isChecked }
                        } else {
                            tasks.filter { it.category == category && !it.isChecked }
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
    private fun fetchTasks() {
        binding.swipeRefreshLayout.isRefreshing = true
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response: Response<List<Task>> = RetrofitClient.getApiService(requireContext()).getAllTasks()
                if (response.isSuccessful) {
                    response.body()?.let { tasks ->
                        Log.d("ListsFragment", "Fetched tasks: ${tasks.size}")
                        withContext(Dispatchers.Main) {
                            taskList.clear()
                            taskList.addAll(tasks.filter { !it.isChecked }) // Filter out completed tasks
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
    private fun handleTaskClick(task: Task) {
        // Create and show a dialog to display task details
        val dialogView = layoutInflater.inflate(R.layout.activity_task_view_recycler, null)
        val dialogBuilder = AlertDialog.Builder(requireContext())
            .setView(dialogView)
        val dialog = dialogBuilder.create()

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
        val btnBack = dialogView.findViewById<ImageButton>(R.id.btnBack)
        val btnRepeat = dialogView.findViewById<AppCompatButton>(R.id.btnRepeat)

        tvTaskName.text = task.task_name
        tvTaskDescription.text = task.task_description
        tvDueDate.text = task.end_date
        tvTimeReminder.text = task.end_time
        tvCategory.text = task.category
        tvRepeat.text = task.repeat_days?.joinToString(", ") ?: "No repeat days selected"

        if (task.repeat_days?.isNotEmpty() == true) {
            tvRepeat.text = "Repeats on: ${task.repeat_days!!.joinToString(", ")}"
            btnRepeat.text = "Yes"
        } else {
            tvRepeat.text = "No repeat days selected"
            btnRepeat.text = "No"
        }

        // Set the initial checked state for the repeat days checkboxes
        repeatDaysSelected = repeatDays.mapIndexed { _, day -> task.repeat_days?.contains(day) ?: false }.toBooleanArray()

        btnBack.setOnClickListener {
            dialog.dismiss()
        }



        btnCategory.setOnClickListener {
            showCategoryPopup(btnCategory, tvCategory)
        }

        addDueIcon.setOnClickListener {
            showDatePicker(tvDueDate)
        }

        addTimeIcon.setOnClickListener {
            showTimePicker(tvTimeReminder, tvDueDate)
        }

        // Handling the repeat days selection dialog and updating button text accordingly
        btnRepeat.setOnClickListener {
            showRepeatDaysDialog(tvRepeat) { selectedDays ->
                task.repeat_days = selectedDays // Update the repeat_days in the task

                // Update the UI based on selected days
                if (selectedDays.isEmpty()) {
                    tvRepeat.text = "No repeat days selected"
                    btnRepeat.text = "No"
                } else {
                    tvRepeat.text = "Repeats On: " + selectedDays.joinToString(", ")
                    btnRepeat.text = "Yes"
                }
            }
        }


        // Handling the task update logic
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

                        // Make the API call
                        val updateResponse: Response<Task> = RetrofitClient.getApiService(requireContext()).updateTask(task.id, updateRequest)
                        if (updateResponse.isSuccessful) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(requireContext(), "Task updated successfully", Toast.LENGTH_SHORT).show()
                                fetchTasks() // Refresh task list
                                dialog.dismiss()
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(requireContext(), "Error updating task", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(requireContext(), "Error fetching task", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Error updating task", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        dialog.show()
    }
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
    private fun showDatePicker(tvDueDate: TextView) {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            R.style.DialogTheme, // Apply your custom theme here
            { _, year, month, dayOfMonth ->
                val date = "$dayOfMonth/${month + 1}/$year"
                tvDueDate.text = date
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        // Customize button colors
        datePickerDialog.setOnShowListener {
            datePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE)?.setTextColor(resources.getColor(R.color.very_blue))
            datePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE)?.setTextColor(resources.getColor(R.color.very_blue))
        }

        // Show the dialog
        datePickerDialog.show()
    }
    private fun showTimePicker(tvTimeReminder: TextView, tvDueDate: TextView) {
        val calendar = Calendar.getInstance()
        val timePickerDialog = TimePickerDialog(
            requireContext(), // Use requireContext() for the fragment
            R.style.DialogTheme, // Apply your custom theme here
            { _, hourOfDay, minute ->
                // Format the time in 12-hour format with AM/PM
                val hourIn12Format = if (hourOfDay > 12) hourOfDay - 12 else hourOfDay
                val amPm = if (hourOfDay >= 12) "PM" else "AM"
                val time = String.format("%02d:%02d %s", hourIn12Format, minute, amPm)
                tvTimeReminder.text = time
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false // Set to false to display AM/PM
        )

        // Customize button colors
        timePickerDialog.setOnShowListener {
            timePickerDialog.getButton(TimePickerDialog.BUTTON_POSITIVE)?.setTextColor(resources.getColor(R.color.very_blue))
            timePickerDialog.getButton(TimePickerDialog.BUTTON_NEGATIVE)?.setTextColor(resources.getColor(R.color.very_blue))
        }

        timePickerDialog.show()
    }
    private fun showRepeatDaysDialog(tvRepeat: TextView, onDaysSelected: (List<String>) -> Unit) {
        // Initialize repeatDaysSelected with the current state of the task
        repeatDaysSelected = repeatDaysSelected.clone()
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Select Repeat Days")

        // Multi-choice dialog for repeat days
        builder.setMultiChoiceItems(repeatDays, repeatDaysSelected) { _, which, isChecked ->
            repeatDaysSelected[which] = isChecked
        }

        builder.setPositiveButton("OK") { dialog, _ ->
            // Get the selected days based on the checkbox states
            val selectedDays = repeatDays.filterIndexed { index, _ -> repeatDaysSelected[index] }

            // Debug log to check selected days
            Log.d("RepeatDaysDialog", "Selected Days: $selectedDays")

            // Update the TextView and handle the callback
            if (selectedDays.isNotEmpty()) {
                tvRepeat.text = "Repeats On: " + selectedDays.joinToString(", ")
                onDaysSelected(selectedDays) // Callback with the selected days
            } else {
                tvRepeat.text = "No repeat days selected"
                Toast.makeText(requireContext(), "No repeat days selected", Toast.LENGTH_SHORT).show()
            }

            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        // Create the dialog
        val dialog = builder.create()
        dialog.setOnShowListener {
            // Set the text color of the buttons to blue
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(resources.getColor(R.color.very_blue))
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(resources.getColor(R.color.very_blue))
        }

        // Show the dialog
        dialog.show()
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
    private fun updateNoTasksMessage() {
        if (taskList.isEmpty()) {
            binding.tvNotask.visibility = View.VISIBLE
            binding.listsrecyclerView.visibility = View.GONE
        } else {
            binding.tvNotask.visibility = View.GONE
            binding.listsrecyclerView.visibility = View.VISIBLE
        }
    }
    private fun markTaskAsComplete(task: Task, isChecked: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response: Response<Task> = RetrofitClient.getApiService(requireContext()).markTaskComplete(task.id, CompleteTaskRequest(id = task.id, isChecked = isChecked))
                if (response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        if (isChecked) {
                            val taskPosition = taskList.indexOf(task)
                            taskList.removeAt(taskPosition)
                            listsAdapter.notifyItemRemoved(taskPosition)
                        } else {
                            fetchTasks() //optional idk
                        }
                        updateNoTasksMessage()
                    }
                } else {
                    Log.e("ListsFragment", "Error marking task complete: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("ListsFragment", "Exception marking task complete", e)
            }
        }
    }
}
