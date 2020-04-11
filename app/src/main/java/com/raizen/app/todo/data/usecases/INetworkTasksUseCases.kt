package com.raizen.app.todo.data.usecases

import com.raizen.app.todo.tasks.TaskModel
import io.reactivex.Single

interface INetworkTasksUseCases {
    fun getTasks(): Single<List<TaskModel>>
}