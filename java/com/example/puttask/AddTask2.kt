package com.example.puttask

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import android.widget.PopupMenu
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.TextView
import java.util.Calendar

class AddTask2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_task2)

        // Inside your AddTask2 activity
        val addIcon: ImageView = findViewById(R.id.imListAdd)
        addIcon.setOnClickListener {
            // Create a PopupMenu
            val popupMenu = PopupMenu(this, addIcon)

            // Inflate the popup menu with your custom items
            popupMenu.menuInflater.inflate(R.menu.popup_categories, popupMenu.menu)

            // Set a click listener for the popup items (optional)
            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.personal -> {
                        // Handle Item 1 click
                        true
                    }
                    R.id.work -> {
                        // Handle Item 2 click
                        true
                    }
                    R.id.school -> {
                        // Handle Item 3 click
                        true
                    }
                    R.id.social -> {
                        // Handle Item 3 click
                        true
                    }
                    else -> false
                }
            }
            val addDueIcon: ImageView = findViewById(R.id.addDueIcon)
            val tvDueDate: TextView = findViewById(R.id.tvStartDate)  // TextView to show the due date
            val tvTimeReminder: TextView = findViewById(R.id.tvEndDate)  // TextView to show the time reminder

            addDueIcon.setOnClickListener {
                // Open DatePicker when the icon is clicked
                val calendar = Calendar.getInstance()
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH)
                val day = calendar.get(Calendar.DAY_OF_MONTH)

                val datePickerDialog = DatePickerDialog(
                    this,
                    { _, selectedYear, selectedMonth, selectedDay ->
                        // Set the selected date in TextView
                        tvDueDate.text = "$selectedDay/${selectedMonth + 1}/$selectedYear"

                        // Open TimePicker after the date is selected
                        val timePickerDialog = TimePickerDialog(
                            this,
                            { _, hourOfDay, minute ->
                                // Set the selected time in TextView
                                tvTimeReminder.text = "$hourOfDay:$minute"
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            true
                        )
                        timePickerDialog.show()

                    }, year, month, day
                )
                datePickerDialog.show()
            }

            // Show the popup menu
            popupMenu.show()
        }
    }
}
