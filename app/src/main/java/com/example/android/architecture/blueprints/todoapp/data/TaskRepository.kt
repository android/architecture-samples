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

    fun observeAll(): Flow<List<Task>>

    suspend fun getAll(forceUpdate: Boolean = false): List<Task>

    suspend fun refresh()

    fun observe(taskId: String): Flow<Task?>

    suspend fun get(taskId: String, forceUpdate: Boolean = false): Task?

    suspend fun refresh(taskId: String)

    suspend fun create(title: String, description: String): String

    suspend fun update(taskId: String, title: String, description: String)

    suspend fun complete(taskId: String)

    suspend fun activate(taskId: String)

    suspend fun clearAllCompleted()

    suspend fun deleteAll()

    suspend fun delete(taskId: String)
}
