package com.example.android.architecture.blueprints.todoapp.data.usecases

import com.example.android.architecture.blueprints.todoapp.tasks.TaskModel
import io.reactivex.Single

interface INetworkTasksUseCases {
    fun getTasks(): Single<List<TaskModel>>
}