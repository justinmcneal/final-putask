// ListsViewModel.kt
package com.example.puttask

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.puttask.api.Task

class ListsViewModel : ViewModel() {
    private val _taskList = MutableLiveData<MutableList<Task>>(mutableListOf())
    val taskList: LiveData<MutableList<Task>> get() = _taskList

    // Function to update tasks when fetched from the API
    fun updateTasks(newTasks: List<Task>) {
        _taskList.value = newTasks.toMutableList()
    }

    // Function to hide a task (sets visibility to false)
    fun hideTask(task: Task) {
        val currentList = _taskList.value ?: return
        val updatedList = currentList.map { if (it.id == task.id) it.copy(isVisible = false) else it }
        _taskList.value = updatedList.toMutableList()
    }
}
