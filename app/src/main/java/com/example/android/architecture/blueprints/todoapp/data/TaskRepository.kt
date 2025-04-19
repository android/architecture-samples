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

import kotlinx.coroutines.flow.Flow

/**
 * Interface to the data layer.
 */
interface TaskRepository {
    /**
     * Get Flow(stream) of List<Task> from local data source
     */
    fun getTasksStream(): Flow<List<Task>>

    /**
     * get List<Task> for local data source
     * @param forceUpdate: If true, call refresh() to clear local data source and replace
     * with data from remote data source
     */
    suspend fun getTasks(forceUpdate: Boolean = false): List<Task>

    /**
     * Delete everything in the local data source and replace it with everything from the network
     * data source.
     */
    suspend fun refresh()

    /**
     * Observes a single task
     * @param taskId: task to observe
     */
    fun getTaskStream(taskId: String): Flow<Task?>

    /**
     * Get a Task with the given ID. Will return null if the task cannot be found.
     *
     * @param taskId - The ID of the task
     * @param forceUpdate - true if the task should be updated from the network data source first.
     */
    suspend fun getTask(taskId: String, forceUpdate: Boolean = false): Task?

    /**
     * Simply calls refresh()
     * @param taskId - currently doesn't have a use.
     */
    suspend fun refreshTask(taskId: String)

    /**
     * Insert or update a task in the database. If a task already exists, replace it.
     */
    suspend fun createTask(title: String, description: String): String

    suspend fun updateTask(taskId: String, title: String, description: String)

    suspend fun completeTask(taskId: String)

    suspend fun activateTask(taskId: String)

    suspend fun clearCompletedTasks()

    suspend fun deleteAllTasks()

    suspend fun deleteTask(taskId: String)
}
