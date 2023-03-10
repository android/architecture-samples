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

import com.example.android.architecture.blueprints.todoapp.data.source.local.LocalTask
import com.example.android.architecture.blueprints.todoapp.data.source.local.TasksDao
import kotlinx.coroutines.flow.Flow

class FakeTasksDao(var tasks: MutableList<LocalTask>? = mutableListOf()) : TasksDao {

    override suspend fun getTasks() = tasks ?: throw Exception("Task list is null")

    override suspend fun getTaskById(taskId: String): LocalTask? =
        tasks?.firstOrNull { it.id == taskId }

    override suspend fun insertTask(task: LocalTask) {
        tasks?.add(task)
    }

    override suspend fun updateTask(task: LocalTask): Int {
        tasks?.apply {
            val didTaskExist = removeIf { it.id == task.id }
            if (didTaskExist) {
                if (add(task)) {
                    return 1
                }
            }
        }
        return 0
    }

    override suspend fun updateCompleted(taskId: String, completed: Boolean) {
        tasks?.firstOrNull { it.id == taskId }?.let { it.isCompleted = completed }
    }

    override suspend fun deleteTasks() {
        tasks?.clear()
    }

    override suspend fun deleteTaskById(taskId: String): Int {
        val wasDeleted = tasks?.removeIf { it.id == taskId } ?: false
        return if (wasDeleted) 1 else 0
    }

    override suspend fun deleteCompletedTasks(): Int {
        tasks?.apply {
            val originalSize = size
            if (removeIf { it.isCompleted }) {
                return originalSize - size
            }
        }
        return 0
    }

    override fun observeTasks(): Flow<List<LocalTask>> {
        TODO("Not implemented")
    }

    override fun observeTaskById(taskId: String): Flow<LocalTask> {
        TODO("Not implemented")
    }
}
