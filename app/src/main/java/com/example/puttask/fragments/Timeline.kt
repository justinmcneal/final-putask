package com.example.puttask.fragments

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arjungupta08.horizontal_calendar_date.HorizontalCalendarAdapter
import com.arjungupta08.horizontal_calendar_date.HorizontalCalendarSetUp
import com.example.puttask.ListsAdapter
import com.example.puttask.R
import com.example.puttask.api.CompleteTaskRequest
import com.example.puttask.api.RetrofitClient
import com.example.puttask.api.Task
import com.example.puttask.api.UpdateRequest
import com.example.puttask.databinding.FragmentTimelineBinding
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

class Timeline : Fragment(R.layout.fragment_timeline), HorizontalCalendarAdapter.OnItemClickListener {
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvDateMonth: TextView
    private lateinit var ivCalendarNext: ImageView
    private lateinit var ivCalendarPrevious: ImageView
    private lateinit var listsAdapter: ListsAdapter
    private var taskList = mutableListOf<Task>()
    private var _binding: FragmentTimelineBinding? = null
    private val binding get() = _binding!!
    private val repeatDays = arrayOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    private  var repeatDaysSelected = BooleanArray(repeatDays.size)

    private var originalTaskList = mutableListOf<Task>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentTimelineBinding.inflate(inflater, container, false).also { _binding = it }.root
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = binding.recyclerView
        tvDateMonth = binding.textDateMonth
        ivCalendarNext = binding.ivCalendarNext
        ivCalendarPrevious = binding.ivCalendarPrevious
        setupRecyclerView()
        fetchTasks()
        setupSwipeRefresh()
        updateNoTasksMessage()
        val calendarSetUp = HorizontalCalendarSetUp()
        tvDateMonth.text = calendarSetUp.setUpCalendarAdapter(recyclerView, this)
        calendarSetUp.setUpCalendarPrevNextClickListener(ivCalendarNext, ivCalendarPrevious, this) {
            tvDateMonth.text = it
        }

        listsAdapter.onTaskCheckedChangeListener = { task, isChecked ->
            markTaskAsComplete(task, isChecked) // Call the method to mark the task as complete
        }
    }
    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            fetchTasks()
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
    private fun fetchTasks() {
        binding.swipeRefreshLayout.isRefreshing = true
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response: Response<List<Task>> = RetrofitClient.getApiService(requireContext()).getAllTasks()
                if (response.isSuccessful) {
                    response.body()?.let { tasks ->
                        Log.d("ListsFragment", "Fetched tasks: ${tasks.size}")

                        withContext(Dispatchers.Main) {
                            originalTaskList.clear()
                            originalTaskList.addAll(tasks)
                            taskList.clear()
                            taskList.addAll(originalTaskList)
                            taskList.addAll(tasks.filter { !it.isChecked }) // Filter out completed tasks






                            // Check if there's a selected date stored
                            val selectedDate = getSelectedDate()
                            if (selectedDate != null) {
                                filterTasksByDate(selectedDate)
                            }

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
            tvRepeat.text = task.repeat_days!!.joinToString(", ")
            btnRepeat.text = "Yes"
        } else {
            tvRepeat.text = "No repeat days selected"
            btnRepeat.text = "No"
        }

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
        btnRepeat.setOnClickListener {
            showRepeatDaysDialog { selectedDays ->
                task.repeat_days = selectedDays // Update the repeat_days in the task
                if (selectedDays.isNullOrEmpty()) {
                    tvRepeat.text = "No repeat days selected"
                    btnRepeat.text = "No"
                } else {
                    // Display selected days if available
                    tvRepeat.text = selectedDays.joinToString(", ")
                    btnRepeat.text = "Yes"

                }
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
                                dialog.dismiss()
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
    private fun showRepeatDaysDialog(onDaysSelected: (List<String>) -> Unit) {
        // Load the previously selected state into repeatDaysSelected
        repeatDaysSelected = repeatDaysSelected.clone()

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
                // Save the selected state so it's remembered next time
                repeatDaysSelected = repeatDaysSelected.clone()
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

    override fun onItemClick(ddMmYy: String, dd: String, day: String) {
        val inputFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formattedDate: String? = try {
            val date = inputFormat.parse(ddMmYy)
            outputFormat.format(date)
        } catch (e: ParseException) {
            Log.e("Timeline", "Error parsing clicked date: $e")
            null
        }

        formattedDate?.let {
            saveSelectedDate(it)  // Save the selected date
            Log.d("Timeline", "Formatted date: $formattedDate")

            // Filter tasks based on the selected date
            filterTasksByDate(it)
        }
    }

    // Save the selected date in SharedPreferences
    private fun saveSelectedDate(selectedDate: String) {
        val sharedPreferences = requireContext().getSharedPreferences("TasksPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("selectedDate", selectedDate)
        editor.apply()
    }

    // Retrieve the selected date from SharedPreferences
    private fun getSelectedDate(): String? {
        val sharedPreferences = requireContext().getSharedPreferences("TasksPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("selectedDate", null)
    }

    private fun filterTasksByDate(selectedDate: String?) {
        Log.d("Timeline", "Filtering tasks for date: $selectedDate")

        if (selectedDate.isNullOrEmpty()) {
            taskList.clear()
            taskList.addAll(originalTaskList)
            Toast.makeText(requireContext(), "No date selected. Displaying all tasks.", Toast.LENGTH_SHORT).show()
        } else {
            val filteredTasks = originalTaskList.filter { task ->
                task.end_date == selectedDate
            }

            Log.d("Timeline", "Filtered tasks count: ${filteredTasks.size}")

            if (filteredTasks.isNotEmpty()) {
                taskList.clear()
                taskList.addAll(filteredTasks)
            } else {
                taskList.clear()
                Toast.makeText(requireContext(), "No tasks found for selected date.", Toast.LENGTH_SHORT).show()
            }
        }

        listsAdapter.notifyDataSetChanged()
        updateNoTasksMessage()
    }

    private fun markTaskAsComplete(task: Task, isChecked: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response: Response<Task> = RetrofitClient.getApiService(requireContext()).markTaskComplete(task.id, CompleteTaskRequest(id = task.id, isChecked = isChecked))
                if (response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        if (isChecked) {
                            // Get the correct position of the task before removing it
                            val taskPosition = taskList.indexOf(task)

                            // Remove the task from the local list if marked complete
                            taskList.removeAt(taskPosition)

                            // Notify the adapter about the item removal at the correct position
                            listsAdapter.notifyItemRemoved(taskPosition)
                        } else {
                            // If unchecked, you might want to re-fetch the task and re-add it to the list
                            fetchTasks() // Optional: You can also re-fetch tasks instead of manually adding back
                        }
                        updateNoTasksMessage() // Update the visibility of the no tasks message
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

