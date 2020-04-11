package com.raizen.app.todo.data.source.network

import io.reactivex.Single
import javax.inject.Inject


class NetworkTasksRepository @Inject constructor(private val apiService: ApiService ): INetworkTasksRepository{

    override fun getTasks() : Single<List<TaskResponse>> = apiService.getTasks()

}