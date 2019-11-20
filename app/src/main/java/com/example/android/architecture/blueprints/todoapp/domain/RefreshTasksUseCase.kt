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

package com.example.android.architecture.blueprints.todoapp.domain

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.example.android.architecture.blueprints.todoapp.data.Result
import com.example.android.architecture.blueprints.todoapp.data.Result.Success
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository
import com.example.android.architecture.blueprints.todoapp.tasks.TasksFilterType
import com.example.android.architecture.blueprints.todoapp.tasks.TasksFilterType.ACTIVE_TASKS
import com.example.android.architecture.blueprints.todoapp.tasks.TasksFilterType.ALL_TASKS
import com.example.android.architecture.blueprints.todoapp.tasks.TasksFilterType.COMPLETED_TASKS

class ObserveTasksUseCase(
    private val tasksRepository: TasksRepository
) {
    operator fun invoke(
        currentFiltering: TasksFilterType = ALL_TASKS
    ): LiveData<Result<List<Task>>> {
        return tasksRepository.observeTasks().map {

            if (it is Success && currentFiltering != ALL_TASKS) {
                val tasksToShow = mutableListOf<Task>()
                // We filter the tasks based on the requestType
                for (task in it.data) {
                    when (currentFiltering) {
                        ACTIVE_TASKS -> if (task.isActive) {
                            tasksToShow.add(task)
                        }
                        COMPLETED_TASKS -> if (task.isCompleted) {
                            tasksToShow.add(task)
                        }
                        else -> throw NotImplementedError()
                    }
                }
                Success(tasksToShow)
            } else {
                it
            }
        }
    }
}
