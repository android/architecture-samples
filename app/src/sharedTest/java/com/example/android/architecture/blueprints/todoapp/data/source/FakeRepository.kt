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

import androidx.annotation.VisibleForTesting
import com.example.android.architecture.blueprints.todoapp.data.Task
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/**
 * Implementation of a remote data source with static access to the data for easy testing.
 */
class FakeRepository : TasksRepository {

    private var shouldThrowError = false

    private val _savedTasks = MutableStateFlow(LinkedHashMap<String, Task>())
    val savedTasks: StateFlow<LinkedHashMap<String, Task>> = _savedTasks.asStateFlow()

    private val observableTasks: Flow<List<Task>> = savedTasks.map {
        if (shouldThrowError) {
            throw Exception("Test exception")
        } else {
            it.values.toList()
        }
    }

    fun setShouldThrowError(value: Boolean) {
        shouldThrowError = value
    }

    override suspend fun refreshTasks() {
        // Tasks already refreshed
    }

    override suspend fun refreshTask(taskId: String) {
        refreshTasks()
    }

    override fun getTasksStream(): Flow<List<Task>> = observableTasks

    override fun getTaskStream(taskId: String): Flow<Task?> {
        return observableTasks.map { tasks ->
            return@map tasks.firstOrNull { it.id == taskId }
        }
    }

    override suspend fun getTask(taskId: String, forceUpdate: Boolean): Task? {
        if (shouldThrowError) {
            throw Exception("Test exception")
        }
        return savedTasks.value[taskId]
    }

    override suspend fun getTasks(forceUpdate: Boolean): List<Task> {
        if (shouldThrowError) {
            throw Exception("Test exception")
        }
        return observableTasks.first()
    }

    override suspend fun saveTask(task: Task) {
        _savedTasks.update { tasks ->
            val newTasks = LinkedHashMap<String, Task>(tasks)
            newTasks[task.id] = task
            newTasks
        }
    }

    override suspend fun completeTask(task: Task) {
        val completedTask = Task(task.title, task.description, true, task.id)
        _savedTasks.update { tasks ->
            val newTasks = LinkedHashMap<String, Task>(tasks)
            newTasks[task.id] = completedTask
            newTasks
        }
    }

    override suspend fun completeTask(taskId: String) {
        // Not required for the remote data source.
        throw NotImplementedError()
    }

    override suspend fun activateTask(task: Task) {
        val activeTask = Task(task.title, task.description, false, task.id)
        _savedTasks.update { tasks ->
            val newTasks = LinkedHashMap<String, Task>(tasks)
            newTasks[task.id] = activeTask
            newTasks
        }
    }

    override suspend fun activateTask(taskId: String) {
        throw NotImplementedError()
    }

    override suspend fun clearCompletedTasks() {
        _savedTasks.update { tasks ->
            tasks.filterValues {
                !it.isCompleted
            } as LinkedHashMap<String, Task>
        }
    }

    override suspend fun deleteTask(taskId: String) {
        _savedTasks.update { tasks ->
            val newTasks = LinkedHashMap<String, Task>(tasks)
            newTasks.remove(taskId)
            newTasks
        }
    }

    override suspend fun deleteAllTasks() {
        _savedTasks.update {
            LinkedHashMap()
        }
    }

    @VisibleForTesting
    fun addTasks(vararg tasks: Task) {
        _savedTasks.update { oldTasks ->
            val newTasks = LinkedHashMap<String, Task>(oldTasks)
            for (task in tasks) {
                newTasks[task.id] = task
            }
            newTasks
        }
    }
}
