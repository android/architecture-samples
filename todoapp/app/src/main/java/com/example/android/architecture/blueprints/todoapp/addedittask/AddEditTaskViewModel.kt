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
package com.example.android.architecture.blueprints.todoapp.addedittask

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import android.support.annotation.StringRes
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.SingleLiveEvent
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository

/**
 * ViewModel for the Add/Edit screen.
 *
 *
 * This ViewModel only exposes [ObservableField]s, so it doesn't need to extend
 * [android.databinding.BaseObservable] and updates are notified automatically. See
 * [com.example.android.architecture.blueprints.todoapp.statistics.StatisticsViewModel] for
 * how to deal with more complex scenarios.
 */
class AddEditTaskViewModel(
        context: Application,
        private val tasksRepository: TasksRepository
) : AndroidViewModel(context), TasksDataSource.GetTaskCallback {

    val title = ObservableField<String>()
    val description = ObservableField<String>()
    val dataLoading = ObservableBoolean(false)
    internal val snackbarMessage = SingleLiveEvent<Int>()
    internal val taskUpdatedEvent = SingleLiveEvent<Void>()
    private var taskId: String? = null
    private val isNewTask
        get() = taskId == null
    private var isDataLoaded = false
    private var taskCompleted = false

    fun start(taskId: String?) {
        if (dataLoading.get()) {
            // Already loading, ignore.
            return
        }
        this.taskId = taskId
        if (isNewTask || isDataLoaded) {
            // No need to populate, it's a new task or it already has data
            return
        }
        dataLoading.set(true)
        taskId?.let {
            tasksRepository.getTask(it, this)
        }
    }

    override fun onTaskLoaded(task: Task) {
        title.set(task.title)
        description.set(task.description)
        taskCompleted = task.isCompleted
        dataLoading.set(false)
        isDataLoaded = true

        // Note that there's no need to notify that the values changed because we're using
        // ObservableFields.
    }

    override fun onDataNotAvailable() {
        dataLoading.set(false)
    }

    // Called when clicking on fab.
    fun saveTask() {
        val task = Task(title.get()?:"", description.get()?:"")
        if (task.isEmpty) {
            showSnackbarMessage(R.string.empty_task_message)
            return
        }
        if (isNewTask) {
            createTask(task)
        } else {
            taskId?.let {
                updateTask(Task(title.get()?:"", description.get()?:"", it)
                        .apply { isCompleted = taskCompleted })
            }
        }
    }


    private fun createTask(newTask: Task) {
        tasksRepository.saveTask(newTask)
        taskUpdatedEvent.call()
    }

    private fun updateTask(task: Task) {
        if (isNewTask) {
            throw RuntimeException("updateTask() was called but task is new.")
        }
        tasksRepository.saveTask(task)
        taskUpdatedEvent.call()
    }

    private fun showSnackbarMessage(@StringRes message: Int) {
        snackbarMessage.value = message
    }
}
