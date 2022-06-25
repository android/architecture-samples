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

package com.example.android.architecture.blueprints.todoapp.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.architecture.blueprints.todoapp.data.Result
import com.example.android.architecture.blueprints.todoapp.data.Result.Success
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository
import com.example.android.architecture.blueprints.todoapp.util.Async
import com.example.android.architecture.blueprints.todoapp.util.Mutation
import com.example.android.architecture.blueprints.todoapp.util.StateProducer
import com.example.android.architecture.blueprints.todoapp.util.mutation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

/**
 * UiState for the statistics screen.
 */
data class StatisticsUiState(
    val isEmpty: Boolean = false,
    val isLoading: Boolean = false,
    val activeTasksPercent: Float = 0f,
    val completedTasksPercent: Float = 0f
)

/**
 * ViewModel for the statistics screen.
 */
@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val tasksRepository: TasksRepository
) : ViewModel() {

    private val loadStateChanges = tasksRepository.getTasksStream()
        .map { Async.Success(it) }
        .onStart<Async<Result<List<Task>>>> { emit(Async.Loading) }
        .loadStateChanges()

    private val stateProducer = StateProducer(
        scope = viewModelScope,
        initial = StatisticsUiState(isLoading = true),
        mutationFlows = listOf(
            loadStateChanges
        )
    )

    val uiState = stateProducer.state

    fun refresh() = stateProducer.launch {
        tasksRepository.refreshTasks()
    }

    private fun Flow<Async<Result<List<Task>>>>.loadStateChanges(): Flow<Mutation<StatisticsUiState>> =
        mapLatest { taskLoad ->
            mutation {
                when (taskLoad) {
                    Async.Loading -> {
                        copy(isLoading = true, isEmpty = true)
                    }
                    is Async.Success -> {
                        when (val result = taskLoad.data) {
                            is Success -> {
                                val stats = getActiveAndCompletedStats(result.data)
                                copy(
                                    isEmpty = result.data.isEmpty(),
                                    activeTasksPercent = stats.activeTasksPercent,
                                    completedTasksPercent = stats.completedTasksPercent,
                                    isLoading = false
                                )
                            }
                            else -> copy(isLoading = false)
                        }
                    }
                }
            }
        }
}
