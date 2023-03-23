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

import com.example.android.architecture.blueprints.todoapp.data.source.local.TaskDao
import com.example.android.architecture.blueprints.todoapp.data.source.network.NetworkDataSource
import javax.inject.Inject
import javax.inject.Singleton
import java.util.UUID
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Default implementation of [TaskRepository]. Single entry point for managing tasks' data.
 *
 * @param tasksNetworkDataSource - The network data source
 * @param taskDao - The local data source
 * @param expensiveWorkDispatcher - The dispatcher to be used for long running or complex
 * operations, such as ID generation or mapping many models. This is important to avoid blocking the
 * calling thread.
 * @param fireAndForgetScope - The coroutine scope used for fire-and-forget jobs, such as sending
 * data to the network.
 */
@Singleton
class DefaultTaskRepository @Inject constructor(
    private val tasksNetworkDataSource: NetworkDataSource,
    private val taskDao: TaskDao,
    private val defaultDispatcher: CoroutineDispatcher,
    private val scope: CoroutineScope,
) : TaskRepository {

    override suspend fun createTask(title: String, description: String): Task {
        // ID creation might be a complex operation so it's executed using the supplied
        // coroutine dispatcher
        val taskId = withContext(coroutineDispatcher) {
            UUID.randomUUID().toString()
        }
        val task = Task(
            title = title,
            description = description,
            id = taskId,
        )
        taskDao.upsert(task.toLocal())
        saveTasksToNetwork()
        return task
    }

    override suspend fun updateTask(taskId: String, title: String, description: String) {
        val task = getTask(taskId)?.copy(
            title = title,
            description = description
        ) ?: throw Exception("Task (id $taskId) not found")

        taskDao.upsert(task.toLocal())
        saveTasksToNetwork()
    }

    override suspend fun getTasks(forceUpdate: Boolean): List<Task> {
        if (forceUpdate) {
            loadTasksFromNetwork()
        }
        return withContext(expensiveWorkDispatcher) {
            taskDao.getAll().toExternal()
        }
    }

    override suspend fun refreshTasks() {
        loadTasksFromNetwork()
    }

    override fun getTasksStream(): Flow<List<Task>> {
        return taskDao.observeAll().map { tasks ->
            withContext(expensiveWorkDispatcher) {
                tasks.toExternal()
            }
        }
    }

    override suspend fun refreshTask(taskId: String) {
        loadTasksFromNetwork()
    }

    override fun getTaskStream(taskId: String): Flow<Task?> {
        return taskDao.observeById(taskId).map { it.toExternal() }
    }

    /**
     * Get a Task with the given ID. Will return null if the task cannot be found.
     *
     * @param taskId - The ID of the task
     * @param forceUpdate - true if the task should be updated from the network data source first.
     */
    override suspend fun getTask(taskId: String, forceUpdate: Boolean): Task? {
        if (forceUpdate) {
            loadTasksFromNetwork()
        }
        return taskDao.getById(taskId)?.toExternal()
    }

    override suspend fun completeTask(taskId: String) {
        taskDao.updateCompleted(taskId = taskId, completed = true)
        saveTasksToNetwork()
    }

    override suspend fun activateTask(taskId: String) {
        taskDao.updateCompleted(taskId = taskId, completed = false)
        saveTasksToNetwork()
    }

    override suspend fun clearCompletedTasks() {
        taskDao.deleteCompleted()
        saveTasksToNetwork()
    }

    override suspend fun deleteAllTasks() {
        taskDao.deleteAll()
        saveTasksToNetwork()
    }

    override suspend fun deleteTask(taskId: String) {
        taskDao.deleteById(taskId)
        saveTasksToNetwork()
    }

    /**
     * The following methods load tasks from, and save tasks to, the network.
     *
     * Consider these to be long running operations, hence the need for `withContext` which
     * can change the coroutine dispatcher so that the caller isn't blocked.
     *
     * Real apps may want to do a proper sync, rather than the "one-way sync everything" approach
     * below. See https://developer.android.com/topic/architecture/data-layer/offline-first
     * for more efficient and robust synchronisation strategies.
     *
     * Also, in a real app, these operations could be scheduled using WorkManager.
     */
    private suspend fun loadTasksFromNetwork() {
        withContext(expensiveWorkDispatcher) {
            val remoteTasks = tasksNetworkDataSource.loadTasks()
            taskDao.deleteAll()
            taskDao.upsertAll(remoteTasks.toLocal())
        }
    }

    private fun saveTasksToNetwork() {
        fireAndForgetScope.launch {
            val localTasks = taskDao.getAll()
            tasksNetworkDataSource.saveTasks(localTasks.toNetwork())
        }
    }
}
