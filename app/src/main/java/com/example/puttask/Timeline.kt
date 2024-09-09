package com.example.puttask

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arjungupta08.horizontal_calendar_date.HorizontalCalendarAdapter
import com.arjungupta08.horizontal_calendar_date.HorizontalCalendarSetUp

class Timeline : Fragment(R.layout.fragment_timeline), HorizontalCalendarAdapter.OnItemClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvDateMonth: TextView
    private lateinit var ivCalendarNext: ImageView
    private lateinit var ivCalendarPrevious: ImageView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        recyclerView = view.findViewById(R.id.recyclerView)
        tvDateMonth = view.findViewById(R.id.text_date_month)
        ivCalendarNext = view.findViewById(R.id.iv_calendar_next)
        ivCalendarPrevious = view.findViewById(R.id.iv_calendar_previous)

        // Set up RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        val calendarSetUp = HorizontalCalendarSetUp()
        val tvMonth = calendarSetUp.setUpCalendarAdapter(recyclerView, this)
        tvDateMonth.text = tvMonth

        calendarSetUp.setUpCalendarPrevNextClickListener(ivCalendarNext, ivCalendarPrevious, this) {
            tvDateMonth.text = it
        }
    }

    override fun onItemClick(ddMmYy: String, dd: String, day: String) {
        view?.findViewById<TextView>(R.id.selectedDate)?.text = "Selected date: $ddMmYy"
        view?.findViewById<TextView>(R.id.selectedDD)?.text = "Selected DD: $dd"
        view?.findViewById<TextView>(R.id.selectedDay)?.text = "Selected day: $day"
    }
}
