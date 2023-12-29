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

package com.example.android.architecture.blueprints.todoapp.data.source.local

import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.coroutines.flow.Flow

class FakeTaskDao(initialTasks: List<LocalTask>? = emptyList()) : TaskDao {

    private var _tasks: MutableMap<String, LocalTask>? = null

    var tasks: List<LocalTask>?
        get() = _tasks?.values?.toList()
        set(newTasks) {
            _tasks = newTasks?.associateBy { it.id }?.toMutableMap()
        }

    init {
        tasks = initialTasks
    }

    override suspend fun getAll() = tasks ?: throw Exception("Task list is null")

    override suspend fun getById(taskId: String): LocalTask? = _tasks?.get(taskId)

    override suspend fun upsertAll(tasks: List<LocalTask>) {
        _tasks?.putAll(tasks.associateBy { it.id })
    }

    override suspend fun upsert(task: LocalTask) {
        _tasks?.put(task.id, task)
    }

    override suspend fun updateCompleted(taskId: String, completed: Boolean) {
        _tasks?.get(taskId)?.let { it.isCompleted = completed }
    }

    override suspend fun deleteAll() {
        _tasks?.clear()
    }

    override suspend fun deleteById(taskId: String): Int {
        return if (_tasks?.remove(taskId) == null) {
            0
        } else {
            1
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override suspend fun deleteCompleted(): Int {
        _tasks?.apply {
            val originalSize = size
            entries.removeIf { it.value.isCompleted }
            return originalSize - size
        }
        return 0
    }

    override fun observeAll(): Flow<List<LocalTask>> {
        TODO("Not implemented")
    }

    override fun observeById(taskId: String): Flow<LocalTask> {
        TODO("Not implemented")
    }
}
