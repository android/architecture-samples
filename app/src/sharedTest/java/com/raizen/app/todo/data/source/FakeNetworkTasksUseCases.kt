package com.raizen.app.todo.data.source

import com.raizen.app.todo.data.usecases.INetworkTasksUseCases
import com.raizen.app.todo.tasks.TaskModel
import io.reactivex.Single

class FakeNetworkTasksUseCases : INetworkTasksUseCases {
    override fun getTasks(): Single<List<TaskModel>> {
        return Single.create(null)
    }

}
