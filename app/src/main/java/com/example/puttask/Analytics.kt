package com.example.puttask

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.TextView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.formatter.ValueFormatter
import org.w3c.dom.Text
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class Analytics : Fragment(R.layout.fragment_analytics) {

    private lateinit var lineChart: LineChart
    private lateinit var tvsevenDays: TextView
    private lateinit var tvtwentyeightDays: TextView
    private lateinit var tvsixtyDays: TextView
    private lateinit var tvthreesixtyfiveDays: TextView
    private lateinit var tvCustom: TextView

    private val entries = ArrayList<Entry>()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize the LineChart
        lineChart = view.findViewById(R.id.lineChart)
        tvsevenDays = view.findViewById(R.id.tvsevenDays)
        tvtwentyeightDays = view.findViewById(R.id.tvtwentyeightDays)
        tvsixtyDays = view.findViewById(R.id.tvsixtyDays)
        tvthreesixtyfiveDays = view.findViewById(R.id.tvthreesixtyfiveDays)
        tvCustom = view.findViewById(R.id.tvCustom)

        tvsevenDays.setOnClickListener{
            updateChartForLast7Days()
            highlightButton(tvsevenDays)
        }
    }
    private fun updateChartForLast7Days(){
        lineChart.clear() //to clear the existing data
        entries.clear()

        // create list of entries for the past 7 days
        val calendar = Calendar.getInstance()
        val currentDate = calendar.timeInMillis

        for (i in 0 until 7){
            val date = Calendar.getInstance().apply() {
                timeInMillis = currentDate
                add(Calendar.DAY_OF_MONTH, -1)
            }.time

            val percentageDataPoint = (Math.random() *100).toFloat()

            entries.add(Entry (i.toFloat(), percentageDataPoint))

        }
        // Create a dataset and customize it
        val dataSet = LineDataSet(entries, "Last 7 Days Data")
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
    private fun highlightButton(tvsevenDays:TextView) {
        tvsevenDays.setBackgroundColor(Color.parseColor("#FFBB86FC")) // Change color to purple or any desired color
    }
}
