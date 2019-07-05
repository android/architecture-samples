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
package com.example.android.architecture.blueprints.todoapp.taskdetail

import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.android.architecture.blueprints.todoapp.Event
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.data.Result
import com.example.android.architecture.blueprints.todoapp.data.Result.Success
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository
import kotlinx.coroutines.launch

/**
 * This ViewModel exposes a Task via a LiveData. When any of the initial parameters or the data in
 * the data layer change, the task will be updated automatically.
 */
class TaskDetailViewModel(
    private val tasksRepository: TasksRepository
) : ViewModel() {


    private val _params = MutableLiveData<Pair<String, Boolean>>()

    private val _task = _params.switchMap { (taskId, forceUpdate) ->
        if (forceUpdate) {
            viewModelScope.launch {
                tasksRepository.refreshTasks()
            }
        }
        tasksRepository.observeTask(taskId).switchMap { computeResult(it) }

    }
    val task: LiveData<Task> = _task

    private val _isDataAvailable = MutableLiveData<Boolean>()
    val isDataAvailable: LiveData<Boolean> = _isDataAvailable

    private val _dataLoading = MutableLiveData<Boolean>()
    val dataLoading: LiveData<Boolean> = _dataLoading

    private val _editTaskCommand = MutableLiveData<Event<Unit>>()
    val editTaskCommand: LiveData<Event<Unit>> = _editTaskCommand

    private val _deleteTaskCommand = MutableLiveData<Event<Unit>>()
    val deleteTaskCommand: LiveData<Event<Unit>> = _deleteTaskCommand

    private val _snackbarText = MutableLiveData<Event<Int>>()
    val snackbarMessage: LiveData<Event<Int>> = _snackbarText

    // This LiveData depends on another so we can use a transformation.
    val completed: LiveData<Boolean> = Transformations.map(_task) { input: Task? ->
        input?.isCompleted ?: false
    }

    fun deleteTask() = viewModelScope.launch {
        _params.value?.first?.let {
            tasksRepository.deleteTask(it)
            _deleteTaskCommand.value = Event(Unit)
        }
    }

    fun editTask() {
        _editTaskCommand.value = Event(Unit)
    }

        fun setCompleted(completed: Boolean) = viewModelScope.launch {
        val task = _task.value ?: return@launch
        if (completed) {
            tasksRepository.completeTask(task)
            showSnackbarMessage(R.string.task_marked_complete)
        } else {
            tasksRepository.activateTask(task)
            showSnackbarMessage(R.string.task_marked_active)
        }
    }

    fun start(taskId: String?, forceRefresh: Boolean = true) {
        if (_isDataAvailable.value == true && !forceRefresh || _dataLoading.value == true) {
            return
        }
        if (taskId == null) {
            _isDataAvailable.value = false
            return
        }

        // Show loading indicator
        _dataLoading.value = true

        _params.value = Pair(taskId, forceRefresh)
    }

    private fun computeResult(taskResult: Result<Task>): LiveData<Task> {

        _dataLoading.value = true
        // TODO: This is a good case for liveData builder. Replace when stable.
        val result = MutableLiveData<Task>()

        if (taskResult is Success) {
            result.value = taskResult.data
            _isDataAvailable.value = true
        } else {
            result.value = null
            showSnackbarMessage(R.string.loading_tasks_error)
            _isDataAvailable.value = false
        }

        _dataLoading.value = false
        return result
    }


    fun refresh() {
        // Recreate the parameters to force a new data load.
        _params.value = _params.value?.copy(second = true)
    }

    private fun showSnackbarMessage(@StringRes message: Int) {
        _snackbarText.value = Event(message)
    }
}
