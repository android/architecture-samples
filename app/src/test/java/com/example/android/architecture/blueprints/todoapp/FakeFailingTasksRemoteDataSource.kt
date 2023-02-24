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

package com.example.android.architecture.blueprints.todoapp

import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

object FakeFailingTasksRemoteDataSource : TasksDataSource {
    override suspend fun getTasks(): List<Task> {
        throw Exception("Test")
    }

    override suspend fun getTask(taskId: String): Task? {
        throw Exception("Test")
    }

    override fun getTasksStream(): Flow<List<Task>> {
        return flow { emit(getTasks()) }
    }

    override suspend fun refreshTasks() {
        TODO("not implemented")
    }

    override fun getTaskStream(taskId: String): Flow<Task?> {
        return flow { emit(getTask(taskId)) }
    }

    override suspend fun refreshTask(taskId: String) {
        TODO("not implemented")
    }

    override suspend fun saveTask(task: Task) {
        TODO("not implemented")
    }

    override suspend fun completeTask(task: Task) {
        TODO("not implemented")
    }

    override suspend fun completeTask(taskId: String) {
        TODO("not implemented")
    }

    override suspend fun activateTask(task: Task) {
        TODO("not implemented")
    }

    override suspend fun activateTask(taskId: String) {
        TODO("not implemented")
    }

    override suspend fun clearCompletedTasks() {
        TODO("not implemented")
    }

    override suspend fun deleteAllTasks() {
        TODO("not implemented")
    }

    override suspend fun deleteTask(taskId: String) {
        TODO("not implemented")
    }
}
