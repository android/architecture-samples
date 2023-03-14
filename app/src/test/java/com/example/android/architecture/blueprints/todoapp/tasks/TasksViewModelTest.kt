/*
 * Copyright 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.architecture.blueprints.todoapp.tasks

import androidx.lifecycle.SavedStateHandle
import com.example.android.architecture.blueprints.todoapp.ADD_EDIT_RESULT_OK
import com.example.android.architecture.blueprints.todoapp.DELETE_RESULT_OK
import com.example.android.architecture.blueprints.todoapp.EDIT_RESULT_OK
import com.example.android.architecture.blueprints.todoapp.MainCoroutineRule
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.data.FakeTasksRepository
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for the implementation of [TasksViewModel]
 */
@ExperimentalCoroutinesApi
class TasksViewModelTest {

    // Subject under test
    private lateinit var tasksViewModel: TasksViewModel

    // Use a fake repository to be injected into the viewmodel
    private lateinit var tasksRepository: FakeTasksRepository

    // Set the main coroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setupViewModel() {
        // We initialise the tasks to 3, with one active and two completed
        tasksRepository = FakeTasksRepository()
        val task1 = Task("Title1", "Description1")
        val task2 = Task("Title2", "Description2", true)
        val task3 = Task("Title3", "Description3", true)
        tasksRepository.addTasks(task1, task2, task3)

        tasksViewModel = TasksViewModel(tasksRepository, SavedStateHandle())
    }

    @Test
    fun loadAllTasksFromRepository_loadingTogglesAndDataLoaded() = runTest {
        // Set Main dispatcher to not run coroutines eagerly, for just this one test
        Dispatchers.setMain(StandardTestDispatcher())

        // Given an initialized TasksViewModel with initialized tasks
        // When loading of Tasks is requested
        tasksViewModel.setFiltering(TasksFilterType.ALL_TASKS)

        // Trigger loading of tasks
        tasksViewModel.refresh()

        // Then progress indicator is shown
        assertThat(tasksViewModel.uiState.first().isLoading).isTrue()

        // Execute pending coroutines actions
        advanceUntilIdle()

        // Then progress indicator is hidden
        assertThat(tasksViewModel.uiState.first().isLoading).isFalse()

        // And data correctly loaded
        assertThat(tasksViewModel.uiState.first().items).hasSize(3)
    }

    @Test
    fun loadActiveTasksFromRepositoryAndLoadIntoView() = runTest {
        // Given an initialized TasksViewModel with initialized tasks
        // When loading of Tasks is requested
        tasksViewModel.setFiltering(TasksFilterType.ACTIVE_TASKS)

        // Load tasks
        tasksViewModel.refresh()

        // Then progress indicator is hidden
        assertThat(tasksViewModel.uiState.first().isLoading).isFalse()

        // And data correctly loaded
        assertThat(tasksViewModel.uiState.first().items).hasSize(1)
    }

    @Test
    fun loadCompletedTasksFromRepositoryAndLoadIntoView() = runTest {
        // Given an initialized TasksViewModel with initialized tasks
        // When loading of Tasks is requested
        tasksViewModel.setFiltering(TasksFilterType.COMPLETED_TASKS)

        // Load tasks
        tasksViewModel.refresh()

        // Then progress indicator is hidden
        assertThat(tasksViewModel.uiState.first().isLoading).isFalse()

        // And data correctly loaded
        assertThat(tasksViewModel.uiState.first().items).hasSize(2)
    }

    @Test
    fun loadTasks_error() = runTest {
        // Make the repository throw errors
        tasksRepository.setShouldThrowError(true)

        // Load tasks
        tasksViewModel.refresh()

        // Then progress indicator is hidden
        assertThat(tasksViewModel.uiState.first().isLoading).isFalse()

        // And the list of items is empty
        assertThat(tasksViewModel.uiState.first().items).isEmpty()
        assertThat(tasksViewModel.uiState.first().userMessage)
            .isEqualTo(R.string.loading_tasks_error)
    }

    @Test
    fun clearCompletedTasks_clearsTasks() = runTest {
        // When completed tasks are cleared
        tasksViewModel.clearCompletedTasks()

        // Fetch tasks
        tasksViewModel.refresh()

        // Fetch tasks
        val allTasks = tasksViewModel.uiState.first().items
        val completedTasks = allTasks?.filter { it.isCompleted }

        // Verify there are no completed tasks left
        assertThat(completedTasks).isEmpty()

        // Verify active task is not cleared
        assertThat(allTasks).hasSize(1)

        // Verify snackbar is updated
        assertThat(tasksViewModel.uiState.first().userMessage)
            .isEqualTo(R.string.completed_tasks_cleared)
    }

    @Test
    fun showEditResultMessages_editOk_snackbarUpdated() = runTest {
        // When the viewmodel receives a result from another destination
        tasksViewModel.showEditResultMessage(EDIT_RESULT_OK)

        // The snackbar is updated
        assertThat(tasksViewModel.uiState.first().userMessage)
            .isEqualTo(R.string.successfully_saved_task_message)
    }

    @Test
    fun showEditResultMessages_addOk_snackbarUpdated() = runTest {
        // When the viewmodel receives a result from another destination
        tasksViewModel.showEditResultMessage(ADD_EDIT_RESULT_OK)

        // The snackbar is updated
        assertThat(tasksViewModel.uiState.first().userMessage)
            .isEqualTo(R.string.successfully_added_task_message)
    }

    @Test
    fun showEditResultMessages_deleteOk_snackbarUpdated() = runTest {
        // When the viewmodel receives a result from another destination
        tasksViewModel.showEditResultMessage(DELETE_RESULT_OK)

        // The snackbar is updated
        assertThat(tasksViewModel.uiState.first().userMessage)
            .isEqualTo(R.string.successfully_deleted_task_message)
    }

    @Test
    fun completeTask_dataAndSnackbarUpdated() = runTest {
        // With a repository that has an active task
        val task = Task("Title", "Description")
        tasksRepository.addTasks(task)

        // Complete task
        tasksViewModel.completeTask(task, true)

        // Verify the task is completed
        assertThat(tasksRepository.savedTasks.value[task.id]?.isCompleted).isTrue()

        // The snackbar is updated
        assertThat(tasksViewModel.uiState.first().userMessage)
            .isEqualTo(R.string.task_marked_complete)
    }

    @Test
    fun activateTask_dataAndSnackbarUpdated() = runTest {
        // With a repository that has a completed task
        val task = Task("Title", "Description", true)
        tasksRepository.addTasks(task)

        // Activate task
        tasksViewModel.completeTask(task, false)

        // Verify the task is active
        assertThat(tasksRepository.savedTasks.value[task.id]?.isActive).isTrue()

        // The snackbar is updated
        assertThat(tasksViewModel.uiState.first().userMessage)
            .isEqualTo(R.string.task_marked_active)
    }
}
