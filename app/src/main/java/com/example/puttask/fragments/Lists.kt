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
import androidx.core.content.ContextCompat
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
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
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(ContextCompat.getColor(requireContext(), R.color.very_blue))
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
    private fun updateUsernameDisplay() {
        val sharedPreferences = requireContext().getSharedPreferences("user_prefs", AppCompatActivity.MODE_PRIVATE)
        val username = sharedPreferences.getString("username", "User")  // Default is "User" if not found
        binding.tvUsername.text = "Hi $username!"
    }
    fun fetchTasks() {
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
        val btnUpdate = dialogView.findViewById<AppCompatButton>(R.id.btnUpdate)
        val btnRepeat = dialogView.findViewById<AppCompatButton>(R.id.btnRepeat)

        tvTaskName.text = task.task_name
        tvTaskDescription.text = task.task_description
        tvDueDate.text = task.end_date
        tvTimeReminder.text = task.end_time
        tvCategory.text = task.category

        if (task.repeat_days?.isNotEmpty() == true) {
            tvRepeat.text = "Repeats on: ${task.repeat_days!!.joinToString(", ")}"
            btnRepeat.text = "Yes"
        } else {
            tvRepeat.text = "No repeat days selected"
            btnRepeat.text = "No"
        }

        dialogView.findViewById<ImageButton>(R.id.btnBack).setOnClickListener { dialog.dismiss() }
        dialogView.findViewById<ImageView>(R.id.imListAdd).setOnClickListener { showCategoryPopup(it, tvCategory) }
        dialogView.findViewById<ImageButton>(R.id.addDueIcon).setOnClickListener { showDatePicker(tvDueDate) }
        dialogView.findViewById<ImageButton>(R.id.addTimeIcon).setOnClickListener { showTimePicker(tvTimeReminder) }
        btnRepeat.setOnClickListener { showRepeatDaysDialog(tvRepeat) { selectedDays -> task.repeat_days = selectedDays } }
        btnUpdate.setOnClickListener {
            val newTaskName = tvTaskName.text.toString()
            val newTaskDescription = tvTaskDescription.text.toString()
            val newEndDate = tvDueDate.text.toString()
            val newEndTime = tvTimeReminder.text.toString()
            val newCategory = tvCategory.text.toString()
            val newRepeatDays = task.repeat_days
            if (newEndDate.isEmpty() || newEndTime.isEmpty()) {
                Toast.makeText(requireContext(), "Please select a date and time", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val updateRequest = UpdateRequest(
                        task_name = newTaskName,
                        task_description = newTaskDescription,
                        end_date = newEndDate,
                        end_time = newEndTime,
                        repeat_days = newRepeatDays,
                        category = newCategory
                    )
                    val updateResponse: Response<Task> = RetrofitClient.getApiService(requireContext()).updateTask(task.id, updateRequest)
                    if (updateResponse.isSuccessful) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(requireContext(), "Task updated successfully", Toast.LENGTH_SHORT).show()
                            fetchTasks()
                            dialog.dismiss()
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(requireContext(), "Selected date and time cannot be in the past", Toast.LENGTH_SHORT).show()
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
            R.style.DialogTheme,
            { _, year, month, dayOfMonth -> tvDueDate.text = "$dayOfMonth/${month + 1}/$year" },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        customizeDialogButtons(datePickerDialog)
        datePickerDialog.show()
    }
    private fun showTimePicker(tvTimeReminder: TextView) {
        val calendar = Calendar.getInstance()
        val timePickerDialog = TimePickerDialog(
            requireContext(),
            R.style.DialogTheme,
            { _, hourOfDay, minute -> val hourIn12Format = if (hourOfDay > 12) hourOfDay - 12 else hourOfDay
                val amPm = if (hourOfDay >= 12) "PM" else "AM"
                tvTimeReminder.text = String.format("%02d:%02d %s", hourIn12Format, minute, amPm)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false
        )
        customizeDialogButtons(timePickerDialog)
        timePickerDialog.show()
    }
    private fun showRepeatDaysDialog(tvRepeat: TextView, onDaysSelected: (List<String>) -> Unit) {
        repeatDaysSelected = repeatDaysSelected.clone()
        AlertDialog.Builder(requireContext()).apply {
            setTitle("Select Repeat Days")
            setMultiChoiceItems(repeatDays, repeatDaysSelected) { _, which, isChecked ->
                repeatDaysSelected[which] = isChecked
            }
            setPositiveButton("OK") { dialog, _ ->
                val selectedDays = repeatDays.filterIndexed { index, _ -> repeatDaysSelected[index] }
                tvRepeat.text = if (selectedDays.isNotEmpty()) {
                    "Repeats On: ${selectedDays.joinToString(", ")}"
                } else {
                    "No repeat days selected".also {
                        Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                    }
                }
                onDaysSelected(selectedDays)
                dialog.dismiss()
            }
            setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
        }.create().apply {
            customizeDialogButtons(this)
            show()
        }
    }
    private fun customizeDialogButtons(dialog: AlertDialog) {
        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            val negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            val color = ContextCompat.getColor(requireContext(), R.color.very_blue)
            positiveButton?.setTextColor(color)
            negativeButton?.setTextColor(color)
        }
    }
    private fun showDeleteConfirmationDialog(task: Task) {
        AlertDialog.Builder(requireContext()).apply {
            setTitle("Delete Task")
            setMessage("Are you sure you want to delete this task?")
            setPositiveButton("Delete") { _, _ -> deleteTask(task)
                Toast.makeText(requireContext(), "Task deleted successfully", Toast.LENGTH_SHORT).show()
            }
            setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            create()
            show()
        }
    }
    private fun deleteTask(task: Task) {
        executeTask {
            val response = RetrofitClient.getApiService(requireContext()).deleteTask(task.id)
            if (response.isSuccessful) {
                updateTaskList(task)
            } else {
                Log.e("ListsFragment", "Error deleting task: ${response.message()}")
            }
        }
    }
    private fun markTaskAsComplete(task: Task, isChecked: Boolean) {
        executeTask {
            val response = RetrofitClient.getApiService(requireContext()).markTaskComplete(task.id, CompleteTaskRequest(id = task.id, isChecked = isChecked))
            if (response.isSuccessful) {
                if (isChecked) {
                    updateTaskList(task)
                } else {
                    fetchTasks()
                }
            } else {
                Log.e("ListsFragment", "Error marking task complete: ${response.message()}")
            }
        }
    }
    private fun executeTask(action: suspend () -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try { action() } catch (e: Exception) { Log.e("ListsFragment", "Exception during task execution", e) }
        }
    }
    private suspend fun updateTaskList(task: Task) {
        withContext(Dispatchers.Main) {
            val index = taskList.indexOf(task)
            if (index != -1) {
                taskList.removeAt(index)
                listsAdapter.notifyItemRemoved(index)
                updateNoTasksMessage()
            }
        }
    }
    private fun updateNoTasksMessage() {
        binding.tvNotask.visibility = if (taskList.isEmpty()) View.VISIBLE else View.GONE
        binding.listsrecyclerView.visibility = if (taskList.isEmpty()) View.GONE else View.VISIBLE
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}