package com.example.puttask.api

data class CreateRequest(
    val task_name: String,        // Ensure this matches the API's expected JSON format
    val task_description: String,
    val end_date: String,
    val end_time: String,
    val repeat_days: List<String>?, // Nullable in case it's optional
    val category: String
)

// Recycler View Shit
data class Task(
    val id: Int,
    val task_name: String,
    val task_description: String,
    val end_date: String,
    val end_time: String,
    val repeat_days: List<String>?, // Nullable, as it might not always be set
    val category: String,
)

// Request to update an existing task
data class UpdateRequest(
    val task_name: String?,       // Nullable in case only some fields are being updated
    val task_description: String?,
    val start_datetime: String?,
    val end_date: String?,
    val end_time: String?,
    val repeat_days: List<String>?,
    val category: String
)

// Response after deleting a task
data class DeleteResponse(
    val success: Boolean,
    val message: String?
)
