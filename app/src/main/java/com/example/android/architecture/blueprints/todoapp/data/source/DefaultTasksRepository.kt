/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.architecture.blueprints.todoapp.data.source

import com.example.android.architecture.blueprints.todoapp.data.Result
import com.example.android.architecture.blueprints.todoapp.data.Result.Success
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.nytimes.android.external.store4.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * DefaultRepository uses [FlowStoreBuilder] to manage data loading
 */
class DefaultTasksRepository(
        private val tasksRemoteDataSource: TasksDataSource,
        private val tasksLocalDataSource: TasksDataSource,
        private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : TasksRepository {

    val tasksStore = FlowStoreBuilder.from<Unit, Result<List<Task>>, Result<List<Task>>> { tasksRemoteDataSource.observeTasks() }
            .persister(reader = { tasksLocalDataSource.observeTasks() },
                    writer = { _, remoteData -> updateTasksFromRemoteDataSource(remoteData) }
            )
            .build()

    val taskStore = FlowStoreBuilder.fromNonFlow<String, Result<Task>, Result<Task>> { tasksRemoteDataSource.getTask(it) }
            .persister(
                    reader = { tasksLocalDataSource.observeTask(it) },
                    writer = { key, result: Result<Task> -> saveTask(result) }
            )
            .build()

    override suspend fun getTasks(forceUpdate: Boolean): Result<List<Task>> {
        if (forceUpdate) {
            try {
                return tasksStore.get(Unit)
            } catch (ex: Exception) {
                return Result.Error(ex)
            }
        }
        return tasksStore.fresh(Unit)
    }


    override fun observeTasks(shouldRefresh: Boolean): Flow<Result<List<Task>>> {
        return tasksStore.stream(StoreRequest.cached(key = Unit, refresh = true)).filter { it  !is StoreResponse.Loading}.map { it.requireData() }
    }

    override fun observeTask(taskId: String, shouldRefresh: Boolean): Flow<Result<Task>> {
        return taskStore.stream(StoreRequest.cached(taskId, refresh = shouldRefresh)).filter { it  !is StoreResponse.Loading}.map { it.requireData() }
    }


    private suspend fun updateTasksFromRemoteDataSource(remoteTasks: Result<List<Task>>) {
        if (remoteTasks is Success) {
            // Real apps might want to do a proper sync.
            tasksLocalDataSource.deleteAllTasks()
            remoteTasks.data.forEach { task ->
                tasksLocalDataSource.saveTask(task)
            }
        } else if (remoteTasks is Result.Error) {
            throw remoteTasks.exception
        }
    }


    /**
     * Relies on [getTasks] to fetch data and picks the task with the same ID.
     */
    override suspend fun getTask(taskId: String, forceUpdate: Boolean): Result<Task> =
            if (forceUpdate) taskStore.fresh(taskId)
            else taskStore.get(taskId)


     suspend fun saveTask(task: Result<Task>) {
        if (task is Success) {
            saveTask(task.data)
        }

    }

    override suspend fun saveTask(task: Task) {
        coroutineScope {
            launch { tasksRemoteDataSource.saveTask(task) }
            launch { tasksLocalDataSource.saveTask(task) }
        }
    }

    override suspend fun completeTask(task: Task) {
        coroutineScope {
            launch { tasksRemoteDataSource.completeTask(task) }
            launch { tasksLocalDataSource.completeTask(task) }
        }
    }

    override suspend fun completeTask(taskId: String) {
        withContext(ioDispatcher) {
            (getTaskWithId(taskId) as? Success)?.let { it ->
                completeTask(it.data)
            }
        }
    }

    override suspend fun activateTask(task: Task) = withContext<Unit>(ioDispatcher) {
        coroutineScope {
            launch { tasksRemoteDataSource.activateTask(task) }
            launch { tasksLocalDataSource.activateTask(task) }
        }
    }

    override suspend fun activateTask(taskId: String) {
        withContext(ioDispatcher) {
            (getTaskWithId(taskId) as? Success)?.let { it ->
                activateTask(it.data)
            }
        }
    }

    override suspend fun clearCompletedTasks() {
        coroutineScope {
            launch { tasksRemoteDataSource.clearCompletedTasks() }
            launch { tasksLocalDataSource.clearCompletedTasks() }
        }
    }

    override suspend fun deleteAllTasks() {
        withContext(ioDispatcher) {
            coroutineScope {
                launch { tasksRemoteDataSource.deleteAllTasks() }
                launch { tasksLocalDataSource.deleteAllTasks() }
            }
        }
    }

    override suspend fun deleteTask(taskId: String) {
        coroutineScope {
            launch { tasksRemoteDataSource.deleteTask(taskId) }
            launch { tasksLocalDataSource.deleteTask(taskId) }
        }
    }

    private suspend fun getTaskWithId(id: String): Result<Task> {
        return tasksLocalDataSource.getTask(id)
    }
}
