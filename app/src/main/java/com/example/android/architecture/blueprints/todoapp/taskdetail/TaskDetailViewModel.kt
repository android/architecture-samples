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

import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.architecture.blueprints.todoapp.Event
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.data.Result
import com.example.android.architecture.blueprints.todoapp.data.Result.Success
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository
import kotlinx.coroutines.launch

/**
 * Listens to user actions from the list item in ([TasksFragment]) and redirects them to the
 * Fragment's actions listener.
 */
class TaskDetailViewModel(private val tasksRepository: TasksRepository) :
    ViewModel() {

    private val _task = MutableLiveData<Task>()
    val task: LiveData<Task>
        get() = _task

    private val _isDataAvailable = MutableLiveData<Boolean>()
    val isDataAvailable: LiveData<Boolean>
        get() = _isDataAvailable

    private val _dataLoading = MutableLiveData<Boolean>()
    val dataLoading: LiveData<Boolean>
        get() = _dataLoading

    private val _editTaskCommand = MutableLiveData<Event<Unit>>()
    val editTaskCommand: LiveData<Event<Unit>>
        get() = _editTaskCommand

    private val _deleteTaskCommand = MutableLiveData<Event<Unit>>()
    val deleteTaskCommand: LiveData<Event<Unit>>
        get() = _deleteTaskCommand

    private val _snackbarText = MutableLiveData<Event<Int>>()
    val snackbarMessage: LiveData<Event<Int>>
        get() = _snackbarText

    // This LiveData depends on another so we can use a transformation.
    val completed: LiveData<Boolean> = Transformations.map(_task) { input: Task? ->
        input?.isCompleted ?: false
    }

    val taskId: String?
        get() = _task.value?.id

    fun deleteTask() {
        taskId?.let {
            tasksRepository.deleteTask(it)
            _deleteTaskCommand.value = Event(Unit)

        }
    }

    fun editTask() {
        _editTaskCommand.value = Event(Unit)
    }

    fun setCompleted(completed: Boolean) {
        val task = _task.value ?: return
        if (completed) {
            tasksRepository.completeTask(task)
            showSnackbarMessage(R.string.task_marked_complete)
        } else {
            tasksRepository.activateTask(task)
            showSnackbarMessage(R.string.task_marked_active)
        }
    }

    fun start(taskId: String?) {
        if (taskId != null) {
            _dataLoading.value = true
            viewModelScope.launch {
                tasksRepository.getTask(taskId).let { result ->
                    if (result is Success) {
                        onTaskLoaded(result.data)
                    } else {
                        onDataNotAvailable(result)
                    }
                }
            }
        }
    }

    private fun setTask(task: Task?) {
        this._task.value = task
        _isDataAvailable.value = task != null
    }

    fun onTaskLoaded(task: Task) {
        setTask(task)
        _dataLoading.value = false
    }

    fun onDataNotAvailable(result: Result<Task>) {
        _task.value = null
        _dataLoading.value = false
        _isDataAvailable.value = false
    }

    fun onRefresh() {
        taskId?.let { start(it) }
    }

    private fun showSnackbarMessage(@StringRes message: Int) {
        _snackbarText.value = Event(message)
    }
}
