package com.example.puttask.fragments

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
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
    private lateinit var tvDueDate: TextView
    private lateinit var tvTimeReminder: TextView
    private lateinit var createButton: AppCompatButton
    private lateinit var etTaskName: EditText
    private lateinit var etTaskDescription: EditText
    private lateinit var btnRepeat: AppCompatButton
    private lateinit var tvBack: TextView
    private lateinit var tvRepeatDays: TextView // TextView for displaying selected repeat days
    private val repeatDays = arrayOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    private var repeatDaysSelected = BooleanArray(repeatDays.size)
    private var selectedRepeatDays: List<String> = emptyList()

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
        createButton = findViewById(R.id.CreateButton)
        btnRepeat = findViewById(R.id.btnRepeat)
        tvBack = findViewById(R.id.tvBack)
        tvRepeatDays = findViewById(R.id.tvRepeat) // Initialize TextView for repeat days
    }

    private fun setupListeners() {
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { startActivity(Intent(this, MainActivity::class.java)) }
        addIcon.setOnClickListener { showCategoryPopup() }
        findViewById<ImageButton>(R.id.addDueIcon).setOnClickListener { showDatePicker() }
        findViewById<ImageButton>(R.id.addTimeIcon).setOnClickListener { showTimePicker() }
        btnRepeat.setOnClickListener {
            // Check if a due date is selected before proceeding
            if (tvDueDate.text.isEmpty()) {
                Toast.makeText(this, "Please select a due date before setting repeat days", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            // Check if the selected due date is at least 24 hours from now before allowing repeat days
            if (isDeadlineAtLeast24HoursAway()) {
                showRepeatDaysDialog { days ->
                    selectedRepeatDays = days // Update selected repeat days
                    tvRepeatDays.text = "Repeats on: ${selectedRepeatDays.joinToString(", ")}" // Display selected days
                }
            } else {
                Toast.makeText(this, "Repeat days are only available if the deadline is at least 24 hours away.", Toast.LENGTH_LONG).show()
            }
        }
        createButton.setOnClickListener { createTask() }
    }

    private fun isDeadlineAtLeast24HoursAway(): Boolean {
        val selectedDate = tvDueDate.text.toString()
        if (selectedDate.isEmpty()) return false

        val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
        val parsedDate = dateFormat.parse(selectedDate) ?: return false

        // Calculate 24 hours from now
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.HOUR_OF_DAY, 24)

        // Check if the selected date is at least 24 hours from now
        return parsedDate.after(calendar.time)
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

                val selectedDate = tvDueDate.text.toString()
                if (selectedDate.isNotEmpty()) {
                    val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
                    val parsedDate = dateFormat.parse(selectedDate)
                    val selectedDateCalendar = Calendar.getInstance().apply { time = parsedDate!! }

                    // If the selected date is today and the selected time is in the past, show a warning
                    if (selectedDateCalendar.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
                        selectedDateCalendar.get(Calendar.DAY_OF_YEAR) == calendar.get(Calendar.DAY_OF_YEAR) &&
                        selectedTimeCalendar.before(Calendar.getInstance())
                    ) {
                        Toast.makeText(this, "Selected time cannot be in the past", Toast.LENGTH_SHORT).show()
                        return@TimePickerDialog
                    }
                }

                tvTimeReminder.text = String.format("%02d:%02d", hourOfDay, minute)
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show()
        }
    }

    private fun showRepeatDaysDialog(onDaysSelected: (List<String>) -> Unit) {
        repeatDaysSelected = repeatDaysSelected.clone()

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select Repeat Days")

        builder.setMultiChoiceItems(repeatDays, repeatDaysSelected) { _, which, isChecked ->
            repeatDaysSelected[which] = isChecked
        }

        builder.setPositiveButton("OK") { dialog, _ ->
            val selectedDays = repeatDays.filterIndexed { index, _ -> repeatDaysSelected[index] }
            val tvRepeat = findViewById<TextView>(R.id.tvRepeat)

            if (selectedDays.isNotEmpty()) {
                onDaysSelected(selectedDays)
                btnRepeat.text = "Yes"
                tvRepeat.text = "Repeats on: ${selectedDays.joinToString(", ")}"
            } else {
                onDaysSelected(emptyList())
                tvRepeat.text = "No repeat days selected"
                Toast.makeText(this, "No repeat days selected", Toast.LENGTH_SHORT).show()
                btnRepeat.text = "No"
            }

            repeatDaysSelected = repeatDaysSelected.clone()
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        builder.create().show()
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
            repeat_days = selectedRepeatDays, // Pass the list of selected repeat days
            category = tvList.text.toString()
        )

        CoroutineScope(Dispatchers.IO).launch {
            val response: Response<Task> = RetrofitClient.getApiService(this@AddTask2).createTask(createRequest)
            runOnUiThread {
                if (response.isSuccessful) {
                    val createdTask = response.body()
                    val intent = Intent().apply {
                        putExtra("new_task", createdTask) // Pass the created task object
                    }
                    setResult(RESULT_OK, intent) // Set result code and intent containing the new task
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
