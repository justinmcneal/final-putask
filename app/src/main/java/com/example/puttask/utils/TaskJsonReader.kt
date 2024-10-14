package com.example.puttask.utils // Change this to match the package where you created the class

import com.example.puttask.model.Task // Import the Task class
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream

class TaskJsonReader {
    fun readTasks(inputStream: InputStream): List<Task> {
        val tasks = mutableListOf<Task>()
        val jsonString = inputStream.bufferedReader().use { it.readText() } // Read input stream to string
        val jsonArray = JSONArray(jsonString) // Parse JSON string to JSONArray

        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i) // Get each JSON object
            val task = Task(
                id = jsonObject.getInt("id"),
                task_name = jsonObject.getString("task_name"),
                task_description = jsonObject.getString("task_description"),
                start_datetime = jsonObject.getString("start_datetime"),
                end_datetime = jsonObject.getString("end_datetime"),
                isChecked = jsonObject.getBoolean("isChecked") // Assuming you have this property
            )
            tasks.add(task) // Add the task to the list
        }
        return tasks // Return the list of tasks
    }
}
