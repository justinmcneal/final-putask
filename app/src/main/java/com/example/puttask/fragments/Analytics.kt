package com.example.puttask.fragments

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
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
    private lateinit var  tvCompletedTasksCount: TextView
    private lateinit var tvPendingTasksCount: TextView
    private lateinit var tvOverdueTasksCount: TextView // Add this for overdue tasks
    private lateinit var tvCreatedTasksCount: TextView // Add this for overdue tasks


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize UI components
        lineChart = view.findViewById(R.id.lineChart)
        tvsevenDays = view.findViewById(R.id.tvsevenDays)
        tvtwentyeightDays = view.findViewById(R.id.tvtwentyeightDays)
        tvsixtyDays = view.findViewById(R.id.tvsixtyDays)
        tvthreesixtyfiveDays = view.findViewById(R.id.tvthreesixtyfiveDays)
        tvTaskOverviewDate = view.findViewById(R.id.tvTaskOverviewDate)

        tvCompletedTasksCount = view.findViewById(R.id.tvCompletedTasksCount)
        tvPendingTasksCount = view.findViewById(R.id.tvPendingTasksCount)
        tvOverdueTasksCount = view.findViewById(R.id.tvOverdueTasksCount) // Initialize overdue TextView
        tvCreatedTasksCount = view.findViewById(R.id.tvCreatedTasksCount) // Initialize overdue TextView



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
                val tasks = response.body() ?: listOf()

                // Filter and group tasks by day for each category
                val pendingTasksGroupedByDay = groupTasksByDay(tasks.filter { task ->
                    val taskEndDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(task.end_date)
                    taskEndDate?.after(Date()) == true && !task.isChecked
                }, days)

                val completedTasksGroupedByDay = groupTasksByDay(tasks.filter { task ->
                    task.isChecked
                }, days)

                val overdueTasksGroupedByDay = groupTasksByDay(tasks.filter { task ->
                    val taskEndDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(task.end_date)
                    taskEndDate?.before(Date()) == true && !task.isChecked
                }, days)

                val createdTasksGroupedByDay = groupTasksByDay(tasks.filter { task ->
                    val taskEndDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(task.end_date)
                    taskEndDate?.before(Date()) == true && !task.isChecked
                }, days)


                // Create entries for each category
                val pendingEntries = mutableListOf<Entry>()
                val completedEntries = mutableListOf<Entry>()
                val overdueEntries = mutableListOf<Entry>()
                val createdEntries = mutableListOf<Entry>()


                for (i in 0 until days) {
                    pendingEntries.add(Entry(i.toFloat(), (pendingTasksGroupedByDay[i] ?: 0).toFloat()))
                    completedEntries.add(Entry(i.toFloat(), (completedTasksGroupedByDay[i] ?: 0).toFloat()))
                    overdueEntries.add(Entry(i.toFloat(), (overdueTasksGroupedByDay[i] ?: 0).toFloat()))
                    createdEntries.add(Entry(i.toFloat(), (createdTasksGroupedByDay[i] ?: 0).toFloat()))

                }

                // Create datasets for each category with custom colors and labels
                val pendingDataSet = LineDataSet(pendingEntries, "$label - Pending")
                pendingDataSet.color = ContextCompat.getColor(requireContext(), R.color.yellow)
                pendingDataSet.lineWidth = 2f
                pendingDataSet.setDrawCircles(true)
                pendingDataSet.setDrawValues(false)

                val completedDataSet = LineDataSet(completedEntries, "$label - Completed")
                completedDataSet.color = resources.getColor(android.R.color.holo_green_light)
                completedDataSet.lineWidth = 2f
                completedDataSet.setDrawCircles(true)
                completedDataSet.setDrawValues(false)

                val overdueDataSet = LineDataSet(overdueEntries, "$label - Overdue")
                overdueDataSet.color = resources.getColor(android.R.color.holo_red_light)
                overdueDataSet.lineWidth = 2f
                overdueDataSet.setDrawCircles(true)
                overdueDataSet.setDrawValues(false)

                val createdDataSet = LineDataSet(overdueEntries, "$label - Created")
                createdDataSet.color = ContextCompat.getColor(requireContext(), R.color.very_blue)
                createdDataSet.lineWidth = 2f
                createdDataSet.setDrawCircles(true)
                createdDataSet.setDrawValues(false)

                // Combine datasets and set to chart
                val lineData = LineData(pendingDataSet, completedDataSet, overdueDataSet)
                lineChart.data = lineData

                // Set x-axis value formatter
                lineChart.xAxis.valueFormatter = object : ValueFormatter() {
                    private val dateFormat = SimpleDateFormat("MM/dd", Locale.getDefault())

                    override fun getFormattedValue(value: Float): String {
                        val calendar = Calendar.getInstance()
                        val totalDays = lineChart.data.xMax.toInt()
                        val daysAgo = totalDays - value.toInt()
                        calendar.add(Calendar.DAY_OF_MONTH, -daysAgo)
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

    private fun highlightButton(button: TextView) {
        // Reset the background tint for all buttons first
        tvsevenDays.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.superlightgray)
        tvtwentyeightDays.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.superlightgray)
        tvsixtyDays.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.superlightgray)
        tvthreesixtyfiveDays.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.superlightgray)

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

                    // Current date and time in the same format as the task's end_date and end_time
                    val currentDateTime = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).parse(
                        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
                    )

                    // Count pending tasks: those with end_date >= currentDate (upcoming tasks)
                    val pendingTasksCount = tasks.count { task ->
                        val taskEndDateTime = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).parse(
                            "${task.end_date} ${task.end_time}"
                        )
                        taskEndDateTime >= currentDateTime && !task.isChecked // Future or same-day end_date and not completed
                    }

                    // Count overdue tasks: those with end_date < currentDate
                    val overdueTasksCount = tasks.count { task ->
                        val taskEndDateTime = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).parse(
                            "${task.end_date} ${task.end_time}"
                        )
                        taskEndDateTime < currentDateTime && !task.isChecked // Past end_date and not completed
                    }

                    // Count completed tasks: those marked as checked
                    val completedTasksCount = tasks.count { task ->
                        task.isChecked // Tasks that are marked as completed
                    }

                    // Count created tasks: all tasks
                    val createdTasksCount = tasks.size // Total number of tasks

                    withContext(Dispatchers.Main) {
                        tvPendingTasksCount.text = pendingTasksCount.toString()
                        tvOverdueTasksCount.text = overdueTasksCount.toString()
                        tvCompletedTasksCount.text = completedTasksCount.toString()
                        tvCreatedTasksCount.text = createdTasksCount.toString()
                    }
                } else {
                    // Handle error response
                }
            } catch (e: Exception) {
                Log.e("PendingTasks", "Error fetching tasks: ${e.message}")
            }
        }
    }
}


