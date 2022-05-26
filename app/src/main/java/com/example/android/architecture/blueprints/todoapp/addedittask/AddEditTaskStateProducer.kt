package com.example.android.architecture.blueprints.todoapp.addedittask

import androidx.lifecycle.SavedStateHandle
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.TodoDestinationsArgs
import com.example.android.architecture.blueprints.todoapp.data.Result
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository
import com.example.android.architecture.blueprints.todoapp.util.frp.StateChange
import com.example.android.architecture.blueprints.todoapp.util.frp.stateProducer
import com.example.android.architecture.blueprints.todoapp.util.frp.toStateChangeStream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.merge

sealed class Action {
    data class Save(
        val taskId: String?,
        val title: String,
        val description: String,
        val taskCompleted: Boolean,
    ) : Action()

    data class UpdateTitle(val title: String) : Action()
    data class UpdateDescription(val description: String) : Action()

    object SnackBarMessageShown : Action()
}

fun addEditStateProducer(
    scope: CoroutineScope,
    tasksRepository: TasksRepository,
    savedStateHandle: SavedStateHandle
) = stateProducer<Action, AddEditTaskUiState>(
    scope = scope,
    initialState = AddEditTaskUiState(),
    actionTransform = { actionStream ->
        val taskId: String? = savedStateHandle[TodoDestinationsArgs.TASK_ID_ARG]
        merge(
            loadStateChanges(
                taskId = taskId,
                tasksRepository = tasksRepository
            ),
            actionStream.toStateChangeStream {
                when (val type = type()) {
                    is Action.Save -> type.flow.saveStateChanges(
                        tasksRepository = tasksRepository
                    )
                    is Action.UpdateDescription -> type.flow.descriptionStateChanges()
                    is Action.UpdateTitle -> type.flow.titleStateChanges()
                    is Action.SnackBarMessageShown -> type.flow.snackbarMessageStateChanges()
                }
            }
        )
    }
)

private fun loadStateChanges(
    taskId: String?,
    tasksRepository: TasksRepository
): Flow<StateChange<AddEditTaskUiState>> =
    // Task does not exist yet, nothing to load
    if (taskId == null) emptyFlow()
    else flow {
        emit(
            StateChange {
                copy(isLoading = true)
            }
        )

        val result = tasksRepository.getTask(taskId)
        emit(
            when (result) {
                is Result.Success -> StateChange {
                    copy(
                        title = result.data.title,
                        description = result.data.description,
                        isTaskCompleted = result.data.isCompleted,
                        isLoading = false
                    )
                }
                else -> StateChange {
                    copy(isLoading = false)
                }
            }
        )
    }

private fun Flow<Action.UpdateTitle>.titleStateChanges(): Flow<StateChange<AddEditTaskUiState>> =
    mapLatest {
        StateChange {
            copy(title = it.title)
        }
    }

private fun Flow<Action.UpdateDescription>.descriptionStateChanges(): Flow<StateChange<AddEditTaskUiState>> =
    mapLatest {
        StateChange {
            copy(description = it.description)
        }
    }

private fun Flow<Action.SnackBarMessageShown>.snackbarMessageStateChanges(): Flow<StateChange<AddEditTaskUiState>> =
    mapLatest {
        StateChange {
            copy(userMessage = null)
        }
    }

private fun Flow<Action.Save>.saveStateChanges(
    tasksRepository: TasksRepository
): Flow<StateChange<AddEditTaskUiState>> =
    // Don't submit the same data twice
    distinctUntilChanged()
        .mapLatest { (taskId, title, description, taskCompleted) ->
            if (title.isEmpty() || description.isEmpty()) StateChange<AddEditTaskUiState> {
                copy(userMessage = R.string.empty_task_message)
            }
            else when (taskId) {
                null -> tasksRepository.saveTask(
                    Task(
                        title = title,
                        description = description
                    )
                )
                else -> tasksRepository.saveTask(
                    Task(
                        title = title,
                        description = description,
                        isCompleted = taskCompleted,
                        id = taskId
                    )
                )
            }
            StateChange {
                copy(isTaskSaved = true)
            }
        }
