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
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
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
    private val repeatDays = arrayOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentTimelineBinding.inflate(inflater, container, false).also { _binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(view) {
            recyclerView = findViewById(R.id.recyclerView)
            tvDateMonth = findViewById(R.id.text_date_month)
            ivCalendarNext = findViewById(R.id.iv_calendar_next)
            ivCalendarPrevious = findViewById(R.id.iv_calendar_previous)
        }

        // Set up the RecyclerView and other components before fetching tasks
        setupRecyclerView()

        // Fetch tasks first
        fetchTasks()

        // Set up calendar after tasks have been fetched
        val calendarSetUp = HorizontalCalendarSetUp()
        tvDateMonth.text = calendarSetUp.setUpCalendarAdapter(recyclerView, this)
        calendarSetUp.setUpCalendarPrevNextClickListener(ivCalendarNext, ivCalendarPrevious, this) {
            tvDateMonth.text = it
        }

        updateNoTasksMessage()
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
            val response = RetrofitClient.getApiService(requireContext()).getAllTasks()
            if (response.isSuccessful) {
                response.body()?.let { tasks ->
                    withContext(Dispatchers.Main) {

                        originalTaskList.clear()
                        originalTaskList.addAll(tasks) // Store the original task list

                        taskList.apply {
                            clear()
                            addAll(tasks)
                        }
                        listsAdapter.notifyDataSetChanged()
                        updateNoTasksMessage()
                        Log.d("Timeline", "Fetched tasks: $taskList")

                    }
                }
            }
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }

//    private fun getCurrentDate(): String {
//        val calendar = Calendar.getInstance()
//        return String.format(
//            "%02d/%02d/%04d",
//            calendar.get(Calendar.DAY_OF_MONTH),
//            calendar.get(Calendar.MONTH) + 1,
//            calendar.get(Calendar.YEAR)
//        )
//    }

    private fun handleTaskClick(task: Task) {
        val dialogView = layoutInflater.inflate(R.layout.activity_task_view_recycler, null)
        val dialogBuilder = AlertDialog.Builder(requireContext()).setView(dialogView)
        with(dialogView) {
            findViewById<TextView>(R.id.taskname).text = task.task_name
            findViewById<TextView>(R.id.taskdescription).text = task.task_description
            findViewById<TextView>(R.id.tvStartDate).text = task.end_date
            findViewById<TextView>(R.id.tvEndDate).text = task.end_time
            findViewById<TextView>(R.id.tvList).text = task.category
            val tvRepeat = findViewById<TextView>(R.id.tvRepeat).apply {
                text = task.repeat_days?.joinToString(", ") ?: "No repeat days selected"
            }
            findViewById<ImageView>(R.id.imListAdd).setOnClickListener {
                showCategoryPopup(it, findViewById(R.id.tvList))
            }
            findViewById<ImageButton>(R.id.addDueIcon).setOnClickListener {
                showDatePicker(findViewById(R.id.tvStartDate))
            }
            findViewById<ImageButton>(R.id.addTimeIcon).setOnClickListener {
                showTimePicker(findViewById(R.id.tvEndDate), findViewById(R.id.tvStartDate))
            }
            findViewById<AppCompatButton>(R.id.btnRepeat).setOnClickListener {
                showRepeatDaysDialog { selectedDays ->
                    task.repeat_days = selectedDays
                    tvRepeat.text = selectedDays.joinToString(", ")
                }
            }
            findViewById<AppCompatButton>(R.id.btnUpdate).setOnClickListener { updateTask(task) }
        }
        dialogBuilder.create().show()
    }

    private fun showRepeatDaysDialog(onDaysSelected: (List<String>) -> Unit) {
        repeatDaysSelected = BooleanArray(repeatDays.size)
        AlertDialog.Builder(requireContext()).apply {
            setTitle("Select Repeat Days")
            setMultiChoiceItems(repeatDays, repeatDaysSelected) { _, which, isChecked ->
                repeatDaysSelected[which] = isChecked
            }
            setPositiveButton("OK") { dialog, _ ->
                val selectedDays = repeatDays.filterIndexed { index, _ -> repeatDaysSelected[index] }
                Toast.makeText(requireContext(), if (selectedDays.isNotEmpty()) {
                    onDaysSelected(selectedDays)
                    "Repeats on: ${selectedDays.joinToString(", ")}"
                } else {
                    "No repeat days selected"
                }, Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
        }.create().show()
    }

    private fun updateTask(task: Task) {
        Toast.makeText(requireContext(), "Task updated successfully", Toast.LENGTH_SHORT).show()
    }

    private fun showCategoryPopup(anchorView: View, categoryTextView: TextView) {
        PopupMenu(requireContext(), anchorView).apply {
            menuInflater.inflate(R.menu.popup_categories, menu)
            setOnMenuItemClickListener { menuItem ->
                categoryTextView.text = when (menuItem.itemId) {
                    R.id.personal, R.id.work, R.id.school, R.id.wishlist -> menuItem.title
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
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Task")
            .setMessage("Are you sure you want to delete this task?")
            .setPositiveButton("Delete") { _, _ -> deleteTask(task) }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
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
            val response = RetrofitClient.getApiService(requireContext()).deleteTask(task.id)
            if (response.isSuccessful) {
                withContext(Dispatchers.Main) {
                    taskList.indexOf(task).takeIf { it != -1 }?.let { index ->
                        taskList.removeAt(index)
                        listsAdapter.notifyItemRemoved(index)
                        updateNoTasksMessage()
                    }
                }
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
        if (formattedDate != null && taskList.isNotEmpty()) {
            filterTasksByDate(formattedDate)
        } else {
            Log.d("Timeline", "No tasks available to filter or date is null.")
        }
    }


    private fun filterTasksByDate(selectedDate: String) {
        // Log the selected date for debugging
        Log.d("Timeline", "Filtering tasks for date: $selectedDate")

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

        listsAdapter.notifyDataSetChanged() // Notify the adapter of data changes
        updateNoTasksMessage() // Update the visibility of the no tasks message

    }
}

