package com.example.android.architecture.blueprints.todoapp.data.usecases

import com.example.android.architecture.blueprints.todoapp.data.source.network.NetworkTasksRepository
import com.example.android.architecture.blueprints.todoapp.data.source.network.toModel
import com.example.android.architecture.blueprints.todoapp.tasks.TaskModel
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