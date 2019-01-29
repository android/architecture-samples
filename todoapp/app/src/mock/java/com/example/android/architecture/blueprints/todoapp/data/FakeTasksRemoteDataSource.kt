/*
 * Copyright 2017, The Android Open Source Project
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
package com.example.android.architecture.blueprints.todoapp.data

import androidx.annotation.VisibleForTesting
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource
import com.google.common.collect.Lists

/**
 * Implementation of a remote data source with static access to the data for easy testing.
 */
class FakeTasksRemoteDataSource private constructor() : TasksDataSource {

    private val TASKS_SERVICE_DATA = LinkedHashMap<String, Task>()

    override fun getTasks(callback: TasksDataSource.LoadTasksCallback) {
        callback.onTasksLoaded(Lists.newArrayList(TASKS_SERVICE_DATA.values))
    }

    override fun getTask(taskId: String, callback: TasksDataSource.GetTaskCallback) {
        val task = TASKS_SERVICE_DATA[taskId]
        if (task != null) {
            callback.onTaskLoaded(task)
        } else {
            callback.onDataNotAvailable()
        }
    }

    override fun saveTask(task: Task) {
        TASKS_SERVICE_DATA.put(task.id, task)
    }

    override fun completeTask(task: Task) {
        val completedTask = Task(task.title, task.description, task.id).apply {
            isCompleted = true
        }
        TASKS_SERVICE_DATA.put(task.id, completedTask)
    }

    override fun completeTask(taskId: String) {
        // Not required for the remote data source.
    }

    override fun activateTask(task: Task) {
        val activeTask = Task(task.title, task.description, task.id)
        TASKS_SERVICE_DATA.put(task.id, activeTask)
    }

    override fun activateTask(taskId: String) {
        // Not required for the remote data source.
    }

    override fun clearCompletedTasks() {
        with(TASKS_SERVICE_DATA.entries.iterator()) {
            while (hasNext()) {
                if (next().value.isCompleted) {
                    remove()
                }
            }
        }
    }

    override fun refreshTasks() {
        // Not required because the {@link TasksRepository} handles the logic of refreshing the
        // tasks from all the available data sources.
    }

    override fun deleteTask(taskId: String) {
        TASKS_SERVICE_DATA.remove(taskId)
    }

    override fun deleteAllTasks() {
        TASKS_SERVICE_DATA.clear()
    }

    @VisibleForTesting fun addTasks(vararg tasks: Task) {
        for (task in tasks) {
            TASKS_SERVICE_DATA.put(task.id, task)
        }
    }

    companion object {

        private lateinit var INSTANCE: FakeTasksRemoteDataSource
        private var needsNewInstance = true

        @JvmStatic fun getInstance(): FakeTasksRemoteDataSource {
            if (needsNewInstance) {
                INSTANCE = FakeTasksRemoteDataSource()
                needsNewInstance = false
            }
            return INSTANCE
        }
    }
}
