package com.raizen.app.todo.data.usecases

import com.raizen.app.todo.data.source.network.NetworkTasksRepository
import com.raizen.app.todo.data.source.network.toModel
import com.raizen.app.todo.tasks.TaskModel
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class NetworkTasksUseCases @Inject constructor(private val networkTasksRepository: NetworkTasksRepository)
    : INetworkTasksUseCases{
    override fun getTasks(): Single<List<TaskModel>> {
        return networkTasksRepository
                .getTasks()
                .map { toModel(it) }
                .subscribeOn(io())
                .observeOn(ui())
    }

    private fun ui() = AndroidSchedulers.mainThread()
    private fun io() = Schedulers.io()
}