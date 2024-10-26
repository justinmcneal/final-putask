package com.example.puttask.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arjungupta08.horizontal_calendar_date.HorizontalCalendarAdapter
import com.arjungupta08.horizontal_calendar_date.HorizontalCalendarSetUp
import com.example.puttask.R
import com.example.puttask.TimelineAdapter
import com.example.puttask.api.RetrofitClient
import com.example.puttask.api.Task
import com.example.puttask.databinding.FragmentTimelineBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale

class Timeline : Fragment(R.layout.fragment_timeline), HorizontalCalendarAdapter.OnItemClickListener {
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvDateMonth: TextView
    private lateinit var ivCalendarNext: ImageView
    private lateinit var ivCalendarPrevious: ImageView
    private lateinit var timelineAdapter: TimelineAdapter
    private var taskList = mutableListOf<Task>()
    private var originalTaskList = mutableListOf<Task>() // Keep the original list
    private var _binding: FragmentTimelineBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentTimelineBinding.inflate(inflater, container, false).also { _binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = binding.recyclerView
        tvDateMonth = binding.textDateMonth
        ivCalendarNext = binding.ivCalendarNext
        ivCalendarPrevious = binding.ivCalendarPrevious
        setupRecyclerView()
        fetchTasks()
        setupSwipeRefresh()
        updateNoTasksMessage()

        val calendarSetUp = HorizontalCalendarSetUp()
        tvDateMonth.text = calendarSetUp.setUpCalendarAdapter(recyclerView, this)
        calendarSetUp.setUpCalendarPrevNextClickListener(ivCalendarNext, ivCalendarPrevious, this) {
            tvDateMonth.text = it
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            fetchTasks()
        }
    }

    private fun setupRecyclerView() {
        binding.listsrecyclerView.layoutManager = LinearLayoutManager(context)
        timelineAdapter = TimelineAdapter(taskList)
        binding.listsrecyclerView.adapter = timelineAdapter
    }

    private fun fetchTasks() {
        binding.swipeRefreshLayout.isRefreshing = true

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response: Response<List<Task>> = RetrofitClient.getApiService(requireContext()).getAllTasks()

                if (response.isSuccessful) {
                    response.body()?.let { tasks ->
                        Log.d("TimelineFragment", "Fetched tasks: ${tasks.size}")

                        withContext(Dispatchers.Main) {
                            originalTaskList.clear()
                            originalTaskList.addAll(tasks.filter { it.isChecked }) // Keep only completed tasks

                            taskList.clear()
                            taskList.addAll(originalTaskList) // Display all completed tasks initially

                            timelineAdapter.notifyDataSetChanged()
                            updateNoTasksMessage()
                        }
                    }
                } else {
                    Log.e("TimelineFragment", "Error fetching tasks: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("TimelineFragment", "Exception fetching tasks", e)
            } finally {
                withContext(Dispatchers.Main) {
                    binding.swipeRefreshLayout.isRefreshing = false
                }
            }
        }
    }

    private fun updateNoTasksMessage() {
        if (taskList.isEmpty()) {
            binding.tvNotask.visibility = View.VISIBLE
            binding.listsrecyclerView.visibility = View.GONE
        } else {
            binding.tvNotask.visibility = View.GONE
            binding.listsrecyclerView.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onItemClick(ddMmYy: String, dd: String, day: String) {
        val inputFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formattedDate: String? = try {
            val date = inputFormat.parse(ddMmYy)
            outputFormat.format(date)
        } catch (e: ParseException) {
            Log.e("Timeline", "Error parsing clicked date: $e")
            null
        }

        formattedDate?.let {
            Log.d("Timeline", "Formatted date: $it")

            // Filter tasks based on the selected date
            filterTasksByDate(it)
        }
    }

    private fun filterTasksByDate(selectedDate: String) {
        Log.d("Timeline", "Filtering tasks for date: $selectedDate")

        val filteredTasks = originalTaskList.filter { task ->
            task.end_date == selectedDate // Assuming end_date is the date field in Task
        }

        if (filteredTasks.isNotEmpty()) {
            taskList.clear()
            taskList.addAll(filteredTasks)
        } else {
            taskList.clear()
            Toast.makeText(requireContext(), "No tasks found for selected date.", Toast.LENGTH_SHORT).show()
        }

        timelineAdapter.notifyDataSetChanged() // Update the RecyclerView with filtered data
        updateNoTasksMessage()
    }
}
