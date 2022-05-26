package com.example.android.architecture.blueprints.todoapp.statistics

import com.example.android.architecture.blueprints.todoapp.data.Result
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository
import com.example.android.architecture.blueprints.todoapp.util.Async
import com.example.android.architecture.blueprints.todoapp.util.frp.StateChange
import com.example.android.architecture.blueprints.todoapp.util.frp.stateProducer
import com.example.android.architecture.blueprints.todoapp.util.frp.toStateChangeStream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onStart

sealed class Action {
    object Refresh : Action()
}

fun statisticsStateProducer(
    scope: CoroutineScope,
    tasksRepository: TasksRepository,
) = stateProducer<Action, StatisticsUiState>(
    scope = scope,
    initialState = StatisticsUiState(isLoading = true),
    actionTransform = { actionStream ->
        merge(
            loadStateChanges(tasksRepository),
            actionStream.toStateChangeStream {
                when (val type = type()) {
                    is Action.Refresh -> type.flow.refreshStateChanges(
                        tasksRepository = tasksRepository
                    )
                }
            }
        )
    }
)

private fun loadStateChanges(
    tasksRepository: TasksRepository
): Flow<StateChange<StatisticsUiState>> =
    tasksRepository.getTasksStream()
        .map { Async.Success(it) }
        .onStart<Async<Result<List<Task>>>> { emit(Async.Loading) }
        .map { taskAsync ->
            StateChange {
                when (taskAsync) {
                    Async.Loading -> {
                        StatisticsUiState(isLoading = true, isEmpty = true)
                    }
                    is Async.Success -> {
                        when (val result = taskAsync.data) {
                            is Result.Success -> {
                                val stats = getActiveAndCompletedStats(result.data)
                                StatisticsUiState(
                                    isEmpty = result.data.isEmpty(),
                                    activeTasksPercent = stats.activeTasksPercent,
                                    completedTasksPercent = stats.completedTasksPercent,
                                    isLoading = false
                                )
                            }
                            else -> StatisticsUiState(isLoading = false)
                        }
                    }
                }
            }
        }

private fun Flow<Action.Refresh>.refreshStateChanges(
    tasksRepository: TasksRepository
): Flow<StateChange<StatisticsUiState>> =
    mapLatest {
        tasksRepository.refreshTasks()
        StateChange.identity()
    }
