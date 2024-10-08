package com.example.puttask.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.puttask.data.Task
import com.example.puttask.data.TaskRepository
import kotlinx.coroutines.launch

class TaskViewModel(private val repository: TaskRepository) : ViewModel() {

    val tasks = MutableLiveData<List<Task>>()

    fun getTasks() = viewModelScope.launch {
        tasks.value = repository.getTasks()
    }

    fun createTask(task: Task) = viewModelScope.launch {
        repository.createTask(task)
    }
}
