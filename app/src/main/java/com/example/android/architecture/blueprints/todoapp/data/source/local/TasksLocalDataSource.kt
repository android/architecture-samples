/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.architecture.blueprints.todoapp.data.source.local

import androidx.annotation.VisibleForTesting
import com.example.android.architecture.blueprints.todoapp.data.Result
import com.example.android.architecture.blueprints.todoapp.data.Result.Error
import com.example.android.architecture.blueprints.todoapp.data.Result.Success
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource
import com.example.android.architecture.blueprints.todoapp.util.AppExecutors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Concrete implementation of a data source as a db.
 */
class TasksLocalDataSource private constructor(
        private val appExecutors: AppExecutors,
        private val tasksDao: TasksDao
) : TasksDataSource {

    override suspend fun getTasks(): Result<List<Task>> = withContext(Dispatchers.IO) {
        return@withContext try {
            Success(tasksDao.getTasks())
        } catch(e: Exception) {
            Error(e)
        }
    }

    override suspend fun getTask(taskId: String): Result<Task> = withContext(Dispatchers.IO) {
        //TODO
        return@withContext try {
            tasksDao.getTaskById(taskId)?.let {
                Success(it)
            }
            Error(Exception("Task not found!"))
        } catch(e: Exception) {
            Error(e)
        }
    }

    override suspend fun saveTask(task: Task) {
        appExecutors.diskIO.execute { tasksDao.insertTask(task) }
    }

    override suspend fun completeTask(task: Task) {
        appExecutors.diskIO.execute { tasksDao.updateCompleted(task.id, true) }
    }

    override suspend fun completeTask(taskId: String) {
        // Not required for the local data source because the {@link DefaultTasksRepository} handles
        // converting from a {@code taskId} to a {@link task} using its cached data.
    }

    override suspend fun activateTask(task: Task) {
        appExecutors.diskIO.execute { tasksDao.updateCompleted(task.id, false) }
    }

    override suspend fun activateTask(taskId: String) {
        // Not required for the local data source because the {@link DefaultTasksRepository} handles
        // converting from a {@code taskId} to a {@link task} using its cached data.
    }

    override suspend fun clearCompletedTasks() {
        appExecutors.diskIO.execute { tasksDao.deleteCompletedTasks() }
    }

    override suspend fun deleteAllTasks() {
        appExecutors.diskIO.execute { tasksDao.deleteTasks() }
    }

    override suspend fun deleteTask(taskId: String) {
        appExecutors.diskIO.execute { tasksDao.deleteTaskById(taskId) }
    }

    companion object {
        private var INSTANCE: TasksLocalDataSource? = null

        @JvmStatic
        fun getInstance(appExecutors: AppExecutors, tasksDao: TasksDao): TasksLocalDataSource {
            if (INSTANCE == null) {
                synchronized(TasksLocalDataSource::javaClass) {
                    INSTANCE = TasksLocalDataSource(appExecutors, tasksDao)
                }
            }
            return INSTANCE!!
        }

        @VisibleForTesting
        fun clearInstance() {
            INSTANCE = null
        }
    }
}
