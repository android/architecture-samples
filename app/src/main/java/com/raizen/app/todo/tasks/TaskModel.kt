package com.raizen.app.todo.tasks

import com.raizen.app.todo.data.Task

data class TaskModel constructor(val title: String,
                                 val description: String) {
    fun toTask(): Task {
        return Task(title, description, false)
    }
}