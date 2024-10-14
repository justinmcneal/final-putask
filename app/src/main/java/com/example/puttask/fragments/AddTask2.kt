package com.example.puttask.fragments

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.cardview.widget.CardView
import com.example.puttask.MainActivity
import com.example.puttask.R
import com.example.puttask.api.CreateRequest
import com.example.puttask.api.RetrofitClient
import com.example.puttask.api.Task
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
        val repeatDays = if (switchRepeat.isChecked) listOf("Monday", "Wednesday") else null // Replace with actual selected repeat days
        val task = Task(1, "Task Name", "Description", "2024-10-14", "2024-10-15", listOf("Monday"), "Work", false)
        Log.d("AddTask2", "Creating Task: $task")



        val createRequest = CreateRequest(
            task_name = etTaskName.text.toString(),
            task_description = etTaskDescription.text.toString(),
            start_datetime = tvDueDate.text.toString(),
            end_datetime = tvTimeReminder.text.toString(),
            repeat_days = repeatDays,
            category = tvList.text.toString()
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response: Response<Task> = RetrofitClient.apiService.createTask(createRequest)
                runOnUiThread {
                    if (response.isSuccessful) {
                        val task = response.body()
                        Log.d("AddTask2", "API Task Response: $task")

                        // Check if the task is not null before proceeding
                        task?.let {
                            val intent = Intent()
                            intent.putExtra("NEW_TASK", it)
                            setResult(RESULT_OK, intent)
                            finish() // Finish and pass the task back to the calling activity
                        } ?: run {
                            Log.e("AddTask2", "API response returned null task")
                            Toast.makeText(this@AddTask2, "Failed to create task: Task is null", Toast.LENGTH_SHORT).show()
                        }

                    } else {
                        // Log API failure message
                        Log.e("AddTask2", "API Failure: ${response.errorBody()?.string()}")
                        Toast.makeText(this@AddTask2, "Failed to create task: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                // Catch any exceptions that occur during the coroutine execution
                Log.e("AddTask2", "Exception in API call: ${e.localizedMessage}", e)
                runOnUiThread {
                    Toast.makeText(this@AddTask2, "Failed to create task: Exception occurred", Toast.LENGTH_SHORT).show()
                }
            }
        }


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
