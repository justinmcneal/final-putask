package com.example.puttask.data

data class CreateRequest(
    val task_name: String,        //note the value should be the same in the api json format
    val task_description: String,
    val start_datetime: String,
    val end_datetime: String,
    val repeat_days: List<String>?
)

data class CreateResponse(
    val success: Boolean,
    val message: String?,
    val task_id: Int?,
    val created_task: Task?
)

data class Task(
    val id: Int,
    val task_name: String,
    val task_description: String,
    val start_datetime: String,
    val end_datetime: String,
    val repeat_days: List<String>?,  // Nullable in case it's not required
    val isChecked: Boolean
)

data class UpdateRequest(
    val id: Int,
    val task_name: String?,
    val task_description: String?,
    val start_datetime: String?,
    val end_datetime: String?,
    val repeat_days: List<String>?
)

data class UpdateResponse(
    val success: Boolean,
    val message: String?,
    val updated_task: Task?
)

data class DeleteRequest(
    val id: Int
)

data class DeleteResponse(
    val success: Boolean,
    val message: String?
)
