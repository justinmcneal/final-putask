package com.example.puttask.model // Ensure the package matches your project structure

data class Task(
    val id: Int,
    val task_name: String,
    val task_description: String,
    val start_datetime: String,
    val end_datetime: String,
    val isChecked: Boolean // Assuming you have this property
)
