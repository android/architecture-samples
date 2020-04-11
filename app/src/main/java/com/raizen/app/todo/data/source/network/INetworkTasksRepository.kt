package com.raizen.app.todo.data.source.network

import io.reactivex.Single

interface INetworkTasksRepository {
    fun getTasks() : Single<List<TaskResponse>>
}
