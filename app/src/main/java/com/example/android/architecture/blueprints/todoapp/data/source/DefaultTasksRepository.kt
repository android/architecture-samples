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

import androidx.lifecycle.LiveData
import com.example.android.architecture.blueprints.todoapp.data.Result
import com.example.android.architecture.blueprints.todoapp.data.Result.Success
import com.example.android.architecture.blueprints.todoapp.data.Task
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * TODO
 */
class DefaultTasksRepository(
    private val tasksRemoteDataSource: TasksDataSource,
    private val tasksLocalDataSource: TasksDataSource,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : TasksRepository {

    override suspend fun getTasks(forceUpdate: Boolean): Result<List<Task>> {
        if (forceUpdate) {
            try {
                updateTasksFromRemoteDataSource()
            } catch (ex: Exception) {
                return Result.Error(ex)
            }
        }
        return tasksLocalDataSource.getTasks()
    }

    override suspend fun refreshTasks() {
        updateTasksFromRemoteDataSource()
    }

    override fun observeTasks(): LiveData<Result<List<Task>>> {
        return tasksLocalDataSource.observeTasks()
    }

    override suspend fun refreshTask(taskId: String) {
        updateTaskFromRemoteDataSource(taskId)
    }

    private suspend fun updateTasksFromRemoteDataSource() {
        val remoteTasks = tasksRemoteDataSource.getTasks()

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

    override fun observeTask(taskId: String): LiveData<Result<Task>> {
        return tasksLocalDataSource.observeTask(taskId)
    }

    private suspend fun updateTaskFromRemoteDataSource(taskId: String) {
        val remoteTask = tasksRemoteDataSource.getTask(taskId)

        if (remoteTask is Success) {
            tasksLocalDataSource.saveTask(remoteTask.data)
        }
    }

    /**
     * Relies on [getTasks] to fetch data and picks the task with the same ID.
     */
    override suspend fun getTask(taskId: String, forceUpdate: Boolean): Result<Task> {

        if (forceUpdate) {
            updateTaskFromRemoteDataSource(taskId)
        }
        return tasksLocalDataSource.getTask(taskId)
    }

    override suspend fun saveTask(task: Task) {
        // Do in memory cache update to keep the app UI up to date
//        cacheAndPerform(task) {
            coroutineScope {
                launch { tasksRemoteDataSource.saveTask(task) }
                launch { tasksLocalDataSource.saveTask(task) }
            }
//        }
    }

    override suspend fun completeTask(task: Task) {
        // Do in memory cache update to keep the app UI up to date
//        cacheAndPerform(task) {
            task.isCompleted = true
            coroutineScope {
                launch { tasksRemoteDataSource.completeTask(task) }
                launch { tasksLocalDataSource.completeTask(task) }
            }
//        }
    }

    override suspend fun completeTask(taskId: String) {
        withContext(ioDispatcher) {
            (getTaskWithId(taskId) as? Success)?.let { it ->
                completeTask(it.data)
            }
        }
    }

    override suspend fun activateTask(task: Task) = withContext<Unit>(ioDispatcher) {
        // Do in memory cache update to keep the app UI up to date
//        cacheAndPerform(task) {
            task.isCompleted = false
            coroutineScope {
                launch { tasksRemoteDataSource.activateTask(task) }
                launch { tasksLocalDataSource.activateTask(task) }
            }

//        }
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
//        withContext(ioDispatcher) {
//            cachedTasks?.entries?.removeAll { it.value.isCompleted }
//        }
    }

    override suspend fun deleteAllTasks() {
        withContext(ioDispatcher) {
            coroutineScope {
                launch { tasksRemoteDataSource.deleteAllTasks() }
                launch { tasksLocalDataSource.deleteAllTasks() }
            }
        }
//        cachedTasks?.clear()
    }

    override suspend fun deleteTask(taskId: String) {
        coroutineScope {
            launch { tasksRemoteDataSource.deleteTask(taskId) }
            launch { tasksLocalDataSource.deleteTask(taskId) }
        }

//        cachedTasks?.remove(taskId)
        Unit // Force return type
    }
//
//    private fun refreshCache(tasks: List<Task>) {
////        cachedTasks?.clear()
//        tasks.sortedBy { it.id }.forEach {
//            cacheAndPerform(it) {}
//        }
//    }
//
//    private suspend fun refreshLocalDataSource(tasks: List<Task>) {
//        tasksLocalDataSource.deleteAllTasks()
//        for (task in tasks) {
//            tasksLocalDataSource.saveTask(task)
//        }
//    }
//
//    private suspend fun refreshLocalDataSource(task: Task) {
//        tasksLocalDataSource.saveTask(task)
//    }

    private suspend fun getTaskWithId(id: String): Result<Task> {
        return tasksLocalDataSource.getTask(id)
    }
//
//    private fun cacheTask(task: Task): Task {
//        val cachedTask = Task(task.title, task.description, task.isCompleted, task.id)
//        // Create if it doesn't exist.
//        if (cachedTasks == null) {
//            cachedTasks = ConcurrentHashMap()
//        }
//        cachedTasks?.put(cachedTask.id, cachedTask)
//        return cachedTask
//    }
//
//    private inline fun cacheAndPerform(task: Task, perform: (Task) -> Unit) {
//        val cachedTask = cacheTask(task)
//        perform(cachedTask)
//    }
}
