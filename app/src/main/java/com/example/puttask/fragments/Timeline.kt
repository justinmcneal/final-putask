package com.example.puttask.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arjungupta08.horizontal_calendar_date.HorizontalCalendarAdapter
import com.arjungupta08.horizontal_calendar_date.HorizontalCalendarSetUp
import com.example.puttask.ListsAdapter
import com.example.puttask.R
import com.example.puttask.api.RetrofitClient
import com.example.puttask.api.Task
import com.example.puttask.databinding.FragmentListsBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response

class Timeline : Fragment(R.layout.fragment_timeline), HorizontalCalendarAdapter.OnItemClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvDateMonth: TextView
    private lateinit var ivCalendarNext: ImageView
    private lateinit var ivCalendarPrevious: ImageView
    private lateinit var listsAdapter: ListsAdapter
    private var taskList: MutableList<Task> = mutableListOf() // Store fetched tasks

    private var _binding: FragmentListsBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        recyclerView = view.findViewById(R.id.recyclerView)
        tvDateMonth = view.findViewById(R.id.text_date_month)
        ivCalendarNext = view.findViewById(R.id.iv_calendar_next)
        ivCalendarPrevious = view.findViewById(R.id.iv_calendar_previous)

        // Set up RecyclerView for lists
        setupRecyclerView()

        // Set up calendar and other listeners
        val calendarSetUp = HorizontalCalendarSetUp()
        val tvMonth = calendarSetUp.setUpCalendarAdapter(recyclerView, this)
        tvDateMonth.text = tvMonth

        calendarSetUp.setUpCalendarPrevNextClickListener(ivCalendarNext, ivCalendarPrevious, this) {
            tvDateMonth.text = it
        }

        // Fetch tasks
        fetchTasks()
    }

    private fun setupRecyclerView() {
        // Initialize the adapter with an empty list
        listsAdapter = ListsAdapter(taskList) { task ->
            // Handle item click
            // You can implement an edit functionality or something else here
        }
        recyclerView.adapter = listsAdapter
        recyclerView.layoutManager = LinearLayoutManager(context)
    }

    private fun fetchTasks() {
        // Show the loading indicator
        binding.swipeRefreshLayout.isRefreshing = true

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response: Response<List<Task>> = RetrofitClient.getApiService(requireContext()).getAllTasks()
                if (response.isSuccessful) {
                    response.body()?.let { tasks ->
                        Log.d("ListsFragment", "Fetched tasks: ${tasks.size}")

                        // Clear and update task list on the main thread
                        withContext(Dispatchers.Main) {
                            taskList.clear()
                            taskList.addAll(tasks)
                            listsAdapter.notifyDataSetChanged()
                            updateNoTasksMessage()
                        }
                    }
                } else {
                    Log.e("ListsFragment", "Error fetching tasks: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("ListsFragment", "Exception fetching tasks", e)
            } finally {
                // Hide the loading indicator
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

    override fun onItemClick(ddMmYy: String, dd: String, day: String) {
        // Handle calendar item clicks
    }
}
