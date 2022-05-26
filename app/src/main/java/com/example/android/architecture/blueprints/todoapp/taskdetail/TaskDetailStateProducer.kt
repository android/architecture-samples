package com.example.android.architecture.blueprints.todoapp.taskdetail

import androidx.lifecycle.SavedStateHandle
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.TodoDestinationsArgs
import com.example.android.architecture.blueprints.todoapp.data.Result
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository
import com.example.android.architecture.blueprints.todoapp.util.Async
import com.example.android.architecture.blueprints.todoapp.util.frp.StateChange
import com.example.android.architecture.blueprints.todoapp.util.frp.stateProducer
import com.example.android.architecture.blueprints.todoapp.util.frp.toStateChangeStream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onStart

sealed class Action {
    object Refresh : Action()
    object Delete : Action()
    object SnackBarMessageShown : Action()
    data class SetCompleted(val completed: Boolean) : Action()
}

fun taskDetailStateProducer(
    scope: CoroutineScope,
    tasksRepository: TasksRepository,
    savedStateHandle: SavedStateHandle
) = stateProducer<Action, TaskDetailUiState>(
    scope = scope,
    initialState = TaskDetailUiState(isLoading = true),
    actionTransform = { actionStream ->
        val taskId: String = savedStateHandle[TodoDestinationsArgs.TASK_ID_ARG]!!
        merge(
            loadStateChanges(
                taskId = taskId,
                tasksRepository = tasksRepository
            ),
            actionStream.toStateChangeStream {
                when (val type = type()) {
                    is Action.Refresh -> type.flow.refreshStateChanges(
                        taskId = taskId,
                        tasksRepository = tasksRepository
                    )
                    is Action.Delete -> type.flow.deleteStateChanges(
                        taskId = taskId,
                        tasksRepository = tasksRepository
                    )
                    is Action.SetCompleted -> type.flow.completionStateChanges(
                        taskId = taskId,
                        tasksRepository = tasksRepository
                    )
                    is Action.SnackBarMessageShown -> type.flow.snackbarMessageStateChanges()
                }
            }
        )
    }
)

private fun loadStateChanges(
    taskId: String,
    tasksRepository: TasksRepository
): Flow<StateChange<TaskDetailUiState>> =
    tasksRepository.getTaskStream(taskId)
        .map { tasksResult ->
            if (tasksResult is Result.Success) Async.Success(tasksResult.data)
            else Async.Success(null)
        }
        .onStart<Async<Task?>> { emit(Async.Loading) }
        .map { taskAsync ->
            StateChange {
                when (taskAsync) {
                    Async.Loading -> copy(isLoading = true)
                    is Async.Success -> copy(
                        isLoading = false,
                        task = taskAsync.data
                    )
                }
            }
        }

private fun Flow<Action.Refresh>.refreshStateChanges(
    taskId: String,
    tasksRepository: TasksRepository
): Flow<StateChange<TaskDetailUiState>> =
    flatMapLatest {
        flow {
            emit(
                StateChange {
                    copy(isLoading = true)
                }
            )
            tasksRepository.refreshTask(taskId)
            emit(
                StateChange {
                    copy(isLoading = false)
                }
            )
        }
    }

private fun Flow<Action.Delete>.deleteStateChanges(
    taskId: String,
    tasksRepository: TasksRepository
): Flow<StateChange<TaskDetailUiState>> =
    mapLatest {
        tasksRepository.deleteTask(taskId)
        StateChange {
            copy(isTaskDeleted = true)
        }
    }

private fun Flow<Action.SnackBarMessageShown>.snackbarMessageStateChanges(): Flow<StateChange<TaskDetailUiState>> =
    mapLatest {
        StateChange {
            copy(userMessage = null)
        }
    }

private fun Flow<Action.SetCompleted>.completionStateChanges(
    taskId: String,
    tasksRepository: TasksRepository
): Flow<StateChange<TaskDetailUiState>> =
    mapLatest { (completed) ->
        if (completed) {
            tasksRepository.completeTask(taskId)
            StateChange {
                copy(userMessage = R.string.task_marked_complete)
            }
        } else {
            tasksRepository.activateTask(taskId)
            StateChange {
                copy(userMessage = R.string.task_marked_active)
            }
        }
    }
