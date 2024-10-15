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
    private lateinit var dimBackground: View
    private lateinit var popupCardView: CardView
    private lateinit var createButton: AppCompatButton
    private lateinit var etTaskName: EditText
    private lateinit var etTaskDescription: EditText

    // Newly added variable for repeat days
    private var repeatDays: MutableList<String> = mutableListOf()

    private var taskCallback: TaskCallback? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_task2)
        // Set the callback from the Intent
        taskCallback = intent.getParcelableExtra("task_callback") // Ensure to send this from the Lists fragment

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

                // Optionally validate that category is valid
                if (tvList.text.isEmpty()) {
                    Toast.makeText(this@AddTask2, "Please select a valid category", Toast.LENGTH_SHORT).show()
                }
                true
            }
            show()
        }
    }


    private fun showDatePicker() {
        Calendar.getInstance().let { calendar ->
            DatePickerDialog(this, { _, year, month, day ->
                tvDueDate.text = String.format("%04d/%02d/%02d", year, month + 1, day)
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }
    }

    private fun showTimePicker() {
        Calendar.getInstance().let { calendar ->
            TimePickerDialog(this, { _, hourOfDay, minute ->
                val selectedTime = String.format("%02d:%02d:00", hourOfDay, minute)
                val dateText = tvDueDate.text.toString()

                if (dateText.isNotEmpty()) {
                    tvTimeReminder.text = selectedTime

                    // Set end datetime (e.g., 1 hour after the start time)
                    val endCalendar = Calendar.getInstance()
                    endCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay + 1) // 1 hour later
                    endCalendar.set(Calendar.MINUTE, minute)

                    val endDateString = String.format("%04d-%02d-%02d %02d:%02d:00",
                        endCalendar.get(Calendar.YEAR),
                        endCalendar.get(Calendar.MONTH) + 1,
                        endCalendar.get(Calendar.DAY_OF_MONTH),
                        endCalendar.get(Calendar.HOUR_OF_DAY),
                        endCalendar.get(Calendar.MINUTE)
                    )

                    // Store the end datetime in a variable for later use
                    tvTimeReminder.text = endDateString
                } else {
                    Toast.makeText(this, "Please select a date first", Toast.LENGTH_SHORT).show()
                }
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show()
        }
    }


    private fun updateRepeatUI(isChecked: Boolean) {
        findViewById<HorizontalScrollView>(R.id.hsvDaily).visibility = if (isChecked) View.VISIBLE else View.GONE
        findViewById<LinearLayout>(R.id.llButtonEnd).visibility = if (isChecked) View.VISIBLE else View.GONE
        findViewById<LinearLayout>(R.id.llBtn).visibility = if (isChecked) View.GONE else View.VISIBLE
        popupCardView.layoutParams.height = if (isChecked) 900 else 300
        findViewById<AppCompatButton>(R.id.btnRepeat).text = if (isChecked) "Yes" else "No"

        // Clear previous selections for repeat days if unchecked
        if (!isChecked) {
            repeatDays.clear()
        }
    }

    private fun createTask() {
        // Ensure that both due date and time are set
        if (tvDueDate.text.isEmpty() || tvTimeReminder.text.isEmpty()) {
            Toast.makeText(this, "Please select both due date and time", Toast.LENGTH_SHORT).show()
            return
        }

        // Combine date and time into a single datetime string
        val startDateTimeString = "${tvDueDate.text} ${tvTimeReminder.text}"
        val endDateTimeString = "${tvDueDate.text} ${tvTimeReminder.text}" // Assume you set end time properly elsewhere

        // Parse the combined strings into Date objects
        val dateFormat = SimpleDateFormat("MM/dd/yyyy hh:mm a", Locale.getDefault()) // Adjust format to match your inputs
        val startDateTime = dateFormat.parse(startDateTimeString)
        val endDateTime = dateFormat.parse(endDateTimeString) // You should set a different end time for a valid comparison

        // Validate the parsed date objects
        if (startDateTime != null && endDateTime != null) {
            if (startDateTime.before(endDateTime)) {
                // Create request if date validations pass
                val createRequest = CreateRequest(
                    task_name = etTaskName.text.toString(),
                    task_description = etTaskDescription.text.toString(),
                    start_datetime = startDateTimeString,
                    end_datetime = endDateTimeString,
                    repeat_days = if (switchRepeat.isChecked) repeatDays else null,
                    category = tvList.text.toString()
                )

                // Launch coroutine to make API call
                CoroutineScope(Dispatchers.IO).launch {
                    val response: Response<Task> = RetrofitClient.getApiService(this@AddTask2).createTask(createRequest)
                    runOnUiThread {
                        if (response.isSuccessful) {
                            // Call the callback method in the Lists fragment
                            (taskCallback)?.onTaskCreated(response.body()!!) // Cast and call the method
                            clearFields()
                            navigateToMainActivity() // Navigate back to MainActivity
                        } else {
                            Toast.makeText(this@AddTask2, "Failed to create task: ${response.message()}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Start date/time must be before end date/time", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Invalid date/time selected", Toast.LENGTH_SHORT).show()
        }
    }


    private fun clearFields() {
        etTaskName.text.clear()
        etTaskDescription.text.clear()
        tvList.text = ""
        tvTimeReminder.text = ""
        tvDueDate.text = ""
        repeatDays.clear() // Clear repeat days selection
    }

    private fun togglePopupVisibility(isVisible: Boolean) {
        dimBackground.visibility = if (isVisible) View.VISIBLE else View.GONE
        popupCardView.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK) // Clear the activity stack
        startActivity(intent) // Navigate to MainActivity
        finish() // Optional: Finish the current activity
    }
}
