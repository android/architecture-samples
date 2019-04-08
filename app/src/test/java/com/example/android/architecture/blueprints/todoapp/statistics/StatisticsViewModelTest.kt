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
package com.example.android.architecture.blueprints.todoapp.statistics

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.android.architecture.blueprints.todoapp.FakeFailingTasksRemoteDataSource
import com.example.android.architecture.blueprints.todoapp.LiveDataTestUtil
import com.example.android.architecture.blueprints.todoapp.ViewModelScopeMainDispatcherRule
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.DefaultTasksRepository
import com.example.android.architecture.blueprints.todoapp.data.source.FakeRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineContext
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for the implementation of [StatisticsViewModel]
 */
@ObsoleteCoroutinesApi
class StatisticsViewModelTest {

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    // Subject under test
    private lateinit var statisticsViewModel: StatisticsViewModel

    // Use a fake repository to be injected into the viewmodel
    private val tasksRepository = FakeRepository()

    // Set the main coroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    var coroutinesMainDispatcherRule = ViewModelScopeMainDispatcherRule()

    // A CoroutineContext that can be controlled from tests
    private val testContext = TestCoroutineContext()

    @Before
    fun setupStatisticsViewModel() {
        statisticsViewModel = StatisticsViewModel(tasksRepository, testContext)
    }

    @Test
    fun loadEmptyTasksFromRepository_EmptyResults() = runBlocking {
        // Given an initialized StatisticsViewModel with no tasks

        // When loading of Tasks is requested
        statisticsViewModel.loadStatistics()

        // Execute pending coroutines actions
        testContext.triggerActions()

        // Then the results are empty
        assertThat(LiveDataTestUtil.getValue(statisticsViewModel.empty), `is`(true))
    }

    @Test
    fun loadNonEmptyTasksFromRepository_NonEmptyResults() {
        // We initialise the tasks to 3, with one active and two completed
        val task1 = Task("Title1", "Description1")
        val task2 = Task("Title2", "Description2").apply {
            isCompleted = true
        }
        val task3 = Task("Title3", "Description3").apply {
            isCompleted = true
        }
        tasksRepository.addTasks(task1, task2, task3)

        // When loading of Tasks is requested
        statisticsViewModel.loadStatistics()

        // Execute pending coroutines actions
        testContext.triggerActions()

        // Then the results are not empty
        assertEquals(LiveDataTestUtil.getValue(statisticsViewModel.empty), false)
        assertEquals(LiveDataTestUtil.getValue(statisticsViewModel.numberOfActiveTasks), 1)
        assertEquals(LiveDataTestUtil.getValue(statisticsViewModel.numberOfCompletedTasks), 2)
    }

    @Test
    fun loadStatisticsWhenTasksAreUnavailable_CallErrorToDisplay() = runBlocking {
        val errorViewModel = StatisticsViewModel(
            DefaultTasksRepository(
                FakeFailingTasksRemoteDataSource,
                FakeFailingTasksRemoteDataSource)
        )

        // When statistics are loaded
        errorViewModel.loadStatistics()

        // Then an error message is shown
        assertEquals(LiveDataTestUtil.getValue(errorViewModel.empty), true)
        assertEquals(LiveDataTestUtil.getValue(errorViewModel.error), true)
    }

    @Test
    fun loadTasks_loading() {
        // Load the task in the viewmodel
        statisticsViewModel.start()

        // Then progress indicator is shown
        Assert.assertTrue(LiveDataTestUtil.getValue(statisticsViewModel.dataLoading))

        // Execute pending coroutines actions
        testContext.triggerActions()

        // Then progress indicator is hidden
        Assert.assertFalse(LiveDataTestUtil.getValue(statisticsViewModel.dataLoading))
    }
}
