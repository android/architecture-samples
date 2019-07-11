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

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.android.architecture.blueprints.todoapp.MainCoroutineRule
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.assertLiveDataEventTriggered
import com.example.android.architecture.blueprints.todoapp.assertSnackbarMessage
import com.example.android.architecture.blueprints.todoapp.awaitNextValue
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.FakeRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
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
    private lateinit var tasksRepository: FakeRepository

    // Set the main coroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setupViewModel() {
        // We initialise the tasks to 3, with one active and two completed
        tasksRepository = FakeRepository()
        val task1 = Task("Title1", "Description1")
        val task2 = Task("Title2", "Description2", true)
        val task3 = Task("Title3", "Description3", true)
        tasksRepository.addTasks(task1, task2, task3)

        tasksViewModel = TasksViewModel(tasksRepository)
    }

    @Test
    fun loadAllTasksFromRepository_loadingTogglesAndDataLoaded() {
        // Pause dispatcher so we can verify initial values
        mainCoroutineRule.pauseDispatcher()

        // Given an initialized TasksViewModel with initialized tasks
        // When loading of Tasks is requested
        tasksViewModel.setFiltering(TasksFilterType.ALL_TASKS)

        // Trigger loading of tasks
        tasksViewModel.loadTasks(true)
        // Observe the items to keep LiveData emitting
        tasksViewModel.items.observeForever { }

        // Then progress indicator is shown
        assertThat(tasksViewModel.dataLoading.awaitNextValue()).isTrue()

        // Execute pending coroutines actions
        mainCoroutineRule.resumeDispatcher()

        // Then progress indicator is hidden
        assertThat(tasksViewModel.dataLoading.awaitNextValue()).isFalse()

        // And data correctly loaded
        assertThat(tasksViewModel.items.awaitNextValue()).hasSize(3)
    }

    @Test
    fun loadActiveTasksFromRepositoryAndLoadIntoView() {
        // Given an initialized TasksViewModel with initialized tasks
        // When loading of Tasks is requested
        tasksViewModel.setFiltering(TasksFilterType.ACTIVE_TASKS)

        // Load tasks
        tasksViewModel.loadTasks(true)
        // Observe the items to keep LiveData emitting
        tasksViewModel.items.observeForever { }

        // Then progress indicator is hidden
        assertThat(tasksViewModel.dataLoading.awaitNextValue()).isFalse()

        // And data correctly loaded
        assertThat(tasksViewModel.items.awaitNextValue()).hasSize(1)
    }

    @Test
    fun loadCompletedTasksFromRepositoryAndLoadIntoView() {
        // Given an initialized TasksViewModel with initialized tasks
        // When loading of Tasks is requested
        tasksViewModel.setFiltering(TasksFilterType.COMPLETED_TASKS)

        // Load tasks
        tasksViewModel.loadTasks(true)
        // Observe the items to keep LiveData emitting
        tasksViewModel.items.observeForever { }

        // Then progress indicator is hidden
        assertThat(tasksViewModel.dataLoading.awaitNextValue()).isFalse()

        // And data correctly loaded
        assertThat(tasksViewModel.items.awaitNextValue()).hasSize(2)
    }

    @Test
    fun loadTasks_error() {
        // Make the repository return errors
        tasksRepository.setReturnError(true)

        // Load tasks
        tasksViewModel.loadTasks(true)
        // Observe the items to keep LiveData emitting
        tasksViewModel.items.observeForever { }

        // Then progress indicator is hidden
        assertThat(tasksViewModel.dataLoading.awaitNextValue()).isFalse()

        // And the list of items is empty
        assertThat(tasksViewModel.items.awaitNextValue()).isEmpty()

        // And the snackbar updated
        assertSnackbarMessage(tasksViewModel.snackbarText, R.string.loading_tasks_error)
    }

    @Test
    fun clickOnFab_showsAddTaskUi() {
        // When adding a new task
        tasksViewModel.addNewTask()

        // Then the event is triggered
        val value = tasksViewModel.newTaskEvent.awaitNextValue()
        assertThat(value.getContentIfNotHandled()).isNotNull()
    }

    @Test
    fun clickOnOpenTask_setsEvent() {
        // When opening a new task
        val taskId = "42"
        tasksViewModel.openTask(taskId)

        // Then the event is triggered
        assertLiveDataEventTriggered(tasksViewModel.openTaskEvent, taskId)
    }

    @Test
    fun clearCompletedTasks_clearsTasks() = mainCoroutineRule.runBlockingTest {
        // When completed tasks are cleared
        tasksViewModel.clearCompletedTasks()

        // Fetch tasks
        tasksViewModel.loadTasks(true)

        // Fetch tasks
        val allTasks = tasksViewModel.items.awaitNextValue()
        val completedTasks = allTasks.filter { it.isCompleted }

        // Verify there are no completed tasks left
        assertThat(completedTasks).isEmpty()

        // Verify active task is not cleared
        assertThat(allTasks).hasSize(1)

        // Verify snackbar is updated
        assertSnackbarMessage(
            tasksViewModel.snackbarText, R.string.completed_tasks_cleared
        )
    }

    @Test
    fun showEditResultMessages_editOk_snackbarUpdated() {
        // When the viewmodel receives a result from another destination
        tasksViewModel.showEditResultMessage(EDIT_RESULT_OK)

        // The snackbar is updated
        assertSnackbarMessage(
            tasksViewModel.snackbarText, R.string.successfully_saved_task_message
        )
    }

    @Test
    fun showEditResultMessages_addOk_snackbarUpdated() {
        // When the viewmodel receives a result from another destination
        tasksViewModel.showEditResultMessage(ADD_EDIT_RESULT_OK)

        // The snackbar is updated
        assertSnackbarMessage(
            tasksViewModel.snackbarText, R.string.successfully_added_task_message
        )
    }

    @Test
    fun showEditResultMessages_deleteOk_snackbarUpdated() {
        // When the viewmodel receives a result from another destination
        tasksViewModel.showEditResultMessage(DELETE_RESULT_OK)

        // The snackbar is updated
        assertSnackbarMessage(tasksViewModel.snackbarText, R.string.successfully_deleted_task_message)
    }

    @Test
    fun completeTask_dataAndSnackbarUpdated() {
        // With a repository that has an active task
        val task = Task("Title", "Description")
        tasksRepository.addTasks(task)

        // Complete task
        tasksViewModel.completeTask(task, true)

        // Verify the task is completed
        assertThat(tasksRepository.tasksServiceData[task.id]?.isCompleted).isTrue()

        // The snackbar is updated
        assertSnackbarMessage(
            tasksViewModel.snackbarText, R.string.task_marked_complete
        )
    }

    @Test
    fun activateTask_dataAndSnackbarUpdated() {
        // With a repository that has a completed task
        val task = Task("Title", "Description", true)
        tasksRepository.addTasks(task)

        // Activate task
        tasksViewModel.completeTask(task, false)

        // Verify the task is active
        assertThat(tasksRepository.tasksServiceData[task.id]?.isActive).isTrue()

        // The snackbar is updated
        assertSnackbarMessage(
            tasksViewModel.snackbarText, R.string.task_marked_active
        )
    }

    @Test
    fun getTasksAddViewVisible() {
        // When the filter type is ALL_TASKS
        tasksViewModel.setFiltering(TasksFilterType.ALL_TASKS)

        // Then the "Add task" action is visible
        assertThat(tasksViewModel.tasksAddViewVisible.awaitNextValue()).isTrue()
    }
}
