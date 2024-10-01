package com.example.puttask

class Task {
    data class Task(
        val title: String,
        val description: String,
        val time: String,
        val isChecked: Boolean
    )
}