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

package com.example.android.architecture.blueprints.todoapp.addedittask

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.TodoDestinationsArgs
import com.example.android.architecture.blueprints.todoapp.data.Result.Success
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository
import com.example.android.architecture.blueprints.todoapp.util.Mutation
import com.example.android.architecture.blueprints.todoapp.util.StateProducer
import com.example.android.architecture.blueprints.todoapp.util.mutation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * UiState for the Add/Edit screen
 */
data class AddEditTaskUiState(
    val title: String = "",
    val description: String = "",
    val isTaskCompleted: Boolean = false,
    val isLoading: Boolean = false,
    val userMessage: Int? = null,
    val isTaskSaved: Boolean = false
)

/**
 * ViewModel for the Add/Edit screen.
 */
@HiltViewModel
class AddEditTaskViewModel @Inject constructor(
    private val tasksRepository: TasksRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val taskId: String? = savedStateHandle[TodoDestinationsArgs.TASK_ID_ARG]

    private val stateProducer = StateProducer(
        scope = viewModelScope,
        initial = AddEditTaskUiState(),
        mutationFlows = listOf(
            loadStateChanges(taskId),
        )
    )

    val uiState: StateFlow<AddEditTaskUiState> = stateProducer.state

    // Called when clicking on fab.
    fun saveTask() = stateProducer.launch {
        if (uiState.value.title.isEmpty() || uiState.value.description.isEmpty()) {
            setState {
                copy(userMessage = R.string.empty_task_message)
            }
            return@launch
        }

        if (taskId == null) {
            createNewTask()
        } else {
            updateTask()
        }
    }

    fun snackbarMessageShown() = stateProducer.launch {
        setState {
            copy(userMessage = null)
        }
    }

    fun updateTitle(newTitle: String) = stateProducer.launch {
        setState {
            copy(title = newTitle)
        }
    }

    fun updateDescription(newDescription: String) = stateProducer.launch {
        setState {
            copy(description = newDescription)
        }
    }

    private fun createNewTask() = stateProducer.launch {
        val newTask = Task(uiState.value.title, uiState.value.description)
        tasksRepository.saveTask(newTask)
        setState {
            copy(isTaskSaved = true)
        }
    }

    private fun updateTask() = stateProducer.launch {
        if (taskId == null) {
            throw RuntimeException("updateTask() was called but task is new.")
        }
        val updatedTask = Task(
            title = uiState.value.title,
            description = uiState.value.description,
            isCompleted = uiState.value.isTaskCompleted,
            id = taskId
        )
        tasksRepository.saveTask(updatedTask)
        setState {
            copy(isTaskSaved = true)
        }
    }

    private fun loadStateChanges(taskId: String?): Flow<Mutation<AddEditTaskUiState>> =
        if (taskId == null) emptyFlow()
        else flow {
            emit(
                mutation {
                    copy(isLoading = true)
                }
            )
            tasksRepository.getTask(taskId).let { result ->
                if (result is Success) emit(
                    mutation {
                        val task = result.data
                        copy(
                            title = task.title,
                            description = task.description,
                            isTaskCompleted = task.isCompleted,
                            isLoading = false
                        )
                    }
                ) else emit(
                    mutation {
                        copy(isLoading = false)
                    }
                )
            }
        }
}
