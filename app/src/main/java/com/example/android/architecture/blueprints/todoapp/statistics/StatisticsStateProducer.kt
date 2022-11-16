package com.example.android.architecture.blueprints.todoapp.statistics

import com.example.android.architecture.blueprints.todoapp.data.Result
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository
import com.example.android.architecture.blueprints.todoapp.util.Async
import com.example.android.architecture.blueprints.todoapp.util.Mutation
import com.example.android.architecture.blueprints.todoapp.util.Mutations
import com.example.android.architecture.blueprints.todoapp.util.actionStateProducer
import com.example.android.architecture.blueprints.todoapp.util.mutation
import com.example.android.architecture.blueprints.todoapp.util.toMutationStream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onStart

sealed class Action {
    object Refresh : Action()
}

fun statisticsStateProducer(
    scope: CoroutineScope,
    tasksRepository: TasksRepository,
) = actionStateProducer<Action, StatisticsUiState>(
    scope = scope,
    initialState = StatisticsUiState(isLoading = true),
    mutationFlows = listOf(
        loadMutations(tasksRepository)
    ),
    actionTransform = {
        onAction<Action.Refresh> {
            flow.refreshMutations(tasksRepository)
        }
    }
)

private fun loadMutations(
    tasksRepository: TasksRepository
): Flow<Mutation<StatisticsUiState>> =
    tasksRepository.getTasksStream()
        .map { Async.Success(it) }
        .onStart<Async<Result<List<Task>>>> { emit(Async.Loading) }
        .map { taskAsync ->
            mutation {
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

private fun Flow<Action.Refresh>.refreshMutations(
    tasksRepository: TasksRepository
): Flow<Mutation<StatisticsUiState>> =
    mapLatest {
        tasksRepository.refreshTasks()
        Mutations.identity()
    }
