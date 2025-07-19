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

import androidx.annotation.VisibleForTesting
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/**
 * Implementation of a tasks repository with static access to the data for easy testing.
 */
class FakeTaskRepository : TaskRepository {

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

    override suspend fun refresh() {
        // Tasks already refreshed
    }

    override suspend fun refresh(taskId: String) {
        refresh()
    }

    override suspend fun create(title: String, description: String): String {
        val taskId = generateTaskId()
        Task(title = title, description = description, id = taskId).also {
            saveTask(it)
        }
        return taskId
    }

    override fun observeAll(): Flow<List<Task>> = observableTasks

    override fun observe(taskId: String): Flow<Task?> {
        return observableTasks.map { tasks ->
            return@map tasks.firstOrNull { it.id == taskId }
        }
    }

    override suspend fun get(taskId: String, forceUpdate: Boolean): Task? {
        if (shouldThrowError) {
            throw Exception("Test exception")
        }
        return savedTasks.value[taskId]
    }

    override suspend fun getAll(forceUpdate: Boolean): List<Task> {
        if (shouldThrowError) {
            throw Exception("Test exception")
        }
        return observableTasks.first()
    }

    override suspend fun update(taskId: String, title: String, description: String) {
        val updatedTask = _savedTasks.value[taskId]?.copy(
            title = title,
            description = description
        ) ?: throw Exception("Task (id $taskId) not found")

        saveTask(updatedTask)
    }

    private fun saveTask(task: Task) {
        _savedTasks.update { tasks ->
            val newTasks = LinkedHashMap<String, Task>(tasks)
            newTasks[task.id] = task
            newTasks
        }
    }

    override suspend fun complete(taskId: String) {
        _savedTasks.value[taskId]?.let {
            saveTask(it.copy(isCompleted = true))
        }
    }

    override suspend fun activate(taskId: String) {
        _savedTasks.value[taskId]?.let {
            saveTask(it.copy(isCompleted = false))
        }
    }

    override suspend fun clearAllCompleted() {
        _savedTasks.update { tasks ->
            tasks.filterValues {
                !it.isCompleted
            } as LinkedHashMap<String, Task>
        }
    }

    override suspend fun delete(taskId: String) {
        _savedTasks.update { tasks ->
            val newTasks = LinkedHashMap<String, Task>(tasks)
            newTasks.remove(taskId)
            newTasks
        }
    }

    override suspend fun deleteAll() {
        _savedTasks.update {
            LinkedHashMap()
        }
    }

    private fun generateTaskId() = UUID.randomUUID().toString()

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
