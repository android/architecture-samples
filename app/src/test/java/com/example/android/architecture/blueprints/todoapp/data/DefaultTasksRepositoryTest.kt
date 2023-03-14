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

package com.example.android.architecture.blueprints.todoapp.data

import com.example.android.architecture.blueprints.todoapp.MainCoroutineRule
import com.example.android.architecture.blueprints.todoapp.data.source.local.FakeTasksDao
import com.example.android.architecture.blueprints.todoapp.data.source.network.FakeNetworkDataSource
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for the implementation of the in-memory repository with cache.
 */
@ExperimentalCoroutinesApi
class DefaultTasksRepositoryTest {

    private val task1 = Task(title = "Title1", description = "Description1")
    private val task2 = Task(title = "Title2", description = "Description2")
    private val task3 = Task(title = "Title3", description = "Description3")
    private val newTask = Task(title = "Title new", description = "Description new")
    private val networkTasks = listOf(task1, task2).toNetworkModels().sortedBy { it.id }
    private val localTasks = listOf(task3.toLocalModel()).sortedBy { it.id }

    private val newTasks = listOf(newTask).sortedBy { it.id }
    private lateinit var tasksNetworkDataSource: FakeNetworkDataSource
    private lateinit var tasksLocalDataSource: FakeTasksDao

    // Class under test
    private lateinit var tasksRepository: DefaultTasksRepository

    // Set the main coroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @ExperimentalCoroutinesApi
    @Before
    fun createRepository() {
        tasksNetworkDataSource = FakeNetworkDataSource(networkTasks.toMutableList())
        tasksLocalDataSource = FakeTasksDao(localTasks.toMutableList())
        // Get a reference to the class under test
        tasksRepository = DefaultTasksRepository(
            tasksNetworkDataSource, tasksLocalDataSource
        )
    }

    @ExperimentalCoroutinesApi
    @Test
    fun getTasks_emptyRepositoryAndUninitializedCache() = runTest {
        val emptyRemoteSource = FakeNetworkDataSource()
        val emptyLocalSource = FakeTasksDao()

        val tasksRepository = DefaultTasksRepository(
            emptyRemoteSource, emptyLocalSource
        )

        assertThat(tasksRepository.getTasks().size).isEqualTo(0)
    }

    @Test
    fun getTasks_repositoryCachesAfterFirstApiCall() = runTest {
        // Trigger the repository to load tasks from the remote data source
        val initial = tasksRepository.getTasks(forceUpdate = true)

        // Change the remote data source
        tasksNetworkDataSource.tasks = newTasks.toNetworkModels().toMutableList()

        // Load the tasks again without forcing a refresh
        val second = tasksRepository.getTasks()

        // Initial and second should match because we didn't force a refresh (no tasks were loaded
        // from the remote data source)
        assertThat(second).isEqualTo(initial)
    }

    @Test
    fun getTasks_requestsAllTasksFromRemoteDataSource() = runTest {
        // When tasks are requested from the tasks repository
        val tasks = tasksRepository.getTasks(true)

        // Then tasks are loaded from the remote data source
        assertThat(tasks).isEqualTo(networkTasks.toExternalModels())
    }

    @Test
    fun saveTask_savesToLocalAndRemote() = runTest {
        // Make sure newTask is not in the remote or local datasources
        assertThat(tasksNetworkDataSource.tasks).doesNotContain(newTask.toNetworkModel())
        assertThat(tasksLocalDataSource.tasks).doesNotContain(newTask.toLocalModel())

        // When a task is saved to the tasks repository
        val newTask = tasksRepository.createTask(newTask.title, newTask.description)

        // Then the remote and local sources are called
        assertThat(tasksNetworkDataSource.tasks).contains(newTask.toNetworkModel())
        assertThat(tasksLocalDataSource.tasks?.contains(newTask.toLocalModel()))
    }

    @Test
    fun getTasks_WithDirtyCache_tasksAreRetrievedFromRemote() = runTest {
        // First call returns from REMOTE
        val tasks = tasksRepository.getTasks()

        // Set a different list of tasks in REMOTE
        tasksNetworkDataSource.tasks = newTasks.toNetworkModels().toMutableList()

        // But if tasks are cached, subsequent calls load from cache
        val cachedTasks = tasksRepository.getTasks()
        assertThat(cachedTasks).isEqualTo(tasks)

        // Now force remote loading
        val refreshedTasks = tasksRepository.getTasks(true)

        // Tasks must be the recently updated in REMOTE
        assertThat(refreshedTasks).isEqualTo(newTasks)
    }

    @Test(expected = Exception::class)
    fun getTasks_WithDirtyCache_remoteUnavailable_throwsException() = runTest {
        // Make remote data source unavailable
        tasksNetworkDataSource.tasks = null

        // Load tasks forcing remote load
        tasksRepository.getTasks(true)

        // Exception should be thrown
    }

