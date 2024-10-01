package com.example.puttask.fragments

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.cardview.widget.CardView
import com.example.puttask.R
import java.util.*

class AddTask2 : AppCompatActivity() {

    private lateinit var selectedImageView: ImageView
    private lateinit var imageContainer: LinearLayout
    private lateinit var addIcon: ImageView
    private lateinit var tvList: TextView
    private lateinit var switchRepeat: Switch
    private lateinit var tvBack: TextView
    private lateinit var tvCancel: TextView
    private lateinit var tvDone: TextView
    private lateinit var btnRepeatt: AppCompatButton
    private lateinit var btnattach: ImageButton
    private lateinit var tvDueDate: TextView
    private lateinit var addDueIcon: ImageButton
    private lateinit var tvTimeReminder: TextView
    private lateinit var addTimeIcon: ImageButton
    private lateinit var dimBackground: View
    private lateinit var popupCardView: CardView
    private lateinit var btnRepeat: AppCompatButton
    private lateinit var llButtonEnd: LinearLayout
    private lateinit var llBtn: LinearLayout
    private lateinit var llDaily: LinearLayout
    private lateinit var hsvDaily: HorizontalScrollView
    private lateinit var radioGroup: RadioGroup
    private val imageUris: MutableList<Uri> = mutableListOf()
    private val maxImages = 10
    private var lastcheckedRadioButton: RadioButton? = null

