package com.example.android.architecture.blueprints.todoapp.addedittask

import androidx.lifecycle.SavedStateHandle
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.TodoDestinationsArgs
import com.example.android.architecture.blueprints.todoapp.data.Result
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository
import com.example.android.architecture.blueprints.todoapp.util.Mutation
import com.example.android.architecture.blueprints.todoapp.util.actionStateProducer
import com.example.android.architecture.blueprints.todoapp.util.mutation
import com.example.android.architecture.blueprints.todoapp.util.toMutationStream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapLatest

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
) = actionStateProducer<Action, AddEditTaskUiState>(
    scope = scope,
    initialState = AddEditTaskUiState(
        taskId = savedStateHandle[TodoDestinationsArgs.TASK_ID_ARG],
        isLoading = savedStateHandle.contains(TodoDestinationsArgs.TASK_ID_ARG)
    ),
    mutationFlows = listOf(
        loadMutations(
            taskId = savedStateHandle[TodoDestinationsArgs.TASK_ID_ARG],
            tasksRepository = tasksRepository
        )
    ),
    actionTransform = { actionStream ->
        actionStream.toMutationStream {
            when (val type = type()) {
                is Action.Save -> type.flow.saveMutations(
                    tasksRepository = tasksRepository
                )
                is Action.UpdateDescription -> type.flow.descriptionMutations()
                is Action.UpdateTitle -> type.flow.titleMutations()
                is Action.SnackBarMessageShown -> type.flow.snackbarMessageMutations()
            }
        }
    }
)

private fun loadMutations(
    taskId: String?,
    tasksRepository: TasksRepository
): Flow<Mutation<AddEditTaskUiState>> =
    // Task does not exist yet, nothing to load
    if (taskId == null) emptyFlow()
    else flow {
        val result = tasksRepository.getTask(taskId)
        emit(
            when (result) {
                is Result.Success -> mutation {
                    copy(
                        title = result.data.title,
                        description = result.data.description,
                        isTaskCompleted = result.data.isCompleted,
                        isLoading = false
                    )
                }
                else -> mutation {
                    copy(isLoading = false)
                }
            }
        )
    }

private fun Flow<Action.UpdateTitle>.titleMutations(): Flow<Mutation<AddEditTaskUiState>> =
    mapLatest {
        mutation {
            copy(title = it.title)
        }
    }

private fun Flow<Action.UpdateDescription>.descriptionMutations(): Flow<Mutation<AddEditTaskUiState>> =
    mapLatest {
        mutation {
            copy(description = it.description)
        }
    }

private fun Flow<Action.SnackBarMessageShown>.snackbarMessageMutations(): Flow<Mutation<AddEditTaskUiState>> =
    mapLatest {
        mutation {
            copy(userMessage = null)
        }
    }

private fun Flow<Action.Save>.saveMutations(
    tasksRepository: TasksRepository
): Flow<Mutation<AddEditTaskUiState>> =
    // Don't submit the same data twice
    distinctUntilChanged()
        .mapLatest { (taskId, title, description, taskCompleted) ->
            if (title.isEmpty() || description.isEmpty()) return@mapLatest mutation<AddEditTaskUiState> {
                copy(userMessage = R.string.empty_task_message)
            }
            when (taskId) {
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
            mutation {
                copy(isTaskSaved = true)
            }
        }
