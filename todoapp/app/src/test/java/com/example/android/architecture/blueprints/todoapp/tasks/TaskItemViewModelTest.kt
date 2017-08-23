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

import android.content.Context
import android.content.res.Resources
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository
import com.example.android.architecture.blueprints.todoapp.util.capture
import com.example.android.architecture.blueprints.todoapp.util.eq
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
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
class TaskItemViewModelTest {

    @Mock private lateinit var tasksRepository: TasksRepository
    @Mock private lateinit var context: Context
    @Mock private lateinit var taskItemNavigator: TasksActivity
    @Captor private lateinit var loadTasksCallbackCaptor: ArgumentCaptor<TasksDataSource.GetTaskCallback>
    private lateinit var taskItemViewModel: TaskItemViewModel
    private lateinit var task: Task
    private val NO_DATA_STRING = "NO_DATA_STRING"
    private val NO_DATA_DESC_STRING = "NO_DATA_DESC_STRING"

    @Before fun setupTasksViewModel() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this)

        setupContext()

        // Get a reference to the class under test
        taskItemViewModel = TaskItemViewModel(context, tasksRepository).apply {
            setNavigator(taskItemNavigator)
        }
    }

    private fun setupContext() {
        `when`(context.applicationContext).thenReturn(context)
        `when`(context.getString(R.string.no_data)).thenReturn(NO_DATA_STRING)
        `when`(context.getString(R.string.no_data_description)).thenReturn(NO_DATA_DESC_STRING)
        `when`(context.resources).thenReturn(mock(Resources::class.java))
    }

    @Test fun clickOnTask_ShowsDetailUi() {
        loadTaskIntoViewModel()

        loadTasksCallbackCaptor.value.onTaskLoaded(task) // Trigger callback

        // Then task detail UI is shown
        assertEquals(taskItemViewModel.title.get(), task.title)
        assertEquals(taskItemViewModel.description.get(), task.description)
    }

    @Test fun completeTask_ShowsTaskMarkedComplete() {
        loadTaskIntoViewModel()

        loadTasksCallbackCaptor.value.onTaskLoaded(task) // Trigger callback

        // When task is marked as complete
        taskItemViewModel.completed = true

        // Then repository is called
        verify<TasksRepository>(tasksRepository).completeTask(task)
    }

    @Test fun activateTask_ShowsTaskMarkedActive() {
        loadTaskIntoViewModel()

        loadTasksCallbackCaptor.value.onTaskLoaded(task) // Trigger callback

        // When task is marked as complete
        taskItemViewModel.completed = false

        // Then repository is called
        verify<TasksRepository>(tasksRepository).activateTask(task)
    }

    @Test fun unavailableTasks_ShowsError() {
        loadTaskIntoViewModel()

        loadTasksCallbackCaptor.value.onDataNotAvailable() // Trigger callback

        // Then repository is called
        assertFalse(taskItemViewModel.isDataAvailable)
    }

    private fun loadTaskIntoViewModel() {
        // Given a stubbed active task
        task = Task("Details Requested", "For this task")

        // When open task details is requested
        taskItemViewModel.start(task.id)

        // Use a captor to get a reference for the callback.
        verify<TasksRepository>(tasksRepository).getTask(eq(task.id),
                capture(loadTasksCallbackCaptor))
    }
}
