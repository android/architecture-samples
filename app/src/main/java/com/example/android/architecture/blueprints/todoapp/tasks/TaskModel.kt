package com.example.android.architecture.blueprints.todoapp.tasks

import com.example.android.architecture.blueprints.todoapp.data.Task

data class TaskModel constructor(val title: String,
                                 val description: String) {
    fun toTask(): Task {
        return Task(title, description, false)
    }
}