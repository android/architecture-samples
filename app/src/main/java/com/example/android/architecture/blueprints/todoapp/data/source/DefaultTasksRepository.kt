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
import com.example.android.architecture.blueprints.todoapp.data.source.local.TaskEntity
import com.example.android.architecture.blueprints.todoapp.data.source.local.TasksDao
import com.example.android.architecture.blueprints.todoapp.data.source.local.asExternalModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Default implementation of [TasksRepository]. Single entry point for managing tasks' data.
 */
class DefaultTasksRepository(
    private val tasksRemoteDataSource: TasksDataSource,
    private val tasksDao: TasksDao,
) : TasksRepository {

    override suspend fun getTasks(forceUpdate: Boolean): List<Task> {
        if (forceUpdate) {
            updateTasksFromRemoteDataSource()
        }
        return tasksDao.getTasks().map { it.asExternalModel() }
    }

    override suspend fun refreshTasks() {
        updateTasksFromRemoteDataSource()
    }

    override fun getTasksStream(): Flow<List<Task>> {
        return tasksDao.observeTasks().map { tasks ->
            tasks.map { task ->
                task.asExternalModel()
            }
        }
    }

    override suspend fun refreshTask(taskId: String) {
        updateTaskFromRemoteDataSource(taskId)
    }

    private suspend fun updateTasksFromRemoteDataSource() {
        val remoteTasks = tasksRemoteDataSource.getTasks()

        // Real apps might want to do a proper sync, deleting, modifying or adding each task.
        tasksDao.deleteTasks()
        remoteTasks.forEach { task ->
            // TODO: Move into mapping function (remote to local)
            tasksDao.insertTask(TaskEntity(
                id = task.id,
                title = task.title,
                description = task.description,
                isCompleted = task.isCompleted
            ))
        }
    }

    override fun getTaskStream(taskId: String): Flow<Task?> {
        return tasksDao.observeTaskById(taskId).map { it.asExternalModel() }
    }

    private suspend fun updateTaskFromRemoteDataSource(taskId: String) {
        val remoteTask = tasksRemoteDataSource.getTask(taskId)

        if (remoteTask == null) {
            tasksDao.deleteTaskById(taskId)
        } else {
            // TODO: Move into mapping function (remote to local)
            tasksDao.insertTask(
                TaskEntity(
                    id = remoteTask.id,
                    title = remoteTask.title,
                    description = remoteTask.description,
                    isCompleted = remoteTask.isCompleted
                ) 
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
        return tasksDao.getTaskById(taskId)?.asExternalModel()
    }

    override suspend fun saveTask(task: Task) {
        coroutineScope {
            launch { tasksRemoteDataSource.saveTask(task) }
            launch {
            // TODO: Move to mapping function (external to internal)
                tasksDao.insertTask(
                    TaskEntity(
                        id = task.id,
                        title = task.title,
                        description = task.description,
                        isCompleted = task.isCompleted
                    )
                )
            }
        }
    }

    override suspend fun completeTask(taskId: String) {
        coroutineScope {
            launch { tasksRemoteDataSource.completeTask(taskId) }
            launch { tasksDao.updateCompleted(taskId = taskId, completed = true) }
        }
    }

    override suspend fun activateTask(taskId: String) {
        coroutineScope {
            launch { tasksRemoteDataSource.activateTask(taskId) }
            launch { tasksDao.updateCompleted(taskId = taskId, completed = false) }
        }
    }

    override suspend fun clearCompletedTasks() {
        coroutineScope {
            launch { tasksRemoteDataSource.clearCompletedTasks() }
            launch { tasksDao.deleteCompletedTasks() }
        }
    }

    override suspend fun deleteAllTasks() {
        coroutineScope {
            launch { tasksRemoteDataSource.deleteAllTasks() }
            launch { tasksDao.deleteTasks() }
        }
    }

    override suspend fun deleteTask(taskId: String) {
        coroutineScope {
            launch { tasksRemoteDataSource.deleteTask(taskId) }
            launch { tasksDao.deleteTaskById(taskId) }
        }
    }
}

fun List<TaskEntity>.asExternalModels() = map { it.asExternalModel() }
