package com.example.puttask.fragments

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.cardview.widget.CardView
import com.example.puttask.R
import com.example.puttask.api.Task
import java.util.*

class AddTask2 : AppCompatActivity() {

    private lateinit var addIcon: ImageView
    private lateinit var tvList: TextView
    private lateinit var switchRepeat: Switch
    private lateinit var tvCancel: TextView
    private lateinit var tvDone: TextView
    private lateinit var tvDueDate: TextView
    private lateinit var tvTimeReminder: TextView
    private lateinit var dimBackground: View
    private lateinit var popupCardView: CardView
    private lateinit var llButtonEnd: LinearLayout
    private lateinit var llBtn: LinearLayout
    private lateinit var btnRepeat: AppCompatButton
    private lateinit var hsvDaily: HorizontalScrollView
    private val taskList: MutableList<Task> = mutableListOf()
    private var currentTaskIndex: Int? = null // Track current task index for update

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_task2)

        addIcon = findViewById(R.id.imListAdd)
        tvList = findViewById(R.id.tvList)
        tvDueDate = findViewById(R.id.tvStartDate)
        tvTimeReminder = findViewById(R.id.tvEndDate)
        dimBackground = findViewById(R.id.dimBackground)
        popupCardView = findViewById(R.id.popupCardView)
        llButtonEnd = findViewById(R.id.llButtonEnd)
        llBtn = findViewById(R.id.llBtn)
        btnRepeat = findViewById(R.id.btnRepeat)
        tvCancel = findViewById(R.id.tvCancel)
        tvDone = findViewById(R.id.tvDone)
        switchRepeat = findViewById(R.id.switchRepeat)
        hsvDaily = findViewById(R.id.hsvDaily)

        // Set up the PopupMenu for category selection
        addIcon.setOnClickListener {
            val dropdownMenu = PopupMenu(this, addIcon)
            dropdownMenu.menuInflater.inflate(R.menu.popup_categories, dropdownMenu.menu)
            dropdownMenu.setOnMenuItemClickListener { menuItem ->
                val category = when (menuItem.itemId) {
                    R.id.personal -> "Personal"
                    R.id.work -> "Work"
                    R.id.school -> "School"
                    R.id.social -> "Social"
                    else -> null
                }
                category?.let {
                    tvList.text = it
                    true
                } ?: false
            }
            dropdownMenu.show()
        }

        val calendar = Calendar.getInstance()
        findViewById<ImageButton>(R.id.addDueIcon).setOnClickListener {
            showDatePicker(calendar)
        }
        findViewById<ImageButton>(R.id.addTimeIcon).setOnClickListener {
            showTimePicker(calendar)
        }

        // Repeat switch logic
        popupCardView.visibility = View.GONE
        hsvDaily.visibility = View.GONE
        switchRepeat.setOnCheckedChangeListener { _, isChecked ->
            hsvDaily.visibility = if (isChecked) View.VISIBLE else View.GONE
            llButtonEnd.visibility = if (isChecked) View.VISIBLE else View.GONE
            llBtn.visibility = if (isChecked) View.GONE else View.VISIBLE
            popupCardView.layoutParams.height = if (isChecked) 900 else 300
            if (isChecked){
                btnRepeat.text = "Yes"
            }
            else{
                btnRepeat.text = "No"
            }
        }

        // Popup visibility handling
        findViewById<TextView>(R.id.tvBack).setOnClickListener { visibilityChecker() }
        tvCancel.setOnClickListener { clearFields(); visibilityChecker(); switchRepeat.isChecked = false }
        tvDone.setOnClickListener { handleTaskAction() }

        btnRepeat.setOnClickListener{
            visibilityChecker()
            popupCardView.visibility = View.VISIBLE

        }
    }

    private fun showDatePicker(calendar: Calendar) {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            tvDueDate.text = "$selectedDay/${selectedMonth + 1}/$selectedYear"
        }, year, month, day)

        datePickerDialog.show()
    }

    private fun showTimePicker(calendar: Calendar) {
        val timePickerDialog = TimePickerDialog(this, { _, hourOfDay, minute ->
            val amPm = if (hourOfDay >= 12) "PM" else "AM"
            val hour = if (hourOfDay > 12) hourOfDay - 12 else if (hourOfDay == 0) 12 else hourOfDay
            tvTimeReminder.text = String.format("%02d:%02d %s", hour, minute, amPm)
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false)

        timePickerDialog.show()
    }

    private fun handleTaskAction() {
        if (currentTaskIndex != null) {
            updateTask()
        } else {
            createTask()
        }
        clearFields()
        visibilityChecker()
    }

    private fun createTask() {
        val title = tvList.text.toString()
        val description = "Your task description" // Update this to get a real description from user input
        val time = tvTimeReminder.text.toString()
        val repeatDays = getSelectedRepeatDays()

        // Create the task and add to the list
        val newTask = Task(0, title, description, time, "", repeatDays, false) // Set ID as 0 for new task
        taskList.add(newTask)
        Toast.makeText(this, "Task created", Toast.LENGTH_SHORT).show()
    }

    private fun updateTask() {
        currentTaskIndex?.let { index ->
            val title = tvList.text.toString()
            val description = "Your task description" // Update this to get a real description from user input
            val time = tvTimeReminder.text.toString()
            val repeatDays = getSelectedRepeatDays()

            // Update the task
            taskList[index] = Task(taskList[index].id, title, description, time, "", repeatDays, false) // Keep the same ID
            Toast.makeText(this, "Task updated", Toast.LENGTH_SHORT).show()
            currentTaskIndex = null // Reset index after updating
        }
    }

    private fun getSelectedRepeatDays(): List<String> {
        // Implement logic to get selected repeat days based on your UI (e.g., checkboxes)
        return listOf() // Replace with actual selected days
    }

    private fun clearFields() {
        tvList.text = ""
        tvTimeReminder.text = ""
        tvDueDate.text = ""
        }

    // Function to handle popup visibility
    private fun visibilityChecker() {
        dimBackground.visibility = if (dimBackground.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        popupCardView.visibility = if (popupCardView.visibility == View.VISIBLE) View.GONE else View.VISIBLE
    }
}
