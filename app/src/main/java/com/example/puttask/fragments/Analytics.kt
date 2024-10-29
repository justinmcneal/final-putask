package com.example.puttask.fragments

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.puttask.R
import com.example.puttask.api.RetrofitClient
import com.example.puttask.api.Task
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class Analytics : Fragment(R.layout.fragment_analytics) {

    private lateinit var lineChart: LineChart
    private lateinit var tvTaskOverviewDate: TextView
    private lateinit var tvCompletedTasksCount: TextView
    private lateinit var tvPendingTasksCount: TextView
    private lateinit var tvOverdueTasksCount: TextView
    private lateinit var tvCreatedTasksCount: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeViews(view)
        fetchTaskData()
    }

    private fun initializeViews(view: View) {
        lineChart = view.findViewById(R.id.lineChart)
        tvCompletedTasksCount = view.findViewById(R.id.tvCompletedTasksCount)
        tvPendingTasksCount = view.findViewById(R.id.tvPendingTasksCount)
        tvOverdueTasksCount = view.findViewById(R.id.tvOverdueTasksCount)
        tvCreatedTasksCount = view.findViewById(R.id.tvCreatedTasksCount)
    }

    private fun fetchTaskData() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = getTasksFromApi()
                if (response.isSuccessful) {
                    val tasks = response.body() ?: emptyList()
                    withContext(Dispatchers.Main) {
                        updateTaskStatusCounts(tasks)
                        setupLineChart(tasks)
                    }
                } else {
                    Log.e("Analytics", "Error fetching tasks: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("Analytics", "Error fetching tasks: ${e.message}")
            }
        }
    }

    private suspend fun getTasksFromApi(): Response<List<Task>> {
        return RetrofitClient.getApiService(requireContext()).getAllTasks()
    }

    private fun updateTaskStatusCounts(tasks: List<Task>) {
        val currentDateTime = Date()
        val pendingCount = tasks.count { isTaskPending(it, currentDateTime) }
        val overdueCount = tasks.count { isTaskOverdue(it, currentDateTime) }
        val completedCount = tasks.count { it.isChecked }
        val createdCount = tasks.size

        tvPendingTasksCount.text = pendingCount.toString()
        tvOverdueTasksCount.text = overdueCount.toString()
        tvCompletedTasksCount.text = completedCount.toString()
        tvCreatedTasksCount.text = createdCount.toString()
    }

    private fun setupLineChart(tasks: List<Task>) {
        // Count tasks
        val completedCount = tasks.count { it.isChecked }
        val pendingCount = tasks.count { isTaskPending(it, Date()) }
        val overdueCount = tasks.count { isTaskOverdue(it, Date()) }
        val createdCount = tasks.size

        // Log counts for debugging
        Log.d("Analytics", "Counts -> Created: $createdCount, Completed: $completedCount, Pending: $pendingCount, Overdue: $overdueCount")

        // Clear previous data
        lineChart.clear()

        // Set up entries for the chart
        val entries = mutableListOf<Entry>().apply {
            add(Entry(0f, createdCount.toFloat())) // Created tasks
            add(Entry(1f, completedCount.toFloat())) // Completed tasks
            add(Entry(2f, pendingCount.toFloat())) // Pending tasks
            add(Entry(3f, overdueCount.toFloat())) // Overdue tasks
        }

        // Create dataset
        val lineDataSet = LineDataSet(entries, "Task Overview").apply {
            color = ContextCompat.getColor(requireContext(), R.color.very_blue)
            valueTextColor = Color.TRANSPARENT // Hide value labels
            valueTextSize = 0f // Set value text size to 0
            setDrawFilled(true)  // Enable fill under the line
            fillColor = ContextCompat.getColor(requireContext(), R.color.very_blue) // Fill color
            lineWidth = 2f // Set line width
            circleRadius = 5f // Set circle radius for data points
            setDrawCircles(true) // Enable circles
            setDrawValues(false) // Disable displaying values at points
        }

        // Set data for the chart
        val lineData = LineData(lineDataSet)
        lineChart.data = lineData

        // Set X-axis labels
        lineChart.xAxis.valueFormatter = object : ValueFormatter() {
            override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                return when (value.toInt()) {
                    0 -> "Created"
                    1 -> "Completed"
                    2 -> "Pending"
                    3 -> "Overdue"
                    else -> ""
                }
            }
        }

        // Configure X-axis
        lineChart.xAxis.granularity = 1f // Set granularity to 1 to ensure labels are unique
        lineChart.xAxis.axisMinimum = -0.1f  // Slightly shift minimum value left
        lineChart.xAxis.axisMaximum = 3.1f   // Slightly shift maximum value right
        lineChart.xAxis.labelCount = 4 // Number of labels
        lineChart.xAxis.position = XAxis.XAxisPosition.BOTTOM // Set labels at the bottom
        lineChart.xAxis.setDrawGridLines(false) // Disable grid lines

        // Customize chart appearance
        lineChart.description.isEnabled = false // Hide the description
        lineChart.legend.isEnabled = false // Hide legend for simplicity
        lineChart.axisLeft.setDrawGridLines(false) // Disable grid lines
        lineChart.axisRight.isEnabled = false // Hide right Y-axis
        lineChart.animateXY(1000, 1000) // Animate chart

        // Set Y-axis range dynamically
        val maxY = maxOf(createdCount, completedCount, pendingCount, overdueCount)
        lineChart.axisLeft.axisMaximum = maxY.toFloat() + 1
        lineChart.axisLeft.axisMinimum = 0f

        // Refresh the chart
        lineChart.invalidate()
    }

    private fun isTaskPending(task: Task, currentDateTime: Date): Boolean {
        val taskEndDateTime = getTaskEndDateTime(task)
        return taskEndDateTime != null && taskEndDateTime >= currentDateTime && !task.isChecked
    }

    private fun isTaskOverdue(task: Task, currentDateTime: Date): Boolean {
        val taskEndDateTime = getTaskEndDateTime(task)
        return taskEndDateTime != null && taskEndDateTime < currentDateTime && !task.isChecked
    }

    private fun getTaskEndDateTime(task: Task): Date? {
        return SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            .parse("${task.end_date} ${task.end_time}")
    }
}
