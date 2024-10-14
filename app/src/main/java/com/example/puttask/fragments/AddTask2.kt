package com.example.puttask.fragments

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.puttask.api.Task
import com.example.puttask.utils.TaskJsonReader
import com.example.puttask.MainActivity
import com.example.puttask.R
import com.example.puttask.TaskAdapter
import com.example.puttask.api.CreateRequest
import com.example.puttask.api.DeleteResponse
import com.example.puttask.api.RetrofitClient
import com.example.puttask.api.UpdateRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response
import java.util.*

class AddTask2 : AppCompatActivity() {

    private lateinit var addIcon: ImageView
    private lateinit var tvList: TextView
    private lateinit var switchRepeat: Switch
    private lateinit var tvDueDate: TextView
    private lateinit var tvTimeReminder: TextView
    private lateinit var dimBackground: View
    private lateinit var popupCardView: CardView
    private lateinit var createButton: AppCompatButton
    private lateinit var etTaskName: EditText
    private lateinit var etTaskDescription: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_task2)
        initViews()
        setupListeners()
    }

    private fun initViews() {
        addIcon = findViewById(R.id.imListAdd)
        tvList = findViewById(R.id.tvList)
        etTaskName = findViewById(R.id.taskname)
        etTaskDescription = findViewById(R.id.taskdescription)
        tvDueDate = findViewById(R.id.tvStartDate)
        tvTimeReminder = findViewById(R.id.tvEndDate)
        dimBackground = findViewById(R.id.dimBackground)
        popupCardView = findViewById(R.id.popupCardView)
        createButton = findViewById(R.id.CreateButton)
        switchRepeat = findViewById(R.id.switchRepeat) // Make sure you have the correct ID here

    }

    private fun setupListeners() {
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { startActivity(Intent(this, MainActivity::class.java)) }
        addIcon.setOnClickListener { showCategoryPopup() }
        findViewById<ImageButton>(R.id.addDueIcon).setOnClickListener { showDatePicker() }
        findViewById<ImageButton>(R.id.addTimeIcon).setOnClickListener { showTimePicker() }
        switchRepeat.setOnCheckedChangeListener { _, isChecked -> updateRepeatUI(isChecked) }
        findViewById<TextView>(R.id.tvCancel).setOnClickListener { clearFields(); togglePopupVisibility(false) }
        findViewById<TextView>(R.id.tvDone).setOnClickListener { createTask() }
        createButton.setOnClickListener { createTask() }
    }

    private fun showCategoryPopup() {
        PopupMenu(this, addIcon).apply {
            menuInflater.inflate(R.menu.popup_categories, menu)
            setOnMenuItemClickListener { menuItem ->
                tvList.text = when (menuItem.itemId) {
                    R.id.personal -> "Personal"
                    R.id.work -> "Work"
                    R.id.school -> "School"
                    R.id.social -> "Social"
                    else -> ""
                }.takeIf { it.isNotEmpty() }
                true
            }
            show()
        }
    }

    private fun showDatePicker() {
        Calendar.getInstance().let { calendar ->
            DatePickerDialog(this, { _, year, month, day ->
                tvDueDate.text = String.format("%02d/%02d/%04d", day, month + 1, year)
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }
    }

    private fun showTimePicker() {
        Calendar.getInstance().let { calendar ->
            TimePickerDialog(this, { _, hourOfDay, minute ->
                tvTimeReminder.text = String.format("%02d:%02d %s", if (hourOfDay > 12) hourOfDay - 12 else if (hourOfDay == 0) 12 else hourOfDay, minute, if (hourOfDay >= 12) "PM" else "AM")
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show()
        }
    }

    private fun updateRepeatUI(isChecked: Boolean) {
        findViewById<HorizontalScrollView>(R.id.hsvDaily).visibility = if (isChecked) View.VISIBLE else View.GONE
        findViewById<LinearLayout>(R.id.llButtonEnd).visibility = if (isChecked) View.VISIBLE else View.GONE
        findViewById<LinearLayout>(R.id.llBtn).visibility = if (isChecked) View.GONE else View.VISIBLE
        popupCardView.layoutParams.height = if (isChecked) 900 else 300
        findViewById<AppCompatButton>(R.id.btnRepeat).text = if (isChecked) "Yes" else "No"
    }

    private fun createTask() {
        // Collect user input for task creation
        val repeatDays = if (switchRepeat.isChecked) listOf("Monday", "Wednesday") else null // Sample days
        val createRequest = CreateRequest(
            task_name = etTaskName.text.toString(),
            task_description = etTaskDescription.text.toString(),
            start_datetime = tvDueDate.text.toString(),
            end_datetime = tvTimeReminder.text.toString(),
            repeat_days = repeatDays,
            category = tvList.text.toString()
        )

        // Launch the coroutine to make the API call
        CoroutineScope(Dispatchers.IO).launch {
            val response: Response<Task> = RetrofitClient.apiService.createTask(createRequest)
            runOnUiThread {
                if (response.isSuccessful) {
                    Toast.makeText(this@AddTask2, "Task created successfully!", Toast.LENGTH_SHORT).show()
                    clearFields() // Reset form after successful task creation
                } else {
                    Toast.makeText(this@AddTask2, "Failed to create task: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getTasks() {
        CoroutineScope(Dispatchers.IO).launch {
            val response: Response<List<Task>> = RetrofitClient.apiService.getAllTasks()
            runOnUiThread {
                if (response.isSuccessful) {
                    val taskList: List<Task> = response.body() ?: emptyList()
                    // Pass the taskList to RecyclerView adapter
                    setupRecyclerView(taskList)
                } else {
                    Toast.makeText(this@AddTask2, "Failed to fetch tasks", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }




    private fun updateTask(taskId: Int) {
        val updateRequest = UpdateRequest(
            task_name = etTaskName.text.toString(),
            task_description = etTaskDescription.text.toString(),
            start_datetime = tvDueDate.text.toString(),
            end_datetime = tvTimeReminder.text.toString(),
            repeat_days = listOf("Monday", "Wednesday"),  // example repeat days
            category = tvList.text.toString()
        )

        CoroutineScope(Dispatchers.IO).launch {
            val response: Response<Task> = RetrofitClient.apiService.updateTask(taskId, updateRequest)
            runOnUiThread {
                if (response.isSuccessful) {
                    Toast.makeText(this@AddTask2, "Task updated successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@AddTask2, "Failed to update task: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun deleteTask(taskId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val response: Response<DeleteResponse> = RetrofitClient.apiService.deleteTask(taskId)
            runOnUiThread {
                if (response.isSuccessful) {
                    Toast.makeText(this@AddTask2, "Task deleted successfully!", Toast.LENGTH_SHORT).show()
                    // Optionally refresh the task list
                    getTasks()
                } else {
                    Toast.makeText(this@AddTask2, "Failed to delete task: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupRecyclerView(taskList: List<Task>) {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = TaskAdapter(taskList) { taskId ->
            // Handle deleteTask when delete button is clicked
            deleteTask(taskId)
        }
        recyclerView.adapter = adapter
    }


    private fun clearFields() {
        etTaskName.text.clear()
        etTaskDescription.text.clear()
        tvList.text = ""
        tvTimeReminder.text = ""
        tvDueDate.text = ""
    }

    private fun togglePopupVisibility(isVisible: Boolean) {
        dimBackground.visibility = if (isVisible) View.VISIBLE else View.GONE
        popupCardView.visibility = if (isVisible) View.VISIBLE else View.GONE
    }


}
