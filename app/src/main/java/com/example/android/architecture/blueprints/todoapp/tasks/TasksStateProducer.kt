package com.example.android.architecture.blueprints.todoapp.tasks

import androidx.lifecycle.SavedStateHandle
import com.example.android.architecture.blueprints.todoapp.ADD_EDIT_RESULT_OK
import com.example.android.architecture.blueprints.todoapp.DELETE_RESULT_OK
import com.example.android.architecture.blueprints.todoapp.EDIT_RESULT_OK
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.data.Result
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository
import com.example.android.architecture.blueprints.todoapp.util.Mutation
import com.example.android.architecture.blueprints.todoapp.util.Mutations
import com.example.android.architecture.blueprints.todoapp.util.actionStateProducer
import com.example.android.architecture.blueprints.todoapp.util.mutation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*

sealed class Action {
    object Refresh : Action()

    object ClearCompletedTasks : Action()

    data class ShowEditResultMessage(
        val result: Int
    ) : Action()

    data class SetFilter(
        val requestType: TasksFilterType
    ) : Action()

    data class SetTaskCompletion(
        val task: Task,
        val completed: Boolean
    ) : Action()

    object SnackBarMessageShown : Action()
}

fun tasksStateProducer(
    scope: CoroutineScope,
    tasksRepository: TasksRepository,
    savedStateHandle: SavedStateHandle
) = actionStateProducer(
    scope = scope,
    initialState = TasksUiState(isLoading = true),
    mutationFlows = listOf(
        loadMutations(
            tasksRepository = tasksRepository,
            savedStateHandle = savedStateHandle
        )
    ),
    actionTransform = {
		onAction<Action.ClearCompletedTasks> {
			flow.clearCompletedTasksMutations(tasksRepository)
		}
        onAction<Action.Refresh> {
			flow.flatMapLatest { refreshMutations(tasksRepository) }
		}
		onAction<Action.SetFilter> {
			flow.filterChangeMutations(savedStateHandle)
		}
		onAction<Action.SetTaskCompletion> {
			flow.taskCompletionMutations(tasksRepository)
		}
		onAction<Action.ShowEditResultMessage> {
			flow.editResultMessageMutations()
		}
		onAction<Action.SnackBarMessageShown> {
			flow.snackbarMessageMutations()
		}
    }
)

private fun loadMutations(
    tasksRepository: TasksRepository,
    savedStateHandle: SavedStateHandle
): Flow<Mutation<TasksUiState>> =
    savedStateHandle.getStateFlow(
        key = TASKS_FILTER_SAVED_STATE_KEY,
        initialValue = TasksFilterType.ALL_TASKS
    )
        .flatMapLatest { filteringType ->
            tasksRepository.getTasksStream().map { tasksResult ->
                if (tasksResult is Result.Success) mutation {
                    copy(
                        isLoading = false,
                        items = tasksResult.data.filter {
                            when (filteringType) {
                                TasksFilterType.ALL_TASKS -> true
                                TasksFilterType.ACTIVE_TASKS -> it.isActive
                                TasksFilterType.COMPLETED_TASKS -> it.isCompleted
                            }
                        },
                        filteringUiInfo = filteringType.filteringUiInfo
                    )
                } else mutation {
                    copy(
                        isLoading = false,
                        items = emptyList(),
                        userMessage = R.string.loading_tasks_error,
                        filteringUiInfo = filteringType.filteringUiInfo
                    )
                }
            }
        }

private fun Flow<Action.SetFilter>.filterChangeMutations(
    savedStateHandle: SavedStateHandle
): Flow<Mutation<TasksUiState>> =
    distinctUntilChanged()
        .mapLatest { (requestType) ->
            // Write the request type to the savedState handle
            savedStateHandle[TASKS_FILTER_SAVED_STATE_KEY] = requestType
            // Handle the filter change upstream
            Mutations.identity()
        }

private fun Flow<Action.ShowEditResultMessage>.editResultMessageMutations(): Flow<Mutation<TasksUiState>> =
    mapLatest { (result) ->
        mutation {
            copy(
                userMessage = when (result) {
                    EDIT_RESULT_OK -> R.string.successfully_saved_task_message
                    ADD_EDIT_RESULT_OK -> R.string.successfully_added_task_message
                    DELETE_RESULT_OK -> R.string.successfully_deleted_task_message
                    else -> null
                }
            )
        }
    }

private fun Flow<Action.SnackBarMessageShown>.snackbarMessageMutations(): Flow<Mutation<TasksUiState>> =
    mapLatest {
        mutation {
            copy(userMessage = null)
        }
    }

private fun Flow<Action.SetTaskCompletion>.taskCompletionMutations(
    tasksRepository: TasksRepository
): Flow<Mutation<TasksUiState>> =
    distinctUntilChanged()
        .mapLatest { (task, completed) ->
            if (completed) tasksRepository.completeTask(task)
            else tasksRepository.activateTask(task)

            mutation {
                copy(
                    userMessage = when (completed) {
                        true -> R.string.task_marked_complete
                        false -> R.string.task_marked_active
                    }
                )
            }
        }

private fun Flow<Action.ClearCompletedTasks>.clearCompletedTasksMutations(
    tasksRepository: TasksRepository
): Flow<Mutation<TasksUiState>> = flatMapLatest {
    tasksRepository.clearCompletedTasks()

    flow {
        emit(
            mutation {
                copy(userMessage = R.string.completed_tasks_cleared)
            }
        )
        emitAll(
            refreshMutations(
                tasksRepository = tasksRepository
            )
        )
    }
}

private fun refreshMutations(
    tasksRepository: TasksRepository
): Flow<Mutation<TasksUiState>> = flow {
    emit(mutation { copy(isLoading = true) })
    tasksRepository.refreshTasks()
    emit(mutation { copy(isLoading = false) })
}

private val TasksFilterType.filteringUiInfo
    get() = when (this) {
        TasksFilterType.ALL_TASKS -> FilteringUiInfo(
            R.string.label_all, R.string.no_tasks_all,
            R.drawable.logo_no_fill
        )
        TasksFilterType.ACTIVE_TASKS -> FilteringUiInfo(
            R.string.label_active, R.string.no_tasks_active,
            R.drawable.ic_check_circle_96dp
        )
        TasksFilterType.COMPLETED_TASKS -> FilteringUiInfo(
            R.string.label_completed, R.string.no_tasks_completed,
            R.drawable.ic_verified_user_96dp
        )
    }
