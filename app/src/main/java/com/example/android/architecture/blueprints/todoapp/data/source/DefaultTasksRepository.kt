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

package com.example.android.architecture.blueprints.todoapp.data.source

import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.local.TasksDao
import com.example.android.architecture.blueprints.todoapp.data.toExternalModel
import com.example.android.architecture.blueprints.todoapp.data.toLocalModel
import com.example.android.architecture.blueprints.todoapp.data.toNetworkModels
import com.example.android.architecture.blueprints.todoapp.data.toTaskEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Default implementation of [TasksRepository]. Single entry point for managing tasks' data.
 */
class DefaultTasksRepository(
    private val tasksNetworkDataSource: NetworkDataSource,
    private val tasksDao: TasksDao,
) : TasksRepository {

    override suspend fun createTask(title: String, description: String): Task {
        val task = Task(title = title, description = description)
        tasksDao.insertTask(task.toLocalModel())
        sendTasksToNetworkDataSource()
        return task
    }

    override suspend fun updateTask(taskId: String, title: String, description: String) {
        val task = getTask(taskId)?.copy(
            title = title,
            description = description
        ) ?: throw Exception("Task (id $taskId) not found")

        tasksDao.insertTask(task.toLocalModel())
        sendTasksToNetworkDataSource()
    }

    override suspend fun getTasks(forceUpdate: Boolean): List<Task> {
        if (forceUpdate) {
            updateTasksFromNetworkDataSource()
        }
        return tasksDao.getTasks().map { it.toExternalModel() }
    }

    override suspend fun refreshTasks() {
        updateTasksFromNetworkDataSource()
    }

    override fun getTasksStream(): Flow<List<Task>> {
        return tasksDao.observeTasks().map { tasks ->
            tasks.map { task ->
                task.toExternalModel()
            }
        }
    }

    override suspend fun refreshTask(taskId: String) {
        updateTasksFromNetworkDataSource()
    }

    private suspend fun updateTasksFromNetworkDataSource() {
        val remoteTasks = tasksNetworkDataSource.loadTasks()

        // Real apps might want to do a proper sync, deleting, modifying or adding each task.
        tasksDao.deleteTasks()
        remoteTasks.forEach { task ->
            tasksDao.insertTask(task.toTaskEntity())
        }
    }

    private suspend fun sendTasksToNetworkDataSource() {
        // Real apps may want to use a proper sync strategy here to avoid data conflicts.
        val localTasks = tasksDao.getTasks()
        tasksNetworkDataSource.saveTasks(localTasks.toNetworkModels())
    }

    override fun getTaskStream(taskId: String): Flow<Task?> {
        return tasksDao.observeTaskById(taskId).map { it.toExternalModel() }
    }

    /**
     * Get a Task with the given ID. Will return null if the task cannot be found.
     *
     * @param taskId - The ID of the task
     * @param forceUpdate - true if the task should be updated from the network data source first.
     */
    override suspend fun getTask(taskId: String, forceUpdate: Boolean): Task? {
        if (forceUpdate) {
            updateTasksFromNetworkDataSource()
        }
        return tasksDao.getTaskById(taskId)?.toExternalModel()
    }

    override suspend fun completeTask(taskId: String) {
        tasksDao.updateCompleted(taskId = taskId, completed = true)
        sendTasksToNetworkDataSource()
    }

    override suspend fun activateTask(taskId: String) {
        tasksDao.updateCompleted(taskId = taskId, completed = false)
        sendTasksToNetworkDataSource()
    }

    override suspend fun clearCompletedTasks() {
        tasksDao.deleteCompletedTasks()
        sendTasksToNetworkDataSource()
    }

    override suspend fun deleteAllTasks() {
        tasksDao.deleteTasks()
        sendTasksToNetworkDataSource()
    }

    override suspend fun deleteTask(taskId: String) {
        tasksDao.deleteTaskById(taskId)
        sendTasksToNetworkDataSource()
    }
}
