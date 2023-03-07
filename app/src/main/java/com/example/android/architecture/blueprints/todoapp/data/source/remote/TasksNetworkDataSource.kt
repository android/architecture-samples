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

package com.example.android.architecture.blueprints.todoapp.data.source.remote

import com.example.android.architecture.blueprints.todoapp.data.source.NetworkDataSource
import kotlinx.coroutines.delay

/**
 * Implementation of the data source that adds a latency simulating network.
 *
 */
object TasksNetworkDataSource : NetworkDataSource {

    private const val SERVICE_LATENCY_IN_MILLIS = 2000L

    private var TASKS_SERVICE_DATA = LinkedHashMap<String, NetworkTask>(2)

    init {
        addTask(
            id = "PISA",
            title = "Build tower in Pisa",
            shortDescription = "Ground looks good, no foundation work required."
        )
        addTask(
            id = "TACOMA",
            title = "Finish bridge in Tacoma",
            shortDescription = "Found awesome girders at half the cost!"
        )
    }

    override suspend fun loadTasks(): List<NetworkTask> {
        // Simulate network by delaying the execution.
        val tasks = TASKS_SERVICE_DATA.values.toList()
        delay(SERVICE_LATENCY_IN_MILLIS)
        return tasks
    }

    override suspend fun getTask(taskId: String): NetworkTask? {
        // Simulate network by delaying the execution.
        delay(SERVICE_LATENCY_IN_MILLIS)
        return TASKS_SERVICE_DATA[taskId]
    }

    private fun addTask(id: String, title: String, shortDescription: String) {
        val newTask = NetworkTask(id = id, title = title, shortDescription = shortDescription)
        TASKS_SERVICE_DATA[newTask.id] = newTask
    }

    override suspend fun saveTask(task: NetworkTask) {
        TASKS_SERVICE_DATA[task.id] = task
    }

    override suspend fun completeTask(taskId: String) {
        TASKS_SERVICE_DATA[taskId]?.let {
            saveTask(it.copy(status = TaskStatus.COMPLETE))
        }
    }

    override suspend fun activateTask(taskId: String) {
        TASKS_SERVICE_DATA[taskId]?.let {
            saveTask(it.copy(status = TaskStatus.ACTIVE))
        }
    }

    override suspend fun clearCompletedTasks() {
        TASKS_SERVICE_DATA = TASKS_SERVICE_DATA.filterValues {
            it.status == TaskStatus.COMPLETE
        } as LinkedHashMap<String, NetworkTask>
    }

    override suspend fun deleteAllTasks() {
        TASKS_SERVICE_DATA.clear()
    }

    override suspend fun deleteTask(taskId: String) {
        TASKS_SERVICE_DATA.remove(taskId)
    }
}
