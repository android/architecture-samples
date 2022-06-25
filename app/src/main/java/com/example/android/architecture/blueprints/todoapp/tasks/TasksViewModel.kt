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
package com.example.android.architecture.blueprints.todoapp.tasks

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.architecture.blueprints.todoapp.ADD_EDIT_RESULT_OK
import com.example.android.architecture.blueprints.todoapp.DELETE_RESULT_OK
import com.example.android.architecture.blueprints.todoapp.EDIT_RESULT_OK
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.data.Result
import com.example.android.architecture.blueprints.todoapp.data.Result.Success
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository
import com.example.android.architecture.blueprints.todoapp.tasks.TasksFilterType.ACTIVE_TASKS
import com.example.android.architecture.blueprints.todoapp.tasks.TasksFilterType.ALL_TASKS
import com.example.android.architecture.blueprints.todoapp.tasks.TasksFilterType.COMPLETED_TASKS
import com.example.android.architecture.blueprints.todoapp.util.Async
import com.example.android.architecture.blueprints.todoapp.util.Mutation
import com.example.android.architecture.blueprints.todoapp.util.Mutations
import com.example.android.architecture.blueprints.todoapp.util.StateProducer
import com.example.android.architecture.blueprints.todoapp.util.mutation
import com.example.android.architecture.blueprints.todoapp.util.plus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

/**
 * UiState for the task list screen.
 */
data class TasksUiState(
    val items: List<Task> = emptyList(),
    val isLoading: Boolean = false,
    val filteringUiInfo: FilteringUiInfo = FilteringUiInfo(),
    val userMessage: Int? = null
)

/**
 * ViewModel for the task list screen.
 */
@HiltViewModel
class TasksViewModel @Inject constructor(
    private val tasksRepository: TasksRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val savedFilterType = savedStateHandle.getStateFlow(
        key = TASKS_FILTER_SAVED_STATE_KEY,
        initialValue = ALL_TASKS
    )

    private val filterStateChanges = savedFilterType
        .map(::getFilterUiInfo)
        .distinctUntilChanged()
        .map(FilteringUiInfo::asStateChange)

    private val loadStateChanges = combine(
        flow = tasksRepository.getTasksStream(),
        flow2 = savedFilterType,
        transform = ::filterTasks
    )
        .map { Async.Success(it) }
        .onStart<Async<List<Task>?>> { emit(Async.Loading) }
        .loadStateChanges()

    private val stateProducer = StateProducer(
        scope = viewModelScope,
        initial = TasksUiState(isLoading = true),
        mutationFlows = listOf(
            loadStateChanges,
            filterStateChanges,
        )
    )

    val uiState = stateProducer.state

    fun setFiltering(requestType: TasksFilterType) {
        savedStateHandle[TASKS_FILTER_SAVED_STATE_KEY] = requestType
    }

    fun clearCompletedTasks() = stateProducer.launch {
        tasksRepository.clearCompletedTasks()
        setState(showSnackbarStateChange(R.string.completed_tasks_cleared))
        refresh()
    }

    fun completeTask(task: Task, completed: Boolean) = stateProducer.launch {
        if (completed) {
            tasksRepository.completeTask(task)
            setState(showSnackbarStateChange(R.string.task_marked_complete))
        } else {
            tasksRepository.activateTask(task)
            setState(showSnackbarStateChange(R.string.task_marked_active))
        }
    }

    fun showEditResultMessage(result: Int) = stateProducer.launch {
        setState(
            when (result) {
                EDIT_RESULT_OK -> showSnackbarStateChange(R.string.successfully_saved_task_message)
                ADD_EDIT_RESULT_OK -> showSnackbarStateChange(R.string.successfully_added_task_message)
                DELETE_RESULT_OK -> showSnackbarStateChange(R.string.successfully_deleted_task_message)
                else -> Mutations.identity()
            }
        )
    }

    fun snackbarMessageShown() = stateProducer.launch {
        setState { copy(userMessage = null) }
    }

    private fun showSnackbarStateChange(message: Int) = mutation<TasksUiState> {
        copy(userMessage = message)
    }

    fun refresh() = stateProducer.launch {
        setState { copy(isLoading = true) }
        tasksRepository.refreshTasks()
        setState { copy(isLoading = false) }
    }

    private fun filterTasks(
        tasksResult: Result<List<Task>>,
        filteringType: TasksFilterType
    ): List<Task>? = if (tasksResult is Success) {
        filterItems(tasksResult.data, filteringType)
    } else {
        null
    }

    private fun filterItems(tasks: List<Task>, filteringType: TasksFilterType): List<Task> =
        tasks.filter { task ->
            when (filteringType) {
                ALL_TASKS -> true
                ACTIVE_TASKS -> task.isActive
                COMPLETED_TASKS -> task.isCompleted
            }
        }

    private fun getFilterUiInfo(requestType: TasksFilterType): FilteringUiInfo =
        when (requestType) {
            ALL_TASKS -> {
                FilteringUiInfo(
                    R.string.label_all, R.string.no_tasks_all,
                    R.drawable.logo_no_fill
                )
            }
            ACTIVE_TASKS -> {
                FilteringUiInfo(
                    R.string.label_active, R.string.no_tasks_active,
                    R.drawable.ic_check_circle_96dp
                )
            }
            COMPLETED_TASKS -> {
                FilteringUiInfo(
                    R.string.label_completed, R.string.no_tasks_completed,
                    R.drawable.ic_verified_user_96dp
                )
            }
        }

    private fun Flow<Async<List<Task>?>>.loadStateChanges(): Flow<Mutation<TasksUiState>> =
        mapLatest { tasksResult: Async<List<Task>?> ->
            when (tasksResult) {
                Async.Loading -> mutation {
                    copy(isLoading = true)
                }
                is Async.Success -> when (val tasks = tasksResult.data) {
                    null -> showSnackbarStateChange(R.string.loading_tasks_error) + mutation {
                        copy(items = emptyList(), isLoading = false)
                    }
                    else -> mutation {
                        copy(items = tasks, isLoading = false)
                    }
                }
            }
        }
}

// Used to save the current filtering in SavedStateHandle.
const val TASKS_FILTER_SAVED_STATE_KEY = "TASKS_FILTER_SAVED_STATE_KEY"

data class FilteringUiInfo(
    val currentFilteringLabel: Int = R.string.label_all,
    val noTasksLabel: Int = R.string.no_tasks_all,
    val noTaskIconRes: Int = R.drawable.logo_no_fill,
)

private fun FilteringUiInfo.asStateChange() = mutation<TasksUiState> {
    copy(filteringUiInfo = this@asStateChange)
}
