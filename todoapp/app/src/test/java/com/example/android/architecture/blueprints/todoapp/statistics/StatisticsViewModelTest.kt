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
package com.example.android.architecture.blueprints.todoapp.statistics


import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.android.architecture.blueprints.todoapp.LiveDataTestUtil
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository
import com.example.android.architecture.blueprints.todoapp.util.capture
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations

/**
 * Unit tests for the implementation of [StatisticsViewModel]
 */
class StatisticsViewModelTest {

    // Executes each task synchronously using Architecture Components.
    @get:Rule var instantExecutorRule = InstantTaskExecutorRule()
    @Mock private lateinit var tasksRepository: TasksRepository
    @Captor private lateinit var loadTasksCallbackCaptor:
            ArgumentCaptor<TasksDataSource.LoadTasksCallback>
    private lateinit var statisticsViewModel: StatisticsViewModel
    private lateinit var tasks: MutableList<Task>

    @Before fun setupStatisticsViewModel() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this)

        // Get a reference to the class under test
        statisticsViewModel = StatisticsViewModel(tasksRepository)

        // We initialise the tasks to 3, with one active and two completed
        val task1 = Task("Title1", "Description1")
        val task2 = Task("Title2", "Description2").apply {
            isCompleted = true
        }
        val task3 = Task("Title3", "Description3").apply {
            isCompleted = true
        }
        tasks = Lists.newArrayList(task1, task2, task3)
    }

    @Test fun loadEmptyTasksFromRepository_EmptyResults() {
        // Given an initialized StatisticsViewModel with no tasks
        tasks.clear()

        // When loading of Tasks is requested
        statisticsViewModel.loadStatistics()

        // Callback is captured and invoked with stubbed tasks
        verify<TasksRepository>(tasksRepository).getTasks(capture(loadTasksCallbackCaptor))
        loadTasksCallbackCaptor.value.onTasksLoaded(tasks)

        // Then the results are empty
        assertThat(LiveDataTestUtil.getValue(statisticsViewModel.empty), `is`(true))
    }

    @Test fun loadNonEmptyTasksFromRepository_NonEmptyResults() {
        // When loading of Tasks is requested
        statisticsViewModel.loadStatistics()

        // Callback is captured and invoked with stubbed tasks
        verify<TasksRepository>(tasksRepository).getTasks(capture(loadTasksCallbackCaptor))
        loadTasksCallbackCaptor.value.onTasksLoaded(tasks)

        // Then the results are empty
        assertThat(LiveDataTestUtil.getValue(statisticsViewModel.empty), `is`(false))
    }


    @Test fun loadStatisticsWhenTasksAreUnavailable_CallErrorToDisplay() {
        // When statistics are loaded
        statisticsViewModel.loadStatistics()

        // And tasks data isn't available
        verify<TasksRepository>(tasksRepository).getTasks(capture(loadTasksCallbackCaptor))
        loadTasksCallbackCaptor.value.onDataNotAvailable()

        // Then an error message is shown
        assertEquals(LiveDataTestUtil.getValue(statisticsViewModel.empty), true)
        assertEquals(LiveDataTestUtil.getValue(statisticsViewModel.error), true)
    }
}
