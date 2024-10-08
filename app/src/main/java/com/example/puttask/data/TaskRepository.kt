package com.example.puttask.data
import com.example.puttask.api.RetrofitInstance


class TaskRepository {

    suspend fun getTasks(): List<Task>? {
        val response = RetrofitInstance.api.getTasks()
        return if (response.isSuccessful) response.body() else null
    }

    suspend fun createTask(task: Task): Task? {
        val response = RetrofitInstance.api.createTask(task)
        return if (response.isSuccessful) response.body() else null
    }

    // Add other CRUD operations as needed
}
