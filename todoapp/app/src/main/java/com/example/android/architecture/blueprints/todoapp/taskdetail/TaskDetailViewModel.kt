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

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.annotation.StringRes
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.SingleLiveEvent
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository
import com.example.android.architecture.blueprints.todoapp.tasks.TasksFragment


/**
 * Listens to user actions from the list item in ([TasksFragment]) and redirects them to the
 * Fragment's actions listener.
 */
class TaskDetailViewModel(
        context: Application,
        private val tasksRepository: TasksRepository
) : AndroidViewModel(context), TasksDataSource.GetTaskCallback {

    val task = ObservableField<Task>()
    val completed = ObservableBoolean()
    val editTaskCommand = SingleLiveEvent<Void>()
    val deleteTaskCommand = SingleLiveEvent<Void>()
    val snackbarMessage = SingleLiveEvent<Int>()
    var isDataLoading = false
        private set
    val isDataAvailable
        get() = task.get() != null

    fun deleteTask() {
        task.get()?.let {
            tasksRepository.deleteTask(it.id)
            deleteTaskCommand.call()
        }
    }

    fun editTask() {
        editTaskCommand.call()
    }

    fun setCompleted(completed: Boolean) {
        if (isDataLoading) {
            return
        }
        val task = this.task.get().apply {
            isCompleted = completed
        }
        if (completed) {
            tasksRepository.completeTask(task)
            showSnackbarMessage(R.string.task_marked_complete)
        } else {
            tasksRepository.activateTask(task)
            showSnackbarMessage(R.string.task_marked_active)
        }
    }

    fun start(taskId: String?) {
        taskId?.let {
            isDataLoading = true
            tasksRepository.getTask(it, this)
        }
    }

    fun setTask(task: Task) {
        this.task.set(task)
        completed.set(task.isCompleted)
    }

    override fun onTaskLoaded(task: Task) {
        setTask(task)
        isDataLoading = false
    }

    override fun onDataNotAvailable() {
        task.set(null)
        isDataLoading = false
    }

    fun onRefresh() {
        if (task.get() != null) {
            start(task.get().id)
        }
    }

    private fun showSnackbarMessage(@StringRes message: Int) {
        snackbarMessage.value = message
    }
}
