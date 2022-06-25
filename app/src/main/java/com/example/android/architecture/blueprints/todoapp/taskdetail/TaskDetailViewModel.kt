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

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.TodoDestinationsArgs
import com.example.android.architecture.blueprints.todoapp.data.Result
import com.example.android.architecture.blueprints.todoapp.data.Result.Success
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository
import com.example.android.architecture.blueprints.todoapp.util.Async
import com.example.android.architecture.blueprints.todoapp.util.Mutation
import com.example.android.architecture.blueprints.todoapp.util.StateProducer
import com.example.android.architecture.blueprints.todoapp.util.mutation
import com.example.android.architecture.blueprints.todoapp.util.plus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

/**
 * UiState for the Details screen.
 */
data class TaskDetailUiState(
    val task: Task? = null,
    val isLoading: Boolean = false,
    val userMessage: Int? = null,
    val isTaskDeleted: Boolean = false
)

/**
 * ViewModel for the Details screen.
 */
@HiltViewModel
class TaskDetailViewModel @Inject constructor(
    private val tasksRepository: TasksRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val taskId: String = savedStateHandle[TodoDestinationsArgs.TASK_ID_ARG]!!

    private val loadStateChanges = tasksRepository.getTaskStream(taskId)
        .map { handleResult(it) }
        .onStart { emit(Async.Loading) }
        .loadStateChanges()

    private val stateProducer = StateProducer(
        scope = viewModelScope,
        initial = TaskDetailUiState(isLoading = true),
        mutationFlows = listOf(
            loadStateChanges,
        )
    )

    val uiState = stateProducer.state

    fun deleteTask() = stateProducer.launch {
        tasksRepository.deleteTask(taskId)
        setState { copy(isTaskDeleted = true) }
    }

    fun setCompleted(completed: Boolean) = stateProducer.launch {
        val task = uiState.value.task ?: return@launch
        if (completed) {
            tasksRepository.completeTask(task)
            setState(snackBarMutation(R.string.task_marked_complete))
        } else {
            tasksRepository.activateTask(task)
            setState(snackBarMutation(R.string.task_marked_active))
        }
    }

    fun refresh() = stateProducer.launch {
        setState { copy(isLoading = true) }
        tasksRepository.refreshTask(taskId)
        setState { copy(isLoading = false) }
    }

    fun snackbarMessageShown() = stateProducer.launch {
        setState { copy(isLoading = false) }
    }

    private fun snackBarMutation(message: Int) = mutation<TaskDetailUiState> {
        copy(userMessage = message)
    }

    private fun handleResult(tasksResult: Result<Task>): Async<Task?> =
        if (tasksResult is Success) {
            Async.Success(tasksResult.data)
        } else {
            Async.Success(null)
        }

    private fun Flow<Async<Task?>>.loadStateChanges(): Flow<Mutation<TaskDetailUiState>> =
        mapLatest { tasksResult: Async<Task?> ->
            when (tasksResult) {
                Async.Loading -> mutation {
                    copy(isLoading = true)
                }
                is Async.Success -> when (val task = tasksResult.data) {
                    null -> snackBarMutation(R.string.loading_tasks_error) + mutation {
                        copy(task = null, isLoading = false)
                    }
                    else -> mutation {
                        copy(task = task, isLoading = false)
                    }
                }
            }
        }
}
