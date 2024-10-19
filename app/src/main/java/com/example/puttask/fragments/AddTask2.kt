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
    private var repeatDays: MutableList<String> = mutableListOf()

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
        switchRepeat = findViewById(R.id.switchRepeat)
    }

    private fun setupListeners() {
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        addIcon.setOnClickListener { showCategoryPopup() }
        findViewById<ImageButton>(R.id.addDueIcon).setOnClickListener { showDatePicker() }
        findViewById<ImageButton>(R.id.addTimeIcon).setOnClickListener { showTimePicker() }
        switchRepeat.setOnCheckedChangeListener { _, isChecked -> updateRepeatUI(isChecked) }
        findViewById<TextView>(R.id.tvCancel).setOnClickListener { clearFields(); togglePopupVisibility(false) }

        createButton.setOnClickListener {
            createTask()
        }
    }

    private fun showCategoryPopup() {
        PopupMenu(this, addIcon).apply {
            menuInflater.inflate(R.menu.popup_categories, menu)
            setOnMenuItemClickListener { menuItem ->
                tvList.text = when (menuItem.itemId) {
                    R.id.personal -> "Personal"
                    R.id.work -> "School"
                    R.id.school -> "Work"
                    R.id.wishlist -> "Wishlist"
                    else -> ""
                }
                true
            }
            show()
        }
    }

    private fun showDatePicker() {
        Calendar.getInstance().let { calendar ->
            DatePickerDialog(this, { _, year, month, day ->
                val selectedDateCalendar = Calendar.getInstance().apply {
                    set(year, month, day)
                }

                if (selectedDateCalendar.before(Calendar.getInstance())) {
                    Toast.makeText(this, "Selected date cannot be in the past", Toast.LENGTH_SHORT).show()
                } else {
                    tvDueDate.text = String.format("%04d/%02d/%02d", year, month + 1, day)
                }
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }
    }

    private fun showTimePicker() {
        Calendar.getInstance().let { calendar ->
            TimePickerDialog(this, { _, hourOfDay, minute ->
                // Create a Calendar object for the selected time
                val selectedTimeCalendar = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hourOfDay)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                }

                // Create a Calendar object for the current date with the selected time
                val currentDateTime = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hourOfDay)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                }

                // Compare selected time with current time
                if (selectedTimeCalendar.before(currentDateTime) && tvDueDate.text.isNotEmpty()) {
                    Toast.makeText(this, "Selected time cannot be in the past", Toast.LENGTH_SHORT).show()
                } else {
                    // Format the time for display
                    tvTimeReminder.text = String.format("%02d:%02d", hourOfDay, minute)
                }
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
        }
    }

    private fun updateRepeatUI(isChecked: Boolean) {
        findViewById<HorizontalScrollView>(R.id.hsvDaily).visibility = if (isChecked) View.VISIBLE else View.GONE
        findViewById<LinearLayout>(R.id.llButtonEnd).visibility = if (isChecked) View.VISIBLE else View.GONE
        findViewById<LinearLayout>(R.id.llBtn).visibility = if (isChecked) View.GONE else View.VISIBLE
        popupCardView.layoutParams.height = if (isChecked) 900 else 300
        findViewById<AppCompatButton>(R.id.btnRepeat).text = if (isChecked) "Yes" else "No"

        if (!isChecked) repeatDays.clear()
    }
    private fun createTask() {
        if (validateFields()) {
            // Combine selected date and time into a single Calendar object
            val endDateParts = tvDueDate.text.toString().split("/")
            val endTimeParts = tvTimeReminder.text.toString().split(":")

            val selectedEndDateTime = Calendar.getInstance().apply {
                set(Calendar.YEAR, endDateParts[0].toInt())
                set(Calendar.MONTH, endDateParts[1].toInt() - 1) // Month is 0-based
                set(Calendar.DAY_OF_MONTH, endDateParts[2].toInt())
                set(Calendar.HOUR_OF_DAY, endTimeParts[0].toInt())
                set(Calendar.MINUTE, endTimeParts[1].toInt())
                set(Calendar.SECOND, 0)
            }

            // Check if the selected end date and time are in the past
            if (selectedEndDateTime.before(Calendar.getInstance())) {
                Toast.makeText(this, "Selected end date and time cannot be in the past", Toast.LENGTH_SHORT).show()
                return
            }

            // Create request object with correct formats
            val createRequest = CreateRequest(
                task_name = etTaskName.text.toString(),
                task_description = etTaskDescription.text.toString(),
                end_date = String.format("%04d-%02d-%02d", selectedEndDateTime.get(Calendar.YEAR), selectedEndDateTime.get(Calendar.MONTH) + 1, selectedEndDateTime.get(Calendar.DAY_OF_MONTH)), // Corrected to YYYY-MM-DD
                end_time = String.format("%02d:%02d", selectedEndDateTime.get(Calendar.HOUR_OF_DAY), selectedEndDateTime.get(Calendar.MINUTE)), // Format to H:i
                repeat_days = if (switchRepeat.isChecked) repeatDays else null,
                category = tvList.text.toString()
            )

            CoroutineScope(Dispatchers.IO).launch {
                val response: Response<Task> = RetrofitClient.getApiService(this@AddTask2).createTask(createRequest)
                runOnUiThread {
                    if (response.isSuccessful) {
                        val createdTask = response.body()
                        val intent = Intent().apply {
                            putExtra("new_task", createdTask)
                        }
                        setResult(RESULT_OK, intent)
                        finish()
                    } else {
                        Toast.makeText(this@AddTask2, "Failed to create task: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }



    private fun validateFields(): Boolean {
        return when {
            etTaskName.text.isEmpty() -> {
                Toast.makeText(this, "Task name is required", Toast.LENGTH_SHORT).show()
                false
            }
            etTaskDescription.text.isEmpty() -> {
                Toast.makeText(this, "Task description is required", Toast.LENGTH_SHORT).show()
                false
            }
            tvDueDate.text.isEmpty() -> {
                Toast.makeText(this, "Due date is required", Toast.LENGTH_SHORT).show()
                false
            }
            tvTimeReminder.text.isEmpty() -> {
                Toast.makeText(this, "Time reminder is required", Toast.LENGTH_SHORT).show()
                false
            }
            tvList.text.isEmpty() -> {
                Toast.makeText(this, "Category is required", Toast.LENGTH_SHORT).show()
                false
            }
            else -> true
        }
    }

    private fun clearFields() {
        etTaskName.text.clear()
        etTaskDescription.text.clear()
        tvList.text = ""
        tvTimeReminder.text = ""
        tvDueDate.text = ""
        repeatDays.clear()
    }

    private fun togglePopupVisibility(isVisible: Boolean) {
        dimBackground.visibility = if (isVisible) View.VISIBLE else View.GONE
        popupCardView.visibility = if (isVisible) View.VISIBLE else View.GONE
    }
}
