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
import com.example.android.architecture.blueprints.todoapp.di.ApplicationScope
import com.example.android.architecture.blueprints.todoapp.di.DefaultDispatcher
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Default implementation of [TaskRepository]. Single entry point for managing tasks' data.
 *
 * @param networkDataSource - The network data source
 * @param localDataSource - The local data source
 * @param dispatcher - The dispatcher to be used for long running or complex operations, such as ID
 * generation or mapping many models.
 * @param scope - The coroutine scope used for deferred jobs where the result isn't important, such
 * as sending data to the network.
 */
@Singleton
class DefaultTaskRepository @Inject constructor(
    private val networkDataSource: NetworkDataSource,
    private val localDataSource: TaskDao,
    @DefaultDispatcher private val dispatcher: CoroutineDispatcher,
    @ApplicationScope scope: CoroutineScope,
) : TaskRepository {

    private val networkSyncEventQueue = Channel<Unit>()
    init {
        scope.launch {
            for (syncEvent in networkSyncEventQueue) saveTasksToNetwork()
        }
    }

    /**
     * Send the tasks from the local data source to the network data source.
     *
     * Real apps may want to use WorkManager to schedule this work, and provide a mechanism for
     * failures to be communicated back to the user so that they are aware that their data isn't
     * being backed up.
     * */
    private suspend fun saveTasksToNetwork() {
        try {
            val localTasks = localDataSource.getAll()
            val networkTasks = withContext(dispatcher) {
                localTasks.toNetwork()
            }
            networkDataSource.saveTasks(networkTasks)
        } catch (e: Exception) {
            // In a real app you'd handle the exception e.g. by exposing a `networkStatus` flow
            // to an app level UI state holder which could then display a Toast message.
        }
    }

    /**
     * Inform this repository that the local data needs sending to the network.
     */
    private fun sendNetworkSyncEvent() {
        networkSyncEventQueue.trySend(Unit)
    }

    override suspend fun createTask(title: String, description: String): String {
        // ID creation might be a complex operation so it's executed using the supplied
        // coroutine dispatcher
        val taskId = withContext(dispatcher) {
            UUID.randomUUID().toString()
        }
        val task = Task(
            title = title,
            description = description,
            id = taskId,
        )
        localDataSource.upsert(task.toLocal())
        sendNetworkSyncEvent()
        return taskId
    }

    override suspend fun updateTask(taskId: String, title: String, description: String) {
        val task = getTask(taskId)?.copy(
            title = title,
            description = description
        ) ?: throw Exception("Task (id $taskId) not found")

        localDataSource.upsert(task.toLocal())
        sendNetworkSyncEvent()
    }

    override suspend fun getTasks(forceUpdate: Boolean): List<Task> {
        if (forceUpdate) {
            refresh()
        }
        return withContext(dispatcher) {
            localDataSource.getAll().toExternal()
        }
    }

    override fun getTasksStream(): Flow<List<Task>> {
        return localDataSource.observeAll().map { tasks ->
            withContext(dispatcher) {
                tasks.toExternal()
            }
        }
    }

    override suspend fun refreshTask(taskId: String) {
        refresh()
    }

    override fun getTaskStream(taskId: String): Flow<Task?> {
        return localDataSource.observeById(taskId).map { it.toExternal() }
    }

    /**
     * Get a Task with the given ID. Will return null if the task cannot be found.
     *
     * @param taskId - The ID of the task
     * @param forceUpdate - true if the task should be updated from the network data source first.
     */
    override suspend fun getTask(taskId: String, forceUpdate: Boolean): Task? {
        if (forceUpdate) {
            refresh()
        }
        return localDataSource.getById(taskId)?.toExternal()
    }

    override suspend fun completeTask(taskId: String) {
        localDataSource.updateCompleted(taskId = taskId, completed = true)
        sendNetworkSyncEvent()
    }

    override suspend fun activateTask(taskId: String) {
        localDataSource.updateCompleted(taskId = taskId, completed = false)
        sendNetworkSyncEvent()
    }

    override suspend fun clearCompletedTasks() {
        localDataSource.deleteCompleted()
        sendNetworkSyncEvent()
    }

    override suspend fun deleteAllTasks() {
        localDataSource.deleteAll()
        sendNetworkSyncEvent()
    }

    override suspend fun deleteTask(taskId: String) {
        localDataSource.deleteById(taskId)
        sendNetworkSyncEvent()
    }

    /**
     * The following methods load tasks from (refresh), and save tasks to, the network.
     *
     * Real apps may want to do a proper sync, rather than the "one-way sync everything" approach
     * below. See https://developer.android.com/topic/architecture/data-layer/offline-first
     * for more efficient and robust synchronisation strategies.
     *
     * Note that the refresh operation is a suspend function (forces callers to wait) and the save
     * operation is not. It returns immediately so callers don't have to wait.
     */

    /**
     * Delete everything in the local data source and replace it with everything from the network
     * data source.
     *
     * `withContext` is used here in case the bulk `toLocal` mapping operation is complex.
     */
    override suspend fun refresh() {
        withContext(dispatcher) {
            val remoteTasks = networkDataSource.loadTasks()
            localDataSource.deleteAll()
            localDataSource.upsertAll(remoteTasks.toLocal())
        }
    }
}
