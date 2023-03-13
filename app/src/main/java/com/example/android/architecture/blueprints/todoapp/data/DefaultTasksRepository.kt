/*
 * Copyright 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.architecture.blueprints.todoapp.data

import com.example.android.architecture.blueprints.todoapp.data.source.network.NetworkDataSource
import com.example.android.architecture.blueprints.todoapp.data.source.local.TasksDao
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Default implementation of [TasksRepository]. Single entry point for managing tasks' data.
 */
class DefaultTasksRepository(
    private val tasksNetworkDataSource: NetworkDataSource,
    private val tasksDao: TasksDao,
) : TasksRepository {

    override suspend fun createTask(title: String, description: String): Task {
        val task = Task(title = title, description = description)

        coroutineScope {
            launch { tasksNetworkDataSource.saveTask(task.toNetworkModel()) }
            launch {
                tasksDao.insertTask(task.toLocalModel())
            }
        }
        return task
    }

    override suspend fun updateTask(taskId: String, title: String, description: String) {

        val task = getTask(taskId)?.copy(
            title = title,
            description = description
        ) ?: throw Exception("Task (id $taskId) not found")

        coroutineScope {
            launch { tasksNetworkDataSource.saveTask(task.toNetworkModel()) }
            launch {
                tasksDao.insertTask(task.toLocalModel())
            }
        }
    }

    override suspend fun getTasks(forceUpdate: Boolean): List<Task> {
        if (forceUpdate) {
            updateTasksFromRemoteDataSource()
        }
        return tasksDao.getTasks().map { it.toExternalModel() }
    }

    override suspend fun refreshTasks() {
        updateTasksFromRemoteDataSource()
    }

    override fun getTasksStream(): Flow<List<Task>> {
        return tasksDao.observeTasks().map { tasks ->
            tasks.map { task ->
                task.toExternalModel()
            }
        }
    }

    override suspend fun refreshTask(taskId: String) {
        updateTaskFromRemoteDataSource(taskId)
    }

    private suspend fun updateTasksFromRemoteDataSource() {
        val remoteTasks = tasksNetworkDataSource.loadTasks()

        // Real apps might want to do a proper sync, deleting, modifying or adding each task.
        tasksDao.deleteTasks()
        remoteTasks.forEach { task ->
            tasksDao.insertTask(task.toTaskEntity())
        }
    }

    override fun getTaskStream(taskId: String): Flow<Task?> {
        return tasksDao.observeTaskById(taskId).map { it.toExternalModel() }
    }

    private suspend fun updateTaskFromRemoteDataSource(taskId: String) {
        val remoteTask = tasksNetworkDataSource.getTask(taskId)

        if (remoteTask == null) {
            tasksDao.deleteTaskById(taskId)
        } else {
            tasksDao.insertTask(
                remoteTask.toTaskEntity()
            )
        }
    }

    /**
     * Relies on [getTasks] to fetch data and picks the task with the same ID. Will return a null
     * Task if the task cannot be found.
     *
     * @param taskId - The ID of the task
     * @param forceUpdate - true if the task should be updated from the remote data source.
     */
    override suspend fun getTask(taskId: String, forceUpdate: Boolean): Task? {
        if (forceUpdate) {
            updateTaskFromRemoteDataSource(taskId)
        }
        return tasksDao.getTaskById(taskId)?.toExternalModel()
    }

    override suspend fun completeTask(taskId: String) {
        coroutineScope {
            launch { tasksNetworkDataSource.completeTask(taskId) }
            launch { tasksDao.updateCompleted(taskId = taskId, completed = true) }
        }
    }

    override suspend fun activateTask(taskId: String) {
        coroutineScope {
            launch { tasksNetworkDataSource.activateTask(taskId) }
            launch { tasksDao.updateCompleted(taskId = taskId, completed = false) }
        }
    }

    override suspend fun clearCompletedTasks() {
        coroutineScope {
            launch { tasksNetworkDataSource.clearCompletedTasks() }
            launch { tasksDao.deleteCompletedTasks() }
        }
    }

    override suspend fun deleteAllTasks() {
        coroutineScope {
            launch { tasksNetworkDataSource.deleteAllTasks() }
            launch { tasksDao.deleteTasks() }
        }
    }

    override suspend fun deleteTask(taskId: String) {
        coroutineScope {
            launch { tasksNetworkDataSource.deleteTask(taskId) }
            launch { tasksDao.deleteTaskById(taskId) }
        }
    }
}
