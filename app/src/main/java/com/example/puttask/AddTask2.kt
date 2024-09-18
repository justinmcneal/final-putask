package com.example.puttask

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.SwitchCompat
import androidx.cardview.widget.CardView
import java.util.Calendar

class AddTask2 : AppCompatActivity() {
    private lateinit var selectedImageView: ImageView
    private lateinit var imageContainer : LinearLayout
    private lateinit var addIcon : ImageView
    private lateinit var tvList : TextView
    private lateinit var switchRepeat : SwitchCompat
    private lateinit var tvCancel :TextView

    private lateinit var tvDone : TextView
    private lateinit var btnRepeatt : AppCompatButton
    private lateinit var btnattach : ImageButton
    private lateinit var tvDueDate : TextView
    private lateinit var addDueIcon :ImageButton
    private lateinit var tvTimeReminder : TextView
    private lateinit var addTimeIcon : ImageButton
    private lateinit var dimBackground : View
    private lateinit var popupCardView : CardView
    private lateinit var btnRepeat : AppCompatButton
    private val imageUris: MutableList<Uri> = mutableListOf()  // List to store image URIs
    private val maxImages = 10


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_task2)


        addIcon = findViewById(R.id.imListAdd)
        tvList = findViewById(R.id.tvList)
        addIcon.setOnClickListener {
            val dropdownMenu = PopupMenu(this, addIcon)
            dropdownMenu.menuInflater.inflate(R.menu.popup_categories, dropdownMenu.menu)

            dropdownMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.personal -> {
                        // Display a message or update UI for "Personal"
                        tvList.text = "Personal"
                        true
                    }

                    R.id.work -> {
                        // Display a message or update UI for "Work"
                        tvList.text = "Work"
                        true
                    }

                    R.id.school -> {
                        // Display a message or update UI for "School"
                        tvList.text = "School"
                        true
                    }

                    R.id.social -> {
                        // Display a message or update UI for "Social"
                        tvList.text = "Social"
                        true
                    }

                    else -> false
                }
            }
            dropdownMenu.show()
        }
        val calendar = Calendar.getInstance()

        // Date and Time Picker logic
        addDueIcon = findViewById(R.id.addDueIcon)
        tvDueDate = findViewById(R.id.tvStartDate)  // TextView to show the due date
        tvTimeReminder = findViewById(R.id.tvEndDate)  // TextView to show the time reminder

        addDueIcon.setOnClickListener {
            // Open DatePicker when the icon is clicked
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
                            // Determine AM/PM
                            val amPm = if (hourOfDay >= 12) "PM" else "AM"
                            // Convert hour into 12-hour format
                            val hour = if (hourOfDay > 12) hourOfDay - 12 else if (hourOfDay == 0) 12 else hourOfDay
                            // Set the selected time with AM/PM
                            tvTimeReminder.text = String.format("%02d:%02d %s", hour, minute, amPm)
                        },
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE),
                        false
                    )
                    timePickerDialog.show()
                }, year, month, day
            )
            datePickerDialog.show()
        }

        addTimeIcon= findViewById(R.id.addTimeIcon)
        addTimeIcon.setOnClickListener {
            val timePickerDialog = TimePickerDialog(
                this,
                { _, hourOfDay, minute ->
                    // Determine AM/PM
                    val amPm = if (hourOfDay >= 12) "PM" else "AM"
                    // Convert hour into 12-hour format
                    val hour = if (hourOfDay > 12) hourOfDay - 12 else if (hourOfDay == 0) 12 else hourOfDay
                    // Set the selected time with AM/PM
                    tvTimeReminder.text = String.format("%02d:%02d %s", hour, minute, amPm)
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false
            )
            timePickerDialog.show()
        }


        dimBackground = findViewById(R.id.dimBackground)
        popupCardView = findViewById(R.id.popupCardView)
        btnRepeat = findViewById(R.id.btnRepeat)
        dimBackground.visibility = View.GONE
        popupCardView.visibility = View.GONE

        btnRepeat.setOnClickListener {
            visibilityChecker()
        }

        switchRepeat = findViewById(R.id.switchRepeat)
         btnRepeatt = findViewById(R.id.btnRepeat)

        // SwitchCompat functionality
        switchRepeat.setOnCheckedChangeListener { _, isCheck ->
            if (isCheck) {

                btnRepeatt.text = "YES"
            } else {
                // The switch is OFF
                btnRepeatt.text = "NO"
            }
        }

        tvCancel = findViewById(R.id.tvCancel)
        tvDone = findViewById(R.id.tvDone)

        tvCancel.setOnClickListener{
            visibilityChecker()


        }
        tvDone.setOnClickListener{
            visibilityChecker()


        }
         selectedImageView = findViewById(R.id.selectedImageView)
        selectedImageView.visibility = View.GONE
        imageContainer = findViewById(R.id.imageContainer)


        btnattach = findViewById(R.id.btnattach)
        btnattach.setOnClickListener {
            if (imageUris.size < maxImages) {
                openFileChooser()
            } else {
                Toast.makeText(
                    this,
                    "You can only attach up to $maxImages images.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    var popUp = false
    private fun visibilityChecker(){
        val dimBackground = findViewById<View>(R.id.dimBackground)
        val popupCardView = findViewById<CardView>(R.id.popupCardView)
        popUp = !popUp
        if (popUp) {
            dimBackground.visibility = View.VISIBLE
            popupCardView.visibility = View.VISIBLE
        } else {
            dimBackground.visibility = View.GONE
            popupCardView.visibility = View.GONE
        }

    }
    private fun openFileChooser() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"  // To pick only images
        }
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE)
    }

    private fun addImageView(imageUri: Uri) {
        val imageView = ImageView(this)
        val layoutParams = LinearLayout.LayoutParams(
            700,
            300  // Set a fixed height for each image
        ).apply {
            setMargins(200, 25, 0, 25)  // Add margins between images
        }
        imageView.layoutParams = layoutParams
        imageView.setImageURI(imageUri)
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP

        if (imageContainer.visibility == View.GONE) {
            imageContainer.visibility = View.VISIBLE
        }
        imageContainer.addView(imageView)  // Add the ImageView to the container
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == RESULT_OK) {
            val uri: Uri? = data?.data
            if (uri != null) {
                if (imageUris.size < maxImages) {
                    imageUris.add(uri)
                    addImageView(uri)  // Display the selected image
                } else {
                    Toast.makeText(this, "You can only attach up to $maxImages images.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    companion object {
        const val REQUEST_CODE_PICK_IMAGE = 1
    }
}