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
package com.example.android.architecture.blueprints.todoapp.addedittask

import androidx.lifecycle.SavedStateHandle
import com.example.android.architecture.blueprints.todoapp.MainCoroutineRule
import com.example.android.architecture.blueprints.todoapp.R.string
import com.example.android.architecture.blueprints.todoapp.TodoDestinationsArgs
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.FakeRepository
import com.example.android.architecture.blueprints.todoapp.util.whileSubscribed
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for the implementation of [AddEditTaskViewModel].
 */
@ExperimentalCoroutinesApi
class AddEditTaskViewModelTest {

    // Subject under test
    private lateinit var addEditTaskViewModel: AddEditTaskViewModel

    // Use a fake repository to be injected into the viewmodel
    private lateinit var tasksRepository: FakeRepository
    private val task = Task(title = "Title1", description = "Description1", id = "0")

    // Set the main coroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setupViewModel() {
        // We initialise the repository with no tasks
        tasksRepository = FakeRepository().apply {
            addTasks(task)
        }
    }

    @Test
    fun saveNewTaskToRepository_showsSuccessMessageUi() = runTest {
        addEditTaskViewModel = AddEditTaskViewModel(
            tasksRepository,
            SavedStateHandle(mapOf(TodoDestinationsArgs.TASK_ID_ARG to "0"))
        )

        addEditTaskViewModel.uiState.whileSubscribed {
            val newTitle = "New Task Title"
            val newDescription = "Some Task Description"
            addEditTaskViewModel.apply {
                process(Action.UpdateTitle(newTitle))
                process(Action.UpdateDescription(newDescription))
            }
            val uiState = addEditTaskViewModel.uiState.value
            addEditTaskViewModel.process(
                Action.Save(
                    taskId = uiState.taskId,
                    title = uiState.title,
                    description = uiState.description,
                    taskCompleted = uiState.isTaskCompleted,
                )
            )

            val newTask = tasksRepository.savedTasks.value.values.first()

            // Then a task is saved in the repository and the view updated
            assertThat(newTask.title).isEqualTo(newTitle)
            assertThat(newTask.description).isEqualTo(newDescription)
        }
    }

    @Test
    fun loadTasks_loading() = runTest {
        // Set Main dispatcher to not run coroutines eagerly, for just this one test
        Dispatchers.setMain(StandardTestDispatcher())

        addEditTaskViewModel = AddEditTaskViewModel(
            tasksRepository,
            SavedStateHandle(mapOf(TodoDestinationsArgs.TASK_ID_ARG to "0"))
        )

        addEditTaskViewModel.uiState.whileSubscribed {
            // Then progress indicator is shown
            assertThat(addEditTaskViewModel.uiState.value.isLoading).isTrue()

            // Execute pending coroutines actions
            advanceUntilIdle()

            // Then progress indicator is hidden
            assertThat(addEditTaskViewModel.uiState.value.isLoading).isFalse()
        }
    }

    @Test
    fun loadTasks_taskShown() = runTest {
        addEditTaskViewModel = AddEditTaskViewModel(
            tasksRepository,
            SavedStateHandle(mapOf(TodoDestinationsArgs.TASK_ID_ARG to "0"))
        )

        addEditTaskViewModel.uiState.whileSubscribed {
            // Add task to repository
            tasksRepository.addTasks(task)

            // Verify a task is loaded
            val uiState = addEditTaskViewModel.uiState.value
            assertThat(uiState.title).isEqualTo(task.title)
            assertThat(uiState.description).isEqualTo(task.description)
            assertThat(uiState.isLoading).isFalse()
        }
    }

    @Test
    fun saveNewTaskToRepository_emptyTitle_error() = runTest {
        addEditTaskViewModel = AddEditTaskViewModel(
            tasksRepository,
            SavedStateHandle(mapOf(TodoDestinationsArgs.TASK_ID_ARG to "0"))
        )

        addEditTaskViewModel.uiState.whileSubscribed {
            saveTaskAndAssertUserMessage("", "Some Task Description")
        }
    }

    @Test
    fun saveNewTaskToRepository_emptyDescription_error() = runTest {
        addEditTaskViewModel = AddEditTaskViewModel(
            tasksRepository,
            SavedStateHandle(mapOf(TodoDestinationsArgs.TASK_ID_ARG to "0"))
        )

        addEditTaskViewModel.uiState.whileSubscribed {
            saveTaskAndAssertUserMessage("Title", "")
        }
    }

    @Test
    fun saveNewTaskToRepository_emptyDescriptionEmptyTitle_error() = runTest {
        addEditTaskViewModel = AddEditTaskViewModel(
            tasksRepository,
            SavedStateHandle(mapOf(TodoDestinationsArgs.TASK_ID_ARG to "0"))
        )

        addEditTaskViewModel.uiState.whileSubscribed {
            saveTaskAndAssertUserMessage("", "")
        }
    }

    private fun saveTaskAndAssertUserMessage(title: String, description: String) = runTest {
        addEditTaskViewModel.apply {
            process(Action.UpdateTitle(title))
            process(Action.UpdateDescription(description))
        }

        addEditTaskViewModel.uiState.whileSubscribed {
            // When saving an incomplete task
            val uiState = addEditTaskViewModel.uiState.value
            addEditTaskViewModel.process(
                Action.Save(
                    taskId = uiState.taskId,
                    title = uiState.title,
                    description = uiState.description,
                    taskCompleted = uiState.isTaskCompleted,
                )
            )
            assertThat(
                addEditTaskViewModel.uiState.value.userMessage
            ).isEqualTo(string.empty_task_message)
        }
    }
}
