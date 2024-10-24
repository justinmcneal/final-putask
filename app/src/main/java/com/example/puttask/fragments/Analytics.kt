package com.example.puttask.fragments

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
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
import java.util.*
import kotlin.collections.ArrayList

class Analytics : Fragment(R.layout.fragment_analytics) {

    private lateinit var lineChart: LineChart
    private lateinit var tvsevenDays: TextView
    private lateinit var tvtwentyeightDays: TextView
    private lateinit var tvsixtyDays: TextView
    private lateinit var tvthreesixtyfiveDays: TextView
    private lateinit var tvTaskOverviewDate: TextView
    private val entries = ArrayList<Entry>()

    private lateinit var tvPendingTasksCount: TextView
    private lateinit var tvOverdueTasksCount: TextView // Add this for overdue tasks

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
        tvOverdueTasksCount = view.findViewById(R.id.tvOverdueTasksCount) // Initialize overdue TextView


        // Fetch tasks and update pending tasks count
        fetchPendingTasksCount()



        // Set up click listeners for the buttons
        tvsevenDays.setOnClickListener {
            updateChart(7, "Pending Tasks - Last 7 Days")
            highlightButton(tvsevenDays)
        }
        tvtwentyeightDays.setOnClickListener {
            updateChart(28, "Pending Tasks - Last 28 Days")
            highlightButton(tvtwentyeightDays)
        }
        tvsixtyDays.setOnClickListener {
            updateChart(60, "Pending Tasks - Last 60 Days")
            highlightButton(tvsixtyDays)
        }
        tvthreesixtyfiveDays.setOnClickListener {
            updateChart(365, "Pending Tasks - Last 365 Days")
            highlightButton(tvthreesixtyfiveDays)
        }
    }

    // Fetch tasks and update chart
    private fun updateChart(days: Int, label: String) {
        lineChart.clear()
        entries.clear()

        // Get current date and calculate date range
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
        val currentDate = dateFormat.format(calendar.time)
        calendar.add(Calendar.DAY_OF_YEAR, -days)
        val startDate = dateFormat.format(calendar.time)

        // Set the date range in tvTaskOverviewDate
        tvTaskOverviewDate.text = "$startDate - $currentDate"

        // Fetch tasks using API
        lifecycleScope.launch {
            val response = getTasksFromApi()
            if (response.isSuccessful) {
                // Filter tasks where the end date is in the future (pending tasks)
                val pendingTasks = response.body()?.filter { task ->
                    val taskEndDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(task.end_date)
                    taskEndDate?.after(Date()) == true
                } ?: listOf()

                val tasksGroupedByDay = groupTasksByDay(pendingTasks, days)

                // Create data entries for the past 'days' days based on pending tasks
                for (i in 0 until days) {
                    val taskCountForDay = tasksGroupedByDay[i] ?: 0 // Default to 0 if no tasks for the day
                    entries.add(Entry(i.toFloat(), taskCountForDay.toFloat()))
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
                        // Create a calendar instance and set it to the past (days ago) and move towards the current date
                        val calendar = Calendar.getInstance()

                        // Subtract (total days - value) to get dates leading up to today
                        val totalDays = lineChart.data.xMax.toInt() // Total days in the chart
                        val daysAgo = totalDays - value.toInt()

                        calendar.add(Calendar.DAY_OF_MONTH, -daysAgo) // Move the calendar back by the calculated number of days

                        // Format the date to "MM/dd" and return
                        return dateFormat.format(calendar.time)
                    }

                }

                // Customize chart appearance
                lineChart.xAxis.labelRotationAngle = -45f
                lineChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
                lineChart.axisRight.isEnabled = false
                lineChart.invalidate() // Refresh the chart with new data
            }
        }
    }

    private fun markTaskComplete(task: Task) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Log the task being marked as complete/incomplete
                Log.d("TaskManager", "Attempting to mark task (ID: ${task.id}) as ${if (task.isChecked) "complete" else "incomplete"}")

                // Make the API call to update the task's status
                val response = RetrofitClient.getApiService(requireContext())
                    .markTaskComplete(task.id, CompleteTaskRequest(task.isChecked))

                if (response.isSuccessful) {
                    val updatedTask = response.body()

                    // Log successful response
                    Log.i("TaskManager", "Successfully updated task (ID: ${task.id}) status to ${if (task.isChecked) "complete" else "incomplete"}")

                    // Update the task list or UI
                    withContext(Dispatchers.Main) {
                        showSuccess("Task marked as ${if (task.isChecked) "complete" else "incomplete"}")
                    }
                } else {
                    // Log API failure response
                    Log.e("TaskManager", "Failed to update task status (ID: ${task.id}): ${response.message()}")

                    withContext(Dispatchers.Main) {
                        showError("Failed to update task status: ${response.message()}")
                    }
                }
            } catch (e: Exception) {
                // Log the exception
                Log.e("TaskManager", "Error occurred while updating task (ID: ${task.id}): ${e.message}", e)

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



    // Group tasks by day for the given date range
    private fun groupTasksByDay(tasks: List<Task>, days: Int): Map<Int, Int> {
        val calendar = Calendar.getInstance()
        val tasksPerDay = mutableMapOf<Int, Int>()
        val currentDate = calendar.timeInMillis

        for (task in tasks) {
            val taskDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(task.end_date)?.time ?: continue
            val dayDifference = ((currentDate - taskDate) / (1000 * 60 * 60 * 24)).toInt()

            // Only include tasks within the specified date range
            if (dayDifference in 0 until days) {
                tasksPerDay[dayDifference] = tasksPerDay.getOrDefault(dayDifference, 0) + 1
            }
        }
        return tasksPerDay
    }

    private fun highlightButton(selectedButton: TextView) {
        selectedButton.setBackgroundColor(Color.parseColor("#FFBB86FC")) // Change color to purple or any desired color
    }
    private suspend fun getTasksFromApi(): Response<List<Task>> {
        return RetrofitClient.getApiService(requireContext()).getAllTasks()
    }

    private fun fetchPendingTasksCount() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = getTasksFromApi()
                if (response.isSuccessful) {
                    val tasks = response.body() ?: emptyList()

                    // Current date in the same format as the end_date
                    val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                    Log.d("PendingTasks", "Current date: $currentDate")

                    // Count pending tasks: those with end_date >= currentDate (upcoming tasks)
                    val pendingTasksCount = tasks.count { task ->
                        val taskEndDate = task.end_date
                        taskEndDate >= currentDate // Tasks with future or same-day end_date
                    }

                    // Count overdue tasks: those with end_date < currentDate
                    val overdueTasksCount = tasks.count { task ->
                        val taskEndDate = task.end_date
                        taskEndDate < currentDate // Tasks with a past end_date
                    }

                    Log.d("PendingTasks", "Pending tasks count: $pendingTasksCount")
                    Log.d("OverdueTasks", "Overdue tasks count: $overdueTasksCount")

                    withContext(Dispatchers.Main) {
                        tvPendingTasksCount.text = pendingTasksCount.toString()
                        tvOverdueTasksCount.text = overdueTasksCount.toString() // Display overdue count
                    }
                } else {
                }
            } catch (e: Exception) {
                Log.e("PendingTasks", "Error fetching tasks: ${e.message}")
            }
        }
    }
}


