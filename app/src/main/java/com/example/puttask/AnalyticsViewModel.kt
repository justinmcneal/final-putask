package com.example.puttask

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.puttask.api.RetrofitClient
import com.example.puttask.api.Task
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class AnalyticsViewModel(private val context: Context) : ViewModel() {

    private val _tasks = MutableLiveData<List<Task>>()
    val tasks: LiveData<List<Task>> = _tasks

    private val _pendingTasksCount = MutableLiveData<Int>()
    val pendingTasksCount: LiveData<Int> = _pendingTasksCount

    private val _overdueTasksCount = MutableLiveData<Int>()
    val overdueTasksCount: LiveData<Int> = _overdueTasksCount

    fun fetchTasks() {
        viewModelScope.launch {
            try {
                val apiService = RetrofitClient.getApiService(context) // Use context from ViewModel
                val response = apiService.getAllTasks()
                if (response.isSuccessful) {
                    val tasks = response.body() ?: emptyList()

                    // Sort tasks into completed, pending, and overdue categories
                    val completedTasks = tasks.filter { it.isChecked }
                    val pendingTasks = tasks.filter { !it.isChecked && !isTaskOverdue(it) }
                    val overdueTasks = tasks.filter { !it.isChecked && isTaskOverdue(it) }

                    _tasks.postValue(tasks)
                    _pendingTasksCount.postValue(pendingTasks.size)
                    _overdueTasksCount.postValue(overdueTasks.size)
                } else {
                    // Handle API error
                    Log.e("TaskViewModel", "Error fetching tasks: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                // Handle network or other exceptions
                Log.e("TaskViewModel", "Error fetching tasks: ${e.message}")
            }
        }
    }

    private fun isTaskOverdue(task: Task): Boolean {
        val taskEndDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse("${task.end_date} ${task.end_time}")?.time ?: 0
        val currentDate = System.currentTimeMillis()
        return taskEndDate < currentDate
    }
}