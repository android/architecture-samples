package com.example.android.architecture.blueprints.todoapp.data.source.network

import com.example.android.architecture.blueprints.todoapp.tasks.TaskModel

data class TaskResponse constructor(val userId: Int,
                                    val id: Int,
                                    val title: String,
                                    val body: String)

fun toModel(it: List<TaskResponse>): List<TaskModel> {
    return it.map { taskResponse -> TaskModel(taskResponse.title, taskResponse.body) }
}