    @Test
    fun getTasks_WithRemoteDataSourceUnavailable_tasksAreRetrievedFromLocal() =
        runTest {
            // When the remote data source is unavailable
            tasksNetworkDataSource.tasks = null

            // The repository fetches from the local source
            assertThat(tasksRepository.getTasks()).isEqualTo(localTasks.toExternalModels())
        }

    @Test(expected = Exception::class)
    fun getTasks_WithBothDataSourcesUnavailable_throwsError() = runTest {
        // When both sources are unavailable
        tasksNetworkDataSource.tasks = null
        tasksLocalDataSource.tasks = null

        // The repository throws an error
        tasksRepository.getTasks()
    }

    @Test
    fun getTasks_refreshesLocalDataSource() = runTest {
        val initialLocal = tasksLocalDataSource.tasks

        // Forcing an update will fetch tasks from remote
        val newTasks = tasksRepository.getTasks(true)

        assertThat(newTasks).isEqualTo(networkTasks.toExternalModels())
        assertThat(newTasks).isEqualTo(tasksLocalDataSource.tasks?.toExternalModels())
        assertThat(tasksLocalDataSource.tasks).isEqualTo(initialLocal)
    }

    @Test
    fun completeTask_completesTaskToServiceAPIUpdatesCache() = runTest {
        // Save a task
        val newTask = tasksRepository.createTask(newTask.title, newTask.description)

        // Make sure it's active
        assertThat(tasksRepository.getTask(newTask.id)?.isCompleted).isFalse()

        // Mark is as complete
        tasksRepository.completeTask(newTask.id)

        // Verify it's now completed
        assertThat(tasksRepository.getTask(newTask.id)?.isCompleted).isTrue()
    }

    @Test
    fun completeTask_activeTaskToServiceAPIUpdatesCache() = runTest {
        // Save a task
        val newTask = tasksRepository.createTask(newTask.title, newTask.description)
        tasksRepository.completeTask(newTask.id)

        // Make sure it's completed
        assertThat(tasksRepository.getTask(newTask.id)?.isActive).isFalse()

        // Mark is as active
        tasksRepository.activateTask(newTask.id)

        // Verify it's now activated
        assertThat(tasksRepository.getTask(newTask.id)?.isActive).isTrue()
    }

    @Test
    fun getTask_repositoryCachesAfterFirstApiCall() = runTest {
        // Obtain a task from the local data source
        tasksLocalDataSource.tasks = mutableListOf(task1.toLocalModel())
        val initial = tasksRepository.getTask(task1.id)

        // Change the tasks on the remote
        tasksNetworkDataSource.tasks = newTasks.toNetworkModels().toMutableList()

        // Obtain the same task again
        val second = tasksRepository.getTask(task1.id)

        // Initial and second tasks should match because we didn't force a refresh
        assertThat(second).isEqualTo(initial)
    }

    @Test
    fun getTask_forceRefresh() = runTest {
        // Trigger the repository to load data, which loads from remote and caches
        tasksNetworkDataSource.tasks = mutableListOf(task1.toNetworkModel())
        val task1FirstTime = tasksRepository.getTask(task1.id, forceUpdate = true)
        assertThat(task1FirstTime?.id).isEqualTo(task1.id)

        // Configure the remote data source to return a different task
        tasksNetworkDataSource.tasks = mutableListOf(task2.toNetworkModel())

        // Force refresh
        val task1SecondTime = tasksRepository.getTask(task1.id, true)
        val task2SecondTime = tasksRepository.getTask(task2.id, true)

        // Only task2 works because task1 does not exist on the remote
        assertThat(task1SecondTime).isNull()
        assertThat(task2SecondTime?.id).isEqualTo(task2.id)
    }

    @Test
    fun clearCompletedTasks() = runTest {
        val completedTask = task1.copy(isCompleted = true)
        tasksLocalDataSource.tasks = mutableListOf(
            completedTask.toLocalModel(),
            task2.toLocalModel()
        )
        tasksRepository.clearCompletedTasks()

        val tasks = tasksRepository.getTasks(true)

        assertThat(tasks).hasSize(1)
        assertThat(tasks).contains(task2)
        assertThat(tasks).doesNotContain(completedTask)
    }

    @Test
    fun deleteAllTasks() = runTest {
        val initialTasks = tasksRepository.getTasks()

        // Verify tasks are returned
        assertThat(initialTasks.size).isEqualTo(1)

        // Delete all tasks
        tasksRepository.deleteAllTasks()

        // Verify tasks are empty now
        val afterDeleteTasks = tasksRepository.getTasks()
        assertThat(afterDeleteTasks).isEmpty()
    }

    @Test
    fun deleteSingleTask() = runTest {
        val initialTasksSize = tasksRepository.getTasks(true).size

        // Delete first task
        tasksRepository.deleteTask(task1.id)

        // Fetch data again
        val afterDeleteTasks = tasksRepository.getTasks(true)

        // Verify only one task was deleted
        assertThat(afterDeleteTasks.size).isEqualTo(initialTasksSize - 1)
        assertThat(afterDeleteTasks).doesNotContain(task1)
    }
}
