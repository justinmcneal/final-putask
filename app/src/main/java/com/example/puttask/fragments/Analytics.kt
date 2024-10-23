package com.example.puttask.fragments

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.example.puttask.R
import com.example.puttask.api.CompleteTaskRequest
import com.example.puttask.api.RetrofitClient
import com.example.puttask.api.Task
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class Analytics : Fragment(R.layout.fragment_analytics) {

    private lateinit var lineChart: LineChart
    private lateinit var tvsevenDays: TextView
    private lateinit var tvtwentyeightDays: TextView
    private lateinit var tvsixtyDays: TextView
    private lateinit var tvthreesixtyfiveDays: TextView
    private lateinit var tvTaskOverviewDate: TextView
    private lateinit var tvPendingTasksCount: TextView
    private val entries = ArrayList<Entry>()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize UI components
        lineChart = view.findViewById(R.id.lineChart)
        tvsevenDays = view.findViewById(R.id.tvsevenDays)
        tvtwentyeightDays = view.findViewById(R.id.tvtwentyeightDays)
        tvsixtyDays = view.findViewById(R.id.tvsixtyDays)
        tvthreesixtyfiveDays = view.findViewById(R.id.tvthreesixtyfiveDays)
        tvTaskOverviewDate = view.findViewById(R.id.tvTaskOverviewDate)
        tvPendingTasksCount = view.findViewById(R.id.tvPendingTasksCount)


        fetchPendingTasks()

        // Set up click listeners for the buttons
        tvsevenDays.setOnClickListener {
            updateChart(7, "Last 7 Days Data")
            highlightButton(tvsevenDays)
        }
        tvtwentyeightDays.setOnClickListener {
            updateChart(28, "Last 28 Days Data")
            highlightButton(tvtwentyeightDays)
        }
        tvsixtyDays.setOnClickListener {
            updateChart(60, "Last 60 Days Data")
            highlightButton(tvsixtyDays)
        }
        tvthreesixtyfiveDays.setOnClickListener {
            updateChart(365, "Last 365 Days Data")
            highlightButton(tvthreesixtyfiveDays)
        }
    }

    private fun fetchPendingTasks() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response: Response<List<Task>> = RetrofitClient.getApiService(requireContext()).getAllTasks()
                if (response.isSuccessful) {
                    val tasks = response.body()
                    val pendingTasks = tasks?.filter { it.isChecked == false }?.size ?: 0 // Assuming `is_completed` is a Boolean field
                    // Update UI on main thread
                    CoroutineScope(Dispatchers.Main).launch {
                        tvPendingTasksCount.text = "Pending Tasks: $pendingTasks"
                    }
                } else {
                    showError("Failed to fetch tasks: ${response.message()}")

                }
            } catch (e: Exception) {
                showError("An error occurred: ${e.message}")
            }
        }
    }


    private fun markTaskComplete(task: Task) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Make the API call to update the task's status
                val response = RetrofitClient.getApiService(requireContext())
                    .markTaskComplete(task.id, CompleteTaskRequest(task.isChecked))

                if (response.isSuccessful) {
                    val updatedTask = response.body()

                    // Update the task list or UI
                    withContext(Dispatchers.Main) {
                        showSuccess("Task marked as ${if (task.isChecked) "complete" else "incomplete"}")
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        showError("Failed to update task status: ${response.message()}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showError("Error occurred: ${e.message}")
                }
            }
        }
    }




    private fun showError(message: String) {
        CoroutineScope(Dispatchers.Main).launch {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun showSuccess(message: String) {
        CoroutineScope(Dispatchers.Main).launch {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }



    // Generic function to update the chart for any range of days
    private fun updateChart(days: Int, label: String) {
        lineChart.clear()
        entries.clear()

        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())

        // Format the current date
        val currentDate = dateFormat.format(calendar.time)

        // Subtract the given number of days
        calendar.add(Calendar.DAY_OF_YEAR, -days)
        val startDate = dateFormat.format(calendar.time)

        // Set the date range in tvTaskOverviewDate
        tvTaskOverviewDate.text = "$startDate - $currentDate"

        // Create data entries for the past 'days' days
        for (i in 0 until days) {
            val percentageDataPoint = (Math.random() * 100).toFloat()
            entries.add(Entry(i.toFloat(), percentageDataPoint))
        }

        // Create and customize dataset
        val dataSet = LineDataSet(entries, label)
        dataSet.color = resources.getColor(android.R.color.holo_blue_light)
        dataSet.lineWidth = 2f
        dataSet.setDrawCircles(true)
        dataSet.setDrawValues(false)

        // Set data to the chart
        val lineData = LineData(dataSet)
        lineChart.data = lineData

        // Set x-axis value formatter
        lineChart.xAxis.valueFormatter = object : ValueFormatter() {
            private val dateFormat = SimpleDateFormat("MM/dd", Locale.getDefault())

            override fun getFormattedValue(value: Float): String {
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.DAY_OF_MONTH, -value.toInt())
                return dateFormat.format(calendar.time)
            }
        }

        // Customize chart appearance
        lineChart.xAxis.labelRotationAngle = -45f
        lineChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        lineChart.axisRight.isEnabled = false
        lineChart.invalidate() // Refresh the chart with new data
    }

    // Function to highlight the clicked button
    private fun highlightButton(selectedButton: TextView) {
        selectedButton.setBackgroundColor(Color.parseColor("#FFBB86FC")) // Change color to purple or any desired color
    }
}
