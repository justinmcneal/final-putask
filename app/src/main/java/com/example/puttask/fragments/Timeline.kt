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
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arjungupta08.horizontal_calendar_date.HorizontalCalendarAdapter
import com.arjungupta08.horizontal_calendar_date.HorizontalCalendarSetUp
import com.example.puttask.ListsAdapter
import com.example.puttask.R
import com.example.puttask.api.RetrofitClient
import com.example.puttask.api.Task
import com.example.puttask.databinding.FragmentTimelineBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.util.Calendar

class Timeline : Fragment(R.layout.fragment_timeline), HorizontalCalendarAdapter.OnItemClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvDateMonth: TextView
    private lateinit var ivCalendarNext: ImageView
    private lateinit var ivCalendarPrevious: ImageView
    private lateinit var listsAdapter: ListsAdapter
    private var taskList: MutableList<Task> = mutableListOf() // Store fetched tasks
    private lateinit var token: String


    private var _binding: FragmentTimelineBinding? = null
    private val binding get() = _binding!!

    private lateinit var repeatDaysSelected: BooleanArray
    private val repeatDays = arrayOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTimelineBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        recyclerView = view.findViewById(R.id.recyclerView)
        tvDateMonth = view.findViewById(R.id.text_date_month)
        ivCalendarNext = view.findViewById(R.id.iv_calendar_next)
        ivCalendarPrevious = view.findViewById(R.id.iv_calendar_previous)

        // Set up RecyclerView for tasks list
        setupRecyclerView()
        fetchTasks()
        updateNoTasksMessage()

        // Set up RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        val calendarSetUp = HorizontalCalendarSetUp()
        val tvMonth = calendarSetUp.setUpCalendarAdapter(recyclerView, this)
        tvDateMonth.text = tvMonth

        calendarSetUp.setUpCalendarPrevNextClickListener(ivCalendarNext, ivCalendarPrevious, this) {
            tvDateMonth.text = it

        }

    }

    private fun setupRecyclerView() {
        binding.listsrecyclerView.layoutManager = LinearLayoutManager(context)

        listsAdapter = ListsAdapter(taskList, { task ->
            handleTaskClick(task)  // Handle task click
        }, { completedTask ->
            markTaskAsComplete(completedTask)  // Handle task completion
        })
        binding.listsrecyclerView.adapter = listsAdapter

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
                        // Update UI on main thread
                        withContext(Dispatchers.Main) {
                            taskList.clear()
                            taskList.addAll(tasks)
                            listsAdapter.notifyDataSetChanged()  // Notify adapter of data changes
                            updateNoTasksMessage()
                        }
                    } ?: run {
                        // Handle empty body case
                        showError("No tasks found")
                    }
                } else {
                    showError("Error fetching tasks: ${response.message()}")
                }
            } catch (e: Exception) {
                showError("Failed to fetch tasks: ${e.message}")
            } finally {
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }
    }


    private fun markTaskAsComplete(task: Task) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Make the API call to complete the task
                val response = RetrofitClient.getApiService(requireContext()).completeTask("Bearer $token", task.id)

                if (response.isSuccessful) {
                    val responseBody = response.body()

                    withContext(Dispatchers.Main) {
                        if (responseBody != null) {
                            Toast.makeText(requireContext(), "Response: $responseBody", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(requireContext(), "Task marked as complete", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Log.e("ListsFragment", "Error marking task as complete: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("ListsFragment", "Exception marking task as complete", e)
            }
        }
    }



    // Function to get the current date in the format dd/MM/yyyy
    private fun getCurrentDate(): String {
        val calendar = Calendar.getInstance()
        return String.format(
            "%02d/%02d/%04d",
            calendar.get(Calendar.DAY_OF_MONTH),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.YEAR)
        )
    }
    private suspend fun handleEmptyResponse() {
        withContext(Dispatchers.Main) {
            taskList.clear()
            listsAdapter.notifyDataSetChanged()
            updateNoTasksMessage()
        }
    }

    private suspend fun showError(message: String) {
        withContext(Dispatchers.Main) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            binding.swipeRefreshLayout.isRefreshing = false
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
        // Filter tasks based on the selected date
        filterTasksByDate(ddMmYy)
        fetchTasks()
    }

    private fun filterTasksByDate(selectedDate: String) {
        val filteredTasks = taskList.filter { task ->
            task.end_date == selectedDate // Assuming end_date is in the same format dd/MM/yyyy
        }

        // Update the adapter with the filtered list
        listsAdapter.updateTasks(filteredTasks)
        updateNoTasksMessage()
    }



}

