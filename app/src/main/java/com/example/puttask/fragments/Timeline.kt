package com.example.puttask.fragments

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arjungupta08.horizontal_calendar_date.HorizontalCalendarAdapter
import com.arjungupta08.horizontal_calendar_date.HorizontalCalendarSetUp
import com.example.puttask.ListsAdapter
import com.example.puttask.R
import com.example.puttask.api.RetrofitClient
import com.example.puttask.api.Task
import com.example.puttask.databinding.FragmentListsBinding
import com.example.puttask.databinding.FragmentTimelineBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.util.Calendar

class Timeline : Fragment(R.layout.fragment_timeline), HorizontalCalendarAdapter.OnItemClickListener {

    private var _binding: FragmentTimelineBinding? = null
    private val binding get() = _binding!!

    private lateinit var listsAdapter: ListsAdapter
    private val taskList = mutableListOf<Task>()
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvDateMonth: TextView
    private lateinit var ivCalendarNext: ImageView
    private lateinit var ivCalendarPrevious: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTimelineBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views using the binding object
        recyclerView = binding.listsrecyclerView // Correct if ID is "listsrecyclerView" in XML
        tvDateMonth = binding.textDateMonth // Correct if ID is "text_date_month" in XML
        ivCalendarNext = binding.ivCalendarNext // Correct if ID is "iv_calendar_next" in XML
        ivCalendarPrevious = binding.ivCalendarPrevious // Correct if ID is "iv_calendar_previous" in XML

        // Set up RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)
        listsAdapter = ListsAdapter(taskList) { task ->
            handleTaskClick(task)
        }
        listsAdapter.setOnDeleteClickListener { task ->
            showDeleteConfirmationDialog(task)
        }
        recyclerView.adapter = listsAdapter

        // Set up horizontal calendar
        val calendarSetUp = HorizontalCalendarSetUp()
        val tvMonth = calendarSetUp.setUpCalendarAdapter(recyclerView, this)
        tvDateMonth.text = tvMonth

        calendarSetUp.setUpCalendarPrevNextClickListener(ivCalendarNext, ivCalendarPrevious, this) {
            tvDateMonth.text = it
        }

        setupSwipeRefresh()
        fetchTasks()
        updateNoTasksMessage()
    }

    override fun onItemClick(ddMmYy: String, dd: String, day: String) {
        // Fetch tasks based on the selected end date
        fetchTasksForDate(ddMmYy) // Ensure the format matches your task's end_date
    }


    private fun fetchTasksForDate(date: String) {
        // Convert date from "DD/MM/YYYY" to "YYYY-MM-DD" if needed
        val dateParts = date.split("/")
        val formattedDate = "${dateParts[2]}-${dateParts[1]}-${dateParts[0]}" // Convert to "YYYY-MM-DD"

        // Filter tasks based on the selected date
        val filteredTasks = taskList.filter { task ->
            task.end_date == formattedDate // Ensure this matches your date format
        }

        listsAdapter.updateTasks(filteredTasks)
        updateNoTasksMessage()
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
                        Log.d("TimelineFragment", "Fetched tasks: ${tasks.size}")

                        // Clear and update task list on the main thread
                        withContext(Dispatchers.Main) {
                            taskList.clear()
                            taskList.addAll(tasks)
                            listsAdapter.notifyDataSetChanged()
                            updateNoTasksMessage()
                        }
                    }
                } else {
                    Log.e("TimelineFragment", "Error fetching tasks: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("TimelineFragment", "Exception fetching tasks", e)
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
            updateTask(task)
        }

        dialogBuilder.create().show()
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

    private fun showDatePicker(tvDueDate: TextView) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
            // Format the selected date and set it to the TextView
            val formattedDate = String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear)
            tvDueDate.text = formattedDate
        }, year, month, day)

        datePickerDialog.show()
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

    private fun updateTask(task: Task) {
        // Code to update task details
        Toast.makeText(requireContext(), "Task updated successfully", Toast.LENGTH_SHORT).show()
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
            binding.tvNotasks.visibility = View.VISIBLE
            binding.listsrecyclerView.visibility = View.GONE
        } else {
            binding.tvNotasks.visibility = View.GONE
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
                    Log.e("TimelineFragment", "Error deleting task: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("TimelineFragment", "Exception deleting task", e)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
