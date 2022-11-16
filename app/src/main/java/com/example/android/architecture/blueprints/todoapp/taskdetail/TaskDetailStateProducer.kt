package com.example.android.architecture.blueprints.todoapp.taskdetail

import androidx.lifecycle.SavedStateHandle
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.TodoDestinationsArgs
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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onStart

sealed class Action {
    object Refresh : Action()
    object Delete : Action()
    object SnackBarMessageShown : Action()
    data class SetCompleted(
        val task: Task?,
        val completed: Boolean
    ) : Action()
}

fun taskDetailStateProducer(
    scope: CoroutineScope,
    tasksRepository: TasksRepository,
    savedStateHandle: SavedStateHandle
) = actionStateProducer<Action, TaskDetailUiState>(
    scope = scope,
    initialState = TaskDetailUiState(
        isLoading = true,
        taskId = savedStateHandle[TodoDestinationsArgs.TASK_ID_ARG]!!
    ),
    mutationFlows = listOf(
        loadMutations(
            taskId = savedStateHandle[TodoDestinationsArgs.TASK_ID_ARG]!!,
            tasksRepository = tasksRepository
        )
    ),
    actionTransform = {
        val taskId: String = savedStateHandle[TodoDestinationsArgs.TASK_ID_ARG]!!
        onAction<Action.Refresh> {
            flow.refreshMutations(
                taskId, tasksRepository
            )
        }
        onAction<Action.Delete> {
            flow.deleteMutations(
                taskId, tasksRepository
            )
        }
        onAction<Action.SetCompleted> {
            flow.completionMutations(
                taskId, tasksRepository
            )
        }
        onAction<Action.SnackBarMessageShown> {
            flow.snackbarMessageMutations()
        }
    }
)

private fun loadMutations(
    taskId: String,
    tasksRepository: TasksRepository
): Flow<Mutation<TaskDetailUiState>> =
    tasksRepository.getTaskStream(taskId)
        .map { tasksResult ->
            if (tasksResult is Result.Success) Async.Success(tasksResult.data)
            else Async.Success(null)
        }
        .onStart<Async<Task?>> { emit(Async.Loading) }
        .map { taskAsync ->
            mutation {
                when (taskAsync) {
                    Async.Loading -> copy(isLoading = true)
                    is Async.Success -> copy(
                        isLoading = false,
                        task = taskAsync.data
                    )
                }
            }
        }

private fun Flow<Action.Refresh>.refreshMutations(
    taskId: String,
    tasksRepository: TasksRepository
): Flow<Mutation<TaskDetailUiState>> =
    flatMapLatest {
        flow {
            emit(
                mutation {
                    copy(isLoading = true)
                }
            )
            tasksRepository.refreshTask(taskId)
            emit(
                mutation {
                    copy(isLoading = false)
                }
            )
        }
    }

private fun Flow<Action.Delete>.deleteMutations(
    taskId: String,
    tasksRepository: TasksRepository
): Flow<Mutation<TaskDetailUiState>> =
    mapLatest {
        tasksRepository.deleteTask(taskId)
        mutation {
            copy(isTaskDeleted = true)
        }
    }

private fun Flow<Action.SnackBarMessageShown>.snackbarMessageMutations(): Flow<Mutation<TaskDetailUiState>> =
    mapLatest {
        mutation {
            copy(userMessage = null)
        }
    }

private fun Flow<Action.SetCompleted>.completionMutations(
    taskId: String,
    tasksRepository: TasksRepository
): Flow<Mutation<TaskDetailUiState>> =
    mapLatest { (task, completed) ->
        if (task == null) return@mapLatest Mutations.identity()
        if (completed) {
            tasksRepository.completeTask(task)
            mutation {
                copy(userMessage = R.string.task_marked_complete)
            }
        } else {
            tasksRepository.activateTask(task)
            mutation {
                copy(userMessage = R.string.task_marked_active)
            }
        }
    }
