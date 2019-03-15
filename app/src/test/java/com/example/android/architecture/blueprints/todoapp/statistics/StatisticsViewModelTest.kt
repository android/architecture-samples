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
import com.example.android.architecture.blueprints.todoapp.FakeFailingTasksRemoteDataSource
import com.example.android.architecture.blueprints.todoapp.LiveDataTestUtil
import com.example.android.architecture.blueprints.todoapp.data.FakeRepository
import com.example.android.architecture.blueprints.todoapp.data.Task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.Executors

/**
 * Unit tests for the implementation of [StatisticsViewModel]
 */
class StatisticsViewModelTest {

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var statisticsViewModel: StatisticsViewModel
    private val tasksRepository = FakeRepository

    @Before
    fun setupStatisticsViewModel() {
        // We initialise the tasks to 3, with one active and two completed
        val task1 = Task("Title1", "Description1")
        val task2 = Task("Title2", "Description2").apply {
            isCompleted = true
        }
        val task3 = Task("Title3", "Description3").apply {
            isCompleted = true
        }
        tasksRepository.addTasks(task1, task2, task3)

        statisticsViewModel = StatisticsViewModel(tasksRepository)
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(Executors.newSingleThreadExecutor().asCoroutineDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadEmptyTasksFromRepository_EmptyResults() = runBlocking {
        // Given an initialized StatisticsViewModel with no tasks
        tasksRepository.deleteAllTasks()

        // When loading of Tasks is requested
        statisticsViewModel.loadStatistics()

        // Then the results are empty
        assertThat(LiveDataTestUtil.getValue(statisticsViewModel.empty), `is`(true))
    }

    @Test
    fun loadNonEmptyTasksFromRepository_NonEmptyResults() = runBlocking {
        // When loading of Tasks is requested
        statisticsViewModel.loadStatistics()

        // Then the results are not empty
        assertThat(LiveDataTestUtil.getValue(statisticsViewModel.empty), `is`(false))
    }

    @Test
    fun loadStatisticsWhenTasksAreUnavailable_CallErrorToDisplay() = runBlocking {
        val errorViewModel = StatisticsViewModel(FakeFailingTasksRemoteDataSource)

        // When statistics are loaded
        errorViewModel.loadStatistics()

        // Then an error message is shown
        assertEquals(LiveDataTestUtil.getValue(errorViewModel.empty), true)
        assertEquals(LiveDataTestUtil.getValue(errorViewModel.error), true)
    }
}
