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
package com.example.android.architecture.blueprints.todoapp.taskdetail

import androidx.lifecycle.SavedStateHandle
import com.example.android.architecture.blueprints.todoapp.MainCoroutineRule
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.TodoDestinationsArgs
import com.example.android.architecture.blueprints.todoapp.data.Result.Success
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.FakeRepository
import com.example.android.architecture.blueprints.todoapp.util.whileSubscribed
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for the implementation of [TaskDetailViewModel]
 */
@ExperimentalCoroutinesApi
class TaskDetailViewModelTest {

    // Set the main coroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    // Subject under test
    private lateinit var taskDetailViewModel: TaskDetailViewModel

    // Use a fake repository to be injected into the viewmodel
    private lateinit var tasksRepository: FakeRepository
    private val task = Task(title = "Title1", description = "Description1", id = "0")

    @Before
    fun setupViewModel() {
        tasksRepository = FakeRepository()
        tasksRepository.addTasks(task)

        taskDetailViewModel = TaskDetailViewModel(
            tasksRepository,
            SavedStateHandle(mapOf(TodoDestinationsArgs.TASK_ID_ARG to "0"))
        )
    }

    @Test
    fun getActiveTaskFromRepositoryAndLoadIntoView() = runTest {
        val uiState = taskDetailViewModel.uiState.first()
        // Then verify that the view was notified
        assertThat(uiState.task?.title).isEqualTo(task.title)
        assertThat(uiState.task?.description).isEqualTo(task.description)
    }

    @Test
    fun completeTask() = runTest {
        // Verify that the task was active initially
        assertThat(tasksRepository.savedTasks.value[task.id]?.isCompleted).isFalse()

        // When the ViewModel is asked to complete the task
        assertThat(taskDetailViewModel.uiState.first().task?.id).isEqualTo("0")
        taskDetailViewModel.process(Action.SetCompleted(task = task, completed = true))

        // Then the task is completed and the snackbar shows the correct message
        assertThat(tasksRepository.savedTasks.value[task.id]?.isCompleted).isTrue()
        assertThat(taskDetailViewModel.uiState.first().userMessage)
            .isEqualTo(R.string.task_marked_complete)
    }

    @Test
    fun activateTask() = runTest {
        task.isCompleted = true

        // Verify that the task was completed initially
        assertThat(tasksRepository.savedTasks.value[task.id]?.isCompleted).isTrue()

        // When the ViewModel is asked to complete the task
        assertThat(taskDetailViewModel.uiState.first().task?.id).isEqualTo("0")
        taskDetailViewModel.process(Action.SetCompleted(task = task, completed = false))

        // Then the task is not completed and the snackbar shows the correct message
        val newTask = (tasksRepository.getTask(task.id) as Success).data
        assertTrue(newTask.isActive)
        assertThat(taskDetailViewModel.uiState.first().userMessage)
            .isEqualTo(R.string.task_marked_active)
    }

    @Test
    fun taskDetailViewModel_repositoryError() = runTest {
        // Given a repository that returns errors
        tasksRepository.setReturnError(true)

        assertThat(taskDetailViewModel.uiState.value.task).isNull()
    }

    @Test
    fun deleteTask() = runTest {
        taskDetailViewModel.uiState.whileSubscribed {
            assertThat(tasksRepository.savedTasks.value.containsValue(task)).isTrue()

            // When the deletion of a task is requested
            taskDetailViewModel.process(Action.Delete)

            assertThat(tasksRepository.savedTasks.value.containsValue(task)).isFalse()
        }
    }

    @Test
    fun loadTask_loading() = runTest {
        // Set Main dispatcher to not run coroutines eagerly, for just this one test
        Dispatchers.setMain(StandardTestDispatcher())

        var isLoading: Boolean? = true
        val job = launch {
            taskDetailViewModel.uiState.collect {
                isLoading = it.isLoading
            }
        }

        // Then progress indicator is shown
        assertThat(isLoading).isTrue()

        // Execute pending coroutines actions
        advanceUntilIdle()

        // Then progress indicator is hidden
        assertThat(isLoading).isFalse()
        job.cancel()
    }
}
