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
        tvRepeatDays = findViewById(R.id.tvRepeat)
    }

    private fun setupListeners() {
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { startActivity(Intent(this, MainActivity::class.java)) }
        addIcon.setOnClickListener { showCategoryPopup() }
        findViewById<ImageButton>(R.id.addDueIcon).setOnClickListener { showDatePicker() }
        findViewById<ImageButton>(R.id.addTimeIcon).setOnClickListener { showTimePicker() }
        btnRepeat.setOnClickListener { showRepeatDaysDialog { days ->
            selectedRepeatDays = days // Update selected repeat days
            tvRepeatDays.text = "Repeats on: ${selectedRepeatDays.joinToString(", ")}"
        }}
        createButton.setOnClickListener { createTask() }
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
            // Create the DatePickerDialog with the custom theme
            val datePickerDialog = DatePickerDialog(
                this, // Using 'this' for the Activity context
                R.style.DialogTheme, // Applying the custom theme
                { _, year, month, day ->
                    val selectedDateCalendar = Calendar.getInstance().apply {
                        set(year, month, day)
                    }
                    if (selectedDateCalendar.before(Calendar.getInstance())) {
                        Toast.makeText(this, "Selected date cannot be in the past", Toast.LENGTH_SHORT).show()
                    } else {
                        tvDueDate.text = String.format("%04d/%02d/%02d", year, month + 1, day)
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )

            // Set on show listener to customize text colors
            datePickerDialog.setOnShowListener {
                // Change the header text color
                val titleTextView = datePickerDialog.findViewById<TextView>(android.R.id.title)
                titleTextView?.setTextColor(resources.getColor(R.color.very_blue))

                // Change the button text color
                datePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE)?.setTextColor(resources.getColor(R.color.very_blue))
                datePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE)?.setTextColor(resources.getColor(R.color.very_blue))
            }

            // Show the dialog
            datePickerDialog.show()
        }
    }

    private fun showTimePicker() {
        Calendar.getInstance().let { calendar ->
            // Create the TimePickerDialog
            val timePickerDialog = TimePickerDialog(
                this,
                R.style.DialogTheme,
                { _, hourOfDay, minute ->
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
                    // Determine AM/PM and format the time
                    val amPm = if (hourOfDay >= 12) "PM" else "AM"
                    val hour12Format = if (hourOfDay % 12 == 0) 12 else hourOfDay % 12
                    tvTimeReminder.text = String.format("%02d:%02d %s", hour12Format, minute, amPm)
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false
            )

            // Set on show listener to customize button colors
            timePickerDialog.setOnShowListener {
                timePickerDialog.getButton(TimePickerDialog.BUTTON_POSITIVE)?.setTextColor(resources.getColor(R.color.very_blue))
                timePickerDialog.getButton(TimePickerDialog.BUTTON_NEGATIVE)?.setTextColor(resources.getColor(R.color.very_blue))
            }

            // Show the dialog
            timePickerDialog.show()
        }
    }


    private fun showRepeatDaysDialog(onDaysSelected: (List<String>) -> Unit) {
        // Clone the current selected state so it persists across dialog invocations
        repeatDaysSelected = repeatDaysSelected.clone()
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select Repeat Days")

        // Set the MultiChoiceItems with repeat days and their corresponding checked states
        builder.setMultiChoiceItems(repeatDays, repeatDaysSelected) { _, which, isChecked ->
            // Update the selected state when a day is checked or unchecked
            repeatDaysSelected[which] = isChecked
        }
        builder.setPositiveButton("OK") { dialog, _ ->
            // Get the list of selected days based on the updated repeatDaysSelected array
            val selectedDays = repeatDays.filterIndexed { index, _ -> repeatDaysSelected[index] }
            val tvRepeat = findViewById<TextView>(R.id.tvRepeat)

            // Update the UI based on whether any days were selected or not
            if (selectedDays.isNotEmpty()) {
                onDaysSelected(selectedDays) // Pass the selected days to the callback
                btnRepeat.text = "Yes"  // Set button text to "Yes" to indicate days are selected
                tvRepeat.text = "Repeats on: ${selectedDays.joinToString(", ")}" // Display selected days
            } else {
                onDaysSelected(emptyList())  // Clear the selection
                tvRepeat.text = "No repeat days selected"  // Display message indicating no days selected
                Toast.makeText(this, "No repeat days selected", Toast.LENGTH_SHORT).show()  // Show toast message
                btnRepeat.text = "No"  // Set button text to "No" to indicate no days are selected
            }
            // Save the selected state so it's remembered next time
            repeatDaysSelected = repeatDaysSelected.clone()
            dialog.dismiss()  // Close the dialog
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()  // Close the dialog without making changes
        }
        builder.create().show()  // Display the dialog
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
