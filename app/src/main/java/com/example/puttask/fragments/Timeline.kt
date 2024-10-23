package com.example.puttask.fragments

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arjungupta08.horizontal_calendar_date.HorizontalCalendarAdapter
import com.arjungupta08.horizontal_calendar_date.HorizontalCalendarSetUp
import com.example.puttask.ListsAdapter
import com.example.puttask.R
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
    private lateinit var repeatDaysSelected: BooleanArray
    private lateinit var tvDropdownLists: TextView
    private lateinit var ic_sort: ImageView
    private val repeatDays = arrayOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    private lateinit var popupcardviewLists: CardView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentTimelineBinding.inflate(inflater, container, false).also { _binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Access views using the binding instance
        recyclerView = binding.recyclerView
        tvDateMonth = binding.textDateMonth
        ivCalendarNext = binding.ivCalendarNext
        ivCalendarPrevious = binding.ivCalendarPrevious
        ic_sort = binding.icSort
        tvDropdownLists = binding.tvDropdownLists
        popupcardviewLists = binding.popupcardviewLists

        // Set up the RecyclerView and other components before fetching tasks
        setupRecyclerView()

        // Fetch tasks first
        fetchTasks()
        setupSwipeRefresh()

        // Set up calendar after tasks have been fetched
        val calendarSetUp = HorizontalCalendarSetUp()
        tvDateMonth.text = calendarSetUp.setUpCalendarAdapter(recyclerView, this)
        calendarSetUp.setUpCalendarPrevNextClickListener(ivCalendarNext, ivCalendarPrevious, this) {
            tvDateMonth.text = it
        }

        updateNoTasksMessage()

        // Set up the dropdown menu
        val dropdownLists = PopupMenu(requireContext(), tvDropdownLists)
        val menuMap = mapOf(
            R.id.allItems to "All Items",
            R.id.personal to "Personal",
            R.id.work to "Work",
            R.id.school to "School",
            R.id.social to "Social"
        )

        dropdownLists.menuInflater.inflate(R.menu.dropdown_lists, dropdownLists.menu)

        binding.tvDropdownLists.setOnClickListener {
            dropdownLists.setOnMenuItemClickListener { menuItem ->
                menuMap[menuItem.itemId]?.let {
                    tvDropdownLists.text = it
                    true
                } ?: false
            }
            dropdownLists.show()
        }

        // Sort options
        ic_sort.setOnClickListener {
            visibilityChecker()
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

    private var originalTaskList = mutableListOf<Task>()


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

        dialogBuilder.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
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
        Log.d("Timeline", "Formatted date: $formattedDate")

        // Call with null if the user clicked on an empty date (you can add your own logic to check for this)
        filterTasksByDate(formattedDate)
    }


    private fun filterTasksByDate(selectedDate: String?) {
        // Log the selected date for debugging
        Log.d("Timeline", "Filtering tasks for date: $selectedDate")

        if (selectedDate.isNullOrEmpty()) {
            // Restore original task list if selectedDate is null or empty
            taskList.clear()
            taskList.addAll(originalTaskList)
            Toast.makeText(requireContext(), "No date selected. Displaying all tasks.", Toast.LENGTH_SHORT).show()
        } else {
            // Filter tasks by comparing end_date
            val filteredTasks = originalTaskList.filter { task ->
                val taskEndDate = task.end_date // Assuming task.end_date is in "YYYY-MM-DD" format
                Log.d("Timeline", "Comparing task end date: $taskEndDate with selected date: $selectedDate")
                taskEndDate == selectedDate
            }

            // Log the filtered tasks for debugging
            Log.d("Timeline", "Filtered tasks count: ${filteredTasks.size}")

            if (filteredTasks.isNotEmpty()) {
                taskList.clear()
                taskList.addAll(filteredTasks)
            } else {
                taskList.clear() // Clear the list if no tasks match
                Toast.makeText(requireContext(), "No tasks found for selected date.", Toast.LENGTH_SHORT).show()
            }
        }

        listsAdapter.notifyDataSetChanged() // Notify the adapter of data changes
        updateNoTasksMessage() // Update the visibility of the no tasks message
    }
    private fun visibilityChecker() {
        popupcardviewLists.visibility = if (popupcardviewLists.visibility == View.VISIBLE) View.GONE else View.VISIBLE

    }
}

