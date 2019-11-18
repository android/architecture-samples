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
import androidx.lifecycle.*
import com.example.android.architecture.blueprints.todoapp.Event
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.data.Result
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * ViewModel for the Details screen.
 */
class TaskDetailViewModel(
        private val tasksRepository: TasksRepository
) : ViewModel() {

    private val _taskId = ConflatedBroadcastChannel<String>()

    private val _task = _taskId.asFlow().flatMapLatest { taskId ->
        tasksRepository.observeTask(taskId, true).map { computeResult(it) }
    }.asLiveData(viewModelScope.coroutineContext)

    val task: LiveData<Task?> = _task

    val isDataAvailable: LiveData<Boolean> = _task.map { it != null }

    private val _dataLoading = MutableLiveData<Boolean>()
    val dataLoading: LiveData<Boolean> = _dataLoading

    private val _editTaskEvent = MutableLiveData<Event<Unit>>()
    val editTaskEvent: LiveData<Event<Unit>> = _editTaskEvent

    private val _deleteTaskEvent = MutableLiveData<Event<Unit>>()
    val deleteTaskEvent: LiveData<Event<Unit>> = _deleteTaskEvent

    private val _snackbarText = MutableLiveData<Event<Int>>()
    val snackbarText: LiveData<Event<Int>> = _snackbarText

    // This LiveData depends on another so we can use a transformation.
    val completed: LiveData<Boolean> = _task.map { input: Task? ->
        input?.isCompleted ?: false
    }

    fun deleteTask() = viewModelScope.launch {
        _taskId.value?.let {
            tasksRepository.deleteTask(it)
            _deleteTaskEvent.value = Event(Unit)
        }
    }

    fun editTask() {
        _editTaskEvent.value = Event(Unit)
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

    fun start(taskId: String?) {

        // Trigger the load
        _taskId.offer(taskId!!)
    }

    private fun computeResult(taskResult: Result<Task>): Task? {
        return if (taskResult is Result.Success) {
            taskResult.data
        } else {
            showSnackbarMessage(R.string.loading_tasks_error)
            null
        }
    }


    fun refresh() {
        _task.value?.id?.let { _taskId.offer(it) }
    }

    private fun showSnackbarMessage(@StringRes message: Int) {
        _snackbarText.value = Event(message)
    }
}
