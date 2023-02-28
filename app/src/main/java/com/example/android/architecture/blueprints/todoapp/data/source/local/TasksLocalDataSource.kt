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

import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource

/**
 * Concrete implementation of a data source as a db.
 */
class TasksLocalDataSource internal constructor(
    private val tasksDao: TasksDao
) : TasksDataSource {

    override fun getTasksStream() = tasksDao.observeTasks()

    override fun getTaskStream(taskId: String) = tasksDao.observeTaskById(taskId)

    override suspend fun refreshTask(taskId: String) {
        // NO-OP
    }

    override suspend fun refreshTasks() {
        // NO-OP
    }

    override suspend fun getTasks(): List<Task> = tasksDao.getTasks()

    override suspend fun getTask(taskId: String): Task? = tasksDao.getTaskById(taskId)

    override suspend fun saveTask(task: Task) = tasksDao.insertTask(task)

    override suspend fun completeTask(taskId: String) =
        tasksDao.updateCompleted(taskId, true)

    override suspend fun activateTask(taskId: String) =
        tasksDao.updateCompleted(taskId, false)

    override suspend fun clearCompletedTasks() {
        tasksDao.deleteCompletedTasks()
    }

    override suspend fun deleteAllTasks() = tasksDao.deleteTasks()

    override suspend fun deleteTask(taskId: String) {
        tasksDao.deleteTaskById(taskId)
    }
}
