package com.example.android.architecture.blueprints.todoapp.data.source

import com.example.android.architecture.blueprints.todoapp.data.usecases.INetworkTasksUseCases
import com.example.android.architecture.blueprints.todoapp.tasks.TaskModel
import io.reactivex.Single

class FakeNetworkTasksUseCases : INetworkTasksUseCases {
    override fun getTasks(): Single<List<TaskModel>> {
        return Single.just(arrayListOf())
    }

}
