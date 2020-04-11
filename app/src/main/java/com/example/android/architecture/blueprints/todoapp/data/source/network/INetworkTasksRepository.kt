package com.example.android.architecture.blueprints.todoapp.data.source.network

import io.reactivex.Single

interface INetworkTasksRepository {
    fun getTasks() : Single<List<TaskResponse>>
}
