/*
 * Copyright 2017, The Android Open Source Project
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

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import android.content.Context
import android.content.res.Resources
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.R.string.successfully_deleted_task_message
import com.example.android.architecture.blueprints.todoapp.TestUtils
import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskActivity
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource.LoadTasksCallback
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository
import com.example.android.architecture.blueprints.todoapp.util.ADD_EDIT_RESULT_OK
import com.example.android.architecture.blueprints.todoapp.util.DELETE_RESULT_OK
import com.example.android.architecture.blueprints.todoapp.util.EDIT_RESULT_OK
import com.example.android.architecture.blueprints.todoapp.util.any
import com.example.android.architecture.blueprints.todoapp.util.capture
import com.example.android.architecture.blueprints.todoapp.util.mock
import com.google.common.collect.Lists
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations

/**
 * Unit tests for the implementation of [TasksViewModel]
 */
class TasksViewModelTest {
    // Executes each task synchronously using Architecture Components.
    @get:Rule var instantExecutorRule = InstantTaskExecutorRule()
    @Mock private lateinit var tasksRepository: TasksRepository
    @Mock private lateinit var context: Application
    @Captor private lateinit var loadTasksCallbackCaptor: ArgumentCaptor<LoadTasksCallback>
    private lateinit var tasksViewModel: TasksViewModel
    private lateinit var tasks: List<Task>

    @Before fun setupTasksViewModel() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this)

        setupContext()

        // Get a reference to the class under test
        tasksViewModel = TasksViewModel(context, tasksRepository)

        // We initialise the tasks to 3, with one active and two completed
        val task1 = Task("Title1", "Description1")
        val task2 = Task("Title2", "Description2").apply {
            isCompleted = true
        }
        val task3 = Task("Title3", "Description3").apply {
            isCompleted = true
        }
        tasks = Lists.newArrayList(task1, task2, task3)

        tasksViewModel.snackbarMessage.removeObservers(TestUtils.TEST_OBSERVER)

    }

    private fun setupContext() {
        `when`<Context>(context.applicationContext).thenReturn(context)
        `when`(context.getString(R.string.successfully_saved_task_message))
                .thenReturn("EDIT_RESULT_OK")
        `when`(context.getString(R.string.successfully_added_task_message))
                .thenReturn("ADD_EDIT_RESULT_OK")
        `when`(context.getString(successfully_deleted_task_message))
                .thenReturn("DELETE_RESULT_OK")

        `when`(context.resources).thenReturn(mock(Resources::class.java))
    }

    @Test fun loadAllTasksFromRepository_dataLoaded() {
        with(tasksViewModel) {
            // Given an initialized TasksViewModel with initialized tasks
            // When loading of Tasks is requested
            currentFiltering = TasksFilterType.ALL_TASKS
            loadTasks(true)

            // Callback is captured and invoked with stubbed tasks
            verify<TasksRepository>(tasksRepository).getTasks(capture(loadTasksCallbackCaptor))


            // Then progress indicator is shown
            assertTrue(dataLoading.get())
            loadTasksCallbackCaptor.value.onTasksLoaded(tasks)

            // Then progress indicator is hidden
            assertFalse(dataLoading.get())

            // And data loaded
            assertFalse(items.isEmpty())
            assertTrue(items.size == 3)
        }
    }

    @Test fun loadActiveTasksFromRepositoryAndLoadIntoView() {
        with(tasksViewModel) {
            // Given an initialized TasksViewModel with initialized tasks
            // When loading of Tasks is requested
            currentFiltering = TasksFilterType.ACTIVE_TASKS
            loadTasks(true)

            // Callback is captured and invoked with stubbed tasks
            verify<TasksRepository>(tasksRepository).getTasks(capture(loadTasksCallbackCaptor))
            loadTasksCallbackCaptor.value.onTasksLoaded(tasks)

            // Then progress indicator is hidden
            assertFalse(dataLoading.get())

            // And data loaded
            assertFalse(items.isEmpty())
            assertTrue(items.size == 1)
        }
    }

    @Test fun loadCompletedTasksFromRepositoryAndLoadIntoView() {
        with(tasksViewModel) {
            // Given an initialized TasksViewModel with initialized tasks
            // When loading of Tasks is requested
            currentFiltering = TasksFilterType.COMPLETED_TASKS
            loadTasks(true)

            // Callback is captured and invoked with stubbed tasks
            verify<TasksRepository>(tasksRepository).getTasks(capture(loadTasksCallbackCaptor))
            loadTasksCallbackCaptor.value.onTasksLoaded(tasks)

            // Then progress indicator is hidden
            assertFalse(dataLoading.get())

            // And data loaded
            assertFalse(items.isEmpty())
            assertTrue(items.size == 2)
        }
    }

    @Test fun clickOnFab_ShowsAddTaskUi() {

        val observer = mock<Observer<Void>>()

        with(tasksViewModel) {
            newTaskEvent.observe(TestUtils.TEST_OBSERVER, observer)

            // When adding a new task
            addNewTask()
        }

        // Then the event is triggered
        verify<Observer<Void>>(observer).onChanged(null)
    }

    @Test fun clearCompletedTasks_ClearsTasks() {
        // When completed tasks are cleared
        tasksViewModel.clearCompletedTasks()

        // Then repository is called and the view is notified
        verify<TasksRepository>(tasksRepository).clearCompletedTasks()
        verify<TasksRepository>(tasksRepository).getTasks(any<LoadTasksCallback>())
    }

    @Test fun handleActivityResult_editOK() {
        // When TaskDetailActivity sends a EDIT_RESULT_OK
        val observer = mock<Observer<Int>>()

        with(tasksViewModel) {
            snackbarMessage.observe(TestUtils.TEST_OBSERVER, observer)

            handleActivityResult(AddEditTaskActivity.REQUEST_CODE, EDIT_RESULT_OK)
        }

        // Then the snackbar shows the correct message
        verify<Observer<Int>>(observer).onChanged(R.string.successfully_saved_task_message)
    }

    @Test fun handleActivityResult_addEditOK() {
        // When TaskDetailActivity sends a EDIT_RESULT_OK
        val observer = mock<Observer<Int>>()

        with(tasksViewModel) {
            snackbarMessage.observe(TestUtils.TEST_OBSERVER, observer)

            // When AddEditTaskActivity sends a ADD_EDIT_RESULT_OK
            handleActivityResult(
                    AddEditTaskActivity.REQUEST_CODE, ADD_EDIT_RESULT_OK)
        }

        // Then the snackbar shows the correct message
        verify<Observer<Int>>(observer).onChanged(R.string.successfully_added_task_message)
    }

    @Test fun handleActivityResult_deleteOk() {
        // When TaskDetailActivity sends a EDIT_RESULT_OK
        val observer = mock<Observer<Int>>()

        with(tasksViewModel) {
            snackbarMessage.observe(TestUtils.TEST_OBSERVER, observer)

            // When AddEditTaskActivity sends a ADD_EDIT_RESULT_OK
            handleActivityResult(
                    AddEditTaskActivity.REQUEST_CODE, DELETE_RESULT_OK)
        }

        // Then the snackbar shows the correct message
        verify<Observer<Int>>(observer).onChanged(R.string.successfully_deleted_task_message)
    }

    @Test fun getTasksAddViewVisible() {
        with(tasksViewModel) {
            // When the filter type is ALL_TASKS
            currentFiltering = TasksFilterType.ALL_TASKS

            // Then the "Add task" action is visible
            assertThat(tasksAddViewVisible.get(), `is`(true))
        }
    }
}
