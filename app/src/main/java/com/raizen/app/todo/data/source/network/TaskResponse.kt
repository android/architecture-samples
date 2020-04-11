package com.raizen.app.todo.data.source.network

import com.raizen.app.todo.tasks.TaskModel

data class TaskResponse constructor(val userId: Int,
                                    val id: Int,
                                    val title: String,
                                    val body: String)

fun toModel(it: List<TaskResponse>): List<TaskModel> {
    return it.map { taskResponse -> TaskModel(taskResponse.title, taskResponse.body) }
}