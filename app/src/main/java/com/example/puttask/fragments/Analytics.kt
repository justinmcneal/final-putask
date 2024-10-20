package com.example.puttask.fragments

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.TextView
import com.example.puttask.R
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.formatter.ValueFormatter
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
