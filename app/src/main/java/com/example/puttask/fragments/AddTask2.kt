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

    //private var taskCallback: TaskCallback? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_task2)
        // Set the callback from the Intent
        //taskCallback = intent.getParcelableExtra("task_callback") // Esure to send this from the Listsn fragment

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
                // Create a calendar instance for the selected date
                val selectedDateCalendar = Calendar.getInstance().apply {
                    set(year, month, day)
                }

                // Get the current date for comparison
                val currentDate = Calendar.getInstance()

                // Check if the selected date is in the past
                if (selectedDateCalendar.before(currentDate)) {
                    Toast.makeText(this, "Selected date cannot be in the past", Toast.LENGTH_SHORT).show()
                } else {
                    // Format and display the selected date
                    tvDueDate.text = String.format("%04d/%02d/%02d", year, month + 1, day)
                }
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }
    }


    private fun showTimePicker() {
        Calendar.getInstance().let { calendar ->
            TimePickerDialog(this, { _, hourOfDay, minute ->
                // Format the selected time as HH:mm (only time)
                val selectedTime = String.format("%02d:%02d", hourOfDay, minute)

                // Display the selected time in the TextView
                tvTimeReminder.text = selectedTime
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
        val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
        val startDateTime = dateFormat.parse(startDateTimeString)
        val endDateTime = dateFormat.parse(endDateTimeString) // You should set a different end time for a valid comparison

        // Validate the parsed date objects
        // Validate that both end date and time are set
        if (tvDueDate.text.isNotEmpty() && tvTimeReminder.text.isNotEmpty()) {
            // Combine end date and time into a single datetime string
            val endDateTimeString = "${tvDueDate.text} ${tvTimeReminder.text}"

            // Parse the combined strings into Date objects
            val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()) // Adjust format to match your inputs
            val endDateTime = dateFormat.parse(endDateTimeString)

            // Validate the parsed endDateTime object
            if (endDateTime != null) {
                // Create request if validations pass
                val createRequest = CreateRequest(
                    task_name = etTaskName.text.toString(),
                    task_description = etTaskDescription.text.toString(),
                    end_date = tvDueDate.text.toString(), // Send end date directly
                    end_time = tvTimeReminder.text.toString(), // Send end time directly
                    repeat_days = if (switchRepeat.isChecked) repeatDays else null,
                    category = tvList.text.toString()
                )

                //fuck this shit

                // Launch coroutine to make API call
                CoroutineScope(Dispatchers.IO).launch {
                    val response: Response<Task> = RetrofitClient.getApiService(this@AddTask2).createTask(createRequest)
                    runOnUiThread {
                        if (response.isSuccessful) {
                            //taskCallback?.onTaskCreated(response.body()!!) // Check for null or handle it
                            clearFields()
                            navigateToMainActivity()
                        } else {
                            Toast.makeText(this@AddTask2, "Failed to create task: ${response.message()}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Invalid date/time selected", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Please select both end date and time", Toast.LENGTH_SHORT).show()
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
        val intent = Intent(this, Lists::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK) // Clear the activity stack
        startActivity(intent) // Navigate to MainActivity
        finish() // Optional: Finish the current activity
    }
}
