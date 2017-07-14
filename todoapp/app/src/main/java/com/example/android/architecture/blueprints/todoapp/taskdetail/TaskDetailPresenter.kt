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
package com.example.android.architecture.blueprints.todoapp.taskdetail

import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository

/**
 * Listens to user actions from the UI ([TaskDetailFragment]), retrieves the data and updates
 * the UI as required.
 */
class TaskDetailPresenter(
        private val taskId: String,
        private val tasksRepository: TasksRepository,
        private val taskDetailView: TaskDetailContract.View
) : TaskDetailContract.Presenter {

    init {
        taskDetailView.presenter = this
    }

    override fun start() {
        openTask()
    }

    private fun openTask() {
        if (taskId.isEmpty()) {
            taskDetailView.showMissingTask()
            return
        }

        taskDetailView.setLoadingIndicator(true)
        tasksRepository.getTask(taskId, object : TasksDataSource.GetTaskCallback {
            override fun onTaskLoaded(task: Task) {
                with(taskDetailView) {
                    // The view may not be able to handle UI updates anymore
                    if (!isActive) {
                        return@onTaskLoaded
                    }
                    setLoadingIndicator(false)
                }
                showTask(task)
            }

            override fun onDataNotAvailable() {
                with(taskDetailView) {
                    // The view may not be able to handle UI updates anymore
                    if (!isActive) {
                        return@onDataNotAvailable
                    }
                    showMissingTask()
                }
            }
        })
    }

    override fun editTask() {
        if (taskId.isEmpty()) {
            taskDetailView.showMissingTask()
            return
        }
        taskDetailView.showEditTask(taskId)
    }

    override fun deleteTask() {
        if (taskId.isEmpty()) {
            taskDetailView.showMissingTask()
            return
        }
        tasksRepository.deleteTask(taskId)
        taskDetailView.showTaskDeleted()
    }

    override fun completeTask() {
        if (taskId.isEmpty()) {
            taskDetailView.showMissingTask()
            return
        }
        tasksRepository.completeTask(taskId)
        taskDetailView.showTaskMarkedComplete()
    }

    override fun activateTask() {
        if (taskId.isEmpty()) {
            taskDetailView.showMissingTask()
            return
        }
        tasksRepository.activateTask(taskId)
        taskDetailView.showTaskMarkedActive()
    }

    private fun showTask(task: Task) {
        with(taskDetailView) {
            if (taskId.isEmpty()) {
                hideTitle()
            } else {
                showTitle(task.title)
            }

            if (taskId.isEmpty()) {
                hideDescription()
            } else {
                showDescription(task.description)
            }
            showCompletionStatus(task.isCompleted)
        }
    }
}
