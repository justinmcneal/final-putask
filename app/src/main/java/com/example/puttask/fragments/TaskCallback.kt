package com.example.puttask.fragments

import com.example.puttask.api.Task

interface TaskCallback {
    fun onTaskCreated(task: Task)

}