    // Declare ActivityResultLauncher for image picking
    private lateinit var pickImageLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_task2)

        // Initialize views
        addIcon = findViewById(R.id.imListAdd)
        tvList = findViewById(R.id.tvList)
        selectedImageView = findViewById(R.id.selectedImageView)
        imageContainer = findViewById(R.id.imageContainer)
        addDueIcon = findViewById(R.id.addDueIcon)
        tvDueDate = findViewById(R.id.tvStartDate)
        tvTimeReminder = findViewById(R.id.tvEndDate)
        addTimeIcon = findViewById(R.id.addTimeIcon)
        btnattach = findViewById(R.id.btnattach)
        dimBackground = findViewById(R.id.dimBackground)
        popupCardView = findViewById(R.id.popupCardView)
        btnRepeat = findViewById(R.id.btnRepeat)
        switchRepeat = findViewById(R.id.switchRepeat)
        btnRepeatt = findViewById(R.id.btnRepeat)
        llButtonEnd = findViewById(R.id.llButtonEnd)
        llBtn = findViewById(R.id.llBtn)
        llDaily = findViewById(R.id.llDaily)
        hsvDaily = findViewById(R.id.hsvDaily)
        radioGroup = findViewById(R.id.radioGroup)
        tvCancel = findViewById(R.id.tvCancel)
        tvDone = findViewById(R.id.tvDone)
        tvBack = findViewById(R.id.tvBack)

        // Initialize the ActivityResultLauncher for picking images
        pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                val uri: Uri? = result.data?.data
                if (uri != null && imageUris.size < maxImages) {
                    imageUris.add(uri)
                    addImageView(uri)
                } else {
                    Toast.makeText(this, "You can only attach up to $maxImages images.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Set up the PopupMenu for category selection
        addIcon.setOnClickListener {
            val dropdownMenu = PopupMenu(this, addIcon)
            dropdownMenu.menuInflater.inflate(R.menu.popup_categories, dropdownMenu.menu)

            val menuMap = mapOf(
                R.id.personal to "Personal",
                R.id.work to "Work",
                R.id.school to "School",
                R.id.social to "Social"
            )

            dropdownMenu.setOnMenuItemClickListener { menuItem ->
                menuMap[menuItem.itemId]?.let {
                    tvList.text = it
                    true
                } ?: false
            }
            dropdownMenu.show()
        }

        // Date and Time Picker logic
        val calendar = Calendar.getInstance()
        addDueIcon.setOnClickListener {
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                tvDueDate.text = "$selectedDay/${selectedMonth + 1}/$selectedYear"

                val timePickerDialog = TimePickerDialog(this, { _, hourOfDay, minute ->
                    val amPm = if (hourOfDay >= 12) "PM" else "AM"
                    val hour = if (hourOfDay > 12) hourOfDay - 12 else if (hourOfDay == 0) 12 else hourOfDay
                    tvTimeReminder.text = String.format("%02d:%02d %s", hour, minute, amPm)
                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false)

                timePickerDialog.show()
            }, year, month, day)

            datePickerDialog.show()
        }

        addTimeIcon.setOnClickListener {
            val timePickerDialog = TimePickerDialog(this, { _, hourOfDay, minute ->
                val amPm = if (hourOfDay >= 12) "PM" else "AM"
                val hour = if (hourOfDay > 12) hourOfDay - 12 else if (hourOfDay == 0) 12 else hourOfDay
                tvTimeReminder.text = String.format("%02d:%02d %s", hour, minute, amPm)
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false)

            timePickerDialog.show()
        }

        // Repeat switch logic
        switchRepeat.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                btnRepeatt.text = "YES"
                hsvDaily.visibility = View.VISIBLE
                llButtonEnd.visibility = View.VISIBLE
                llDaily.visibility = View.VISIBLE
                llBtn.visibility = View.GONE
                popupCardView.layoutParams.height = 900
            } else {
                btnRepeatt.text = "NO"
                hsvDaily.visibility = View.GONE
                llButtonEnd.visibility = View.GONE
                llDaily.visibility = View.GONE
                llBtn.visibility = View.VISIBLE
                popupCardView.layoutParams.height = 300
            }
        }

        // Radio button selection handling
        for (i in 0 until radioGroup.childCount) {
            val linearLayout = radioGroup.getChildAt(i) as LinearLayout
            val radioButton = linearLayout.getChildAt(1) as RadioButton
            radioButton.setOnClickListener { handleRadioButtonSelection(radioButton) }
        }

        // Attach image logic
        btnattach.setOnClickListener {
            if (imageUris.size < maxImages) {
                openFileChooser()
            } else {
                Toast.makeText(this, "You can only attach up to $maxImages images.", Toast.LENGTH_SHORT).show()
            }
        }

        // Popup visibility handling
        tvBack.setOnClickListener { visibilityChecker() }
        tvCancel.setOnClickListener { visibilityChecker(); switchRepeat.isChecked = false }
        tvDone.setOnClickListener { visibilityChecker() }
    }

    // Function to handle popup visibility
    private fun visibilityChecker() {
        val dimBackground = findViewById<View>(R.id.dimBackground)
        val popupCardView = findViewById<CardView>(R.id.popupCardView)
        val isPopupVisible = popupCardView.visibility == View.VISIBLE
        dimBackground.visibility = if (isPopupVisible) View.GONE else View.VISIBLE
        popupCardView.visibility = if (isPopupVisible) View.GONE else View.VISIBLE
    }

    // Open the image file chooser
    private fun openFileChooser() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
        }
        pickImageLauncher.launch(intent)
    }

    // Add the selected image to the container
    private fun addImageView(imageUri: Uri) {
        val imageView = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(700, 300).apply {
                setMargins(200, 25, 0, 25)
            }
            setImageURI(imageUri)
            scaleType = ImageView.ScaleType.CENTER_CROP
        }

        if (imageContainer.visibility == View.GONE) {
            imageContainer.visibility = View.VISIBLE
        }
        imageContainer.addView(imageView)
    }

    // Handle radio button selection
    private fun handleRadioButtonSelection(radioButton: RadioButton) {
        lastcheckedRadioButton?.isChecked = false
        lastcheckedRadioButton = if (lastcheckedRadioButton == radioButton) {
            null
        } else {
            radioButton.isChecked = true
            radioButton
        }
    }
}
