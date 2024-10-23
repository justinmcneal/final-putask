package com.example.puttask.fragments

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.example.puttask.MainActivity
import com.example.puttask.R
import com.example.puttask.api.CreateRequest
import com.example.puttask.api.RetrofitClient
import com.example.puttask.api.Task
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class AddTask2 : AppCompatActivity() {

    private lateinit var addIcon: ImageView
    private lateinit var tvList: TextView
    private lateinit var switchRepeat: Switch
    private lateinit var tvDueDate: TextView
    private lateinit var tvTimeReminder: TextView
    private lateinit var tvRepeat: TextView
    private lateinit var createButton: AppCompatButton
    private lateinit var etTaskName: EditText
    private lateinit var etTaskDescription: EditText
    private lateinit var btnRepeat: AppCompatButton
    private lateinit var tvBack: TextView
    private val repeatDays = arrayOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    private var repeatDaysSelected = BooleanArray(repeatDays.size)
    private var selectedRepeatDays: List<String>? = null

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
        tvRepeat = findViewById(R.id.tvRepeat)
        createButton = findViewById(R.id.CreateButton)
        switchRepeat = findViewById(R.id.switchRepeat)
        btnRepeat = findViewById(R.id.btnRepeat)
        tvBack = findViewById(R.id.tvBack)
    }

    private fun setupListeners() {
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { startActivity(Intent(this, MainActivity::class.java)) }
        addIcon.setOnClickListener { showCategoryPopup() }
        findViewById<ImageButton>(R.id.addDueIcon).setOnClickListener { showDatePicker() }
        findViewById<ImageButton>(R.id.addTimeIcon).setOnClickListener { showTimePicker() }
        btnRepeat.setOnClickListener { showRepeatDaysDialog { selectedDays -> selectedRepeatDays = selectedDays; updateRepeatUI() } }
        createButton.setOnClickListener { createTask() }
    }
    private fun setupRepeatDays() {
        // Pass a lambda to handle the selected days
        showRepeatDaysDialog { selectedDays ->
            // Do something with the selectedDays, for example:
            Toast.makeText(this, "Selected Days: $selectedDays", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showCategoryPopup() {
        PopupMenu(this, addIcon).apply {
            menuInflater.inflate(R.menu.popup_categories, menu)
            setOnMenuItemClickListener { menuItem ->
                tvList.text = when (menuItem.itemId) {
                    R.id.personal -> "Personal"
                    R.id.school -> "School"
                    R.id.work -> "Work"
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
                val selectedTimeCalendar = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hourOfDay)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                }

                // Parse the selected date from the `tvDueDate` text view
                val selectedDate = tvDueDate.text.toString()
                val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
                val today = Calendar.getInstance()

                // Only allow the time validation if the date is today
                if (selectedDate.isNotEmpty()) {
                    val parsedDate = dateFormat.parse(selectedDate)
                    val selectedDateCalendar = Calendar.getInstance().apply { time = parsedDate!! }

                    // If the selected date is today and the selected time is in the past, show a warning
                    if (selectedDateCalendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                        selectedDateCalendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) &&
                        selectedTimeCalendar.before(Calendar.getInstance())
                    ) {
                        Toast.makeText(this, "Selected time cannot be in the past", Toast.LENGTH_SHORT).show()
                        return@TimePickerDialog
                    }
                }

                // Update the time if the time is valid
                tvTimeReminder.text = String.format("%02d:%02d", hourOfDay, minute)
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show()
        }
    }

    private fun showRepeatDaysDialog(onDaysSelected: (List<String>) -> Unit) {
        // The dialog will use the currently stored repeatDaysSelected array
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select Repeat Days")

        // Set multi-choice items and listen to changes
        builder.setMultiChoiceItems(repeatDays, repeatDaysSelected) { _, which, isChecked ->
            repeatDaysSelected[which] = isChecked // Update the stored state when an item is checked/unchecked
        }

        // Handle "OK" button press
        builder.setPositiveButton("OK") { dialog, _ ->
            // Get the selected days based on the stored state
            val selectedDays = repeatDays.filterIndexed { index, _ -> repeatDaysSelected[index] }
            onDaysSelected(selectedDays) // Pass selected days to the callback
            dialog.dismiss()
        }

        // Handle "Cancel" button press
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }

        builder.create().show()
    }

    private fun updateRepeatUI() {
        if (selectedRepeatDays.isNullOrEmpty()) {
            tvRepeat.text = "No repeat days selected" // Default message when no days are selected
            btnRepeat.text = "No" // Change button text to "No" when no days are selected

        } else {
            tvRepeat.text = selectedRepeatDays?.joinToString(", ")
            btnRepeat.text = "Yes" // Change button text to "YEs" when no days are selected

        }
    }

    private fun createTask() {
        if (!validateFields()) return

        val endDateParts = tvDueDate.text.toString().split("/")
        val endTimeParts = tvTimeReminder.text.toString().split(":")
        val selectedEndDateTime = Calendar.getInstance().apply {
            set(Calendar.YEAR, endDateParts[0].toInt())
            set(Calendar.MONTH, endDateParts[1].toInt() - 1)
            set(Calendar.DAY_OF_MONTH, endDateParts[2].toInt())
            set(Calendar.HOUR_OF_DAY, endTimeParts[0].toInt())
            set(Calendar.MINUTE, endTimeParts[1].toInt())
            set(Calendar.SECOND, 0)
        }

        if (selectedEndDateTime.before(Calendar.getInstance())) {
            Toast.makeText(this, "Selected end date and time cannot be in the past", Toast.LENGTH_SHORT).show()
            return
        }

        val createRequest = CreateRequest(
            task_name = etTaskName.text.toString(),
            task_description = etTaskDescription.text.toString(),
            end_date = String.format("%04d-%02d-%02d", selectedEndDateTime.get(Calendar.YEAR), selectedEndDateTime.get(Calendar.MONTH) + 1, selectedEndDateTime.get(Calendar.DAY_OF_MONTH)),
            end_time = String.format("%02d:%02d", selectedEndDateTime.get(Calendar.HOUR_OF_DAY), selectedEndDateTime.get(Calendar.MINUTE)),
            repeat_days = if (switchRepeat.isChecked) selectedRepeatDays else null,
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

    private fun validateFields(): Boolean {
        if (etTaskName.text.isEmpty()) {
            etTaskName.error = "Please enter a task name"
            return false
        }
        if (tvDueDate.text.isEmpty()) {
            Toast.makeText(this, "Please select a due date", Toast.LENGTH_SHORT).show()
            return false
        }
        if (tvTimeReminder.text.isEmpty()) {
            Toast.makeText(this, "Please select a reminder time", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
}
