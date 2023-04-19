/*
 * Copyright 2023 The Android Open Source Project
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

package com.example.android.architecture.blueprints.todoapp.data.source.local

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeTaskDao(initialTasks: List<LocalTask>) : TaskDao {

    private val _tasks = initialTasks.toMutableList()
    private val tasksStream = MutableStateFlow(_tasks.toList())

    override fun observeAll(): Flow<List<LocalTask>> = tasksStream

    override suspend fun upsert(task: LocalTask) {
        _tasks.removeIf { it.id == task.id }
        _tasks.add(task)
        tasksStream.emit(_tasks)
    }

    override suspend fun upsertAll(tasks: List<LocalTask>) {
        val newTaskIds = tasks.map { it.id }
        _tasks.removeIf { newTaskIds.contains(it.id) }
        _tasks.addAll(tasks)
    }

    override suspend fun updateCompleted(taskId: String, completed: Boolean) {
        _tasks.firstOrNull { it.id == taskId }?.let { it.isCompleted = completed }
        tasksStream.emit(_tasks)
    }

    override suspend fun deleteAll() {
        _tasks.clear()
        tasksStream.emit(_tasks)
    }
}

