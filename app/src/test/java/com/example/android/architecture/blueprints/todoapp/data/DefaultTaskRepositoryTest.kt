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
import com.example.android.architecture.blueprints.todoapp.data.source.local.FakeTaskDao
import com.example.android.architecture.blueprints.todoapp.data.source.network.FakeNetworkDataSource
import com.google.common.truth.Truth.assertThat
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for the implementation of the in-memory repository with cache.
 */
@ExperimentalCoroutinesApi
class DefaultTaskRepositoryTest {

    private val task1 = Task(title = "Title1", description = "Description1")
    private val task2 = Task(title = "Title2", description = "Description2")
    private val task3 = Task(title = "Title3", description = "Description3")
    private val newTask = Task(title = "Title new", description = "Description new")
    private val networkTasks = listOf(task1, task2).toNetwork().sortedBy { it.id }
    private val localTasks = listOf(task3.toLocal()).sortedBy { it.id }

    private val newTasks = listOf(newTask).sortedBy { it.id }
    private lateinit var networkDataSource: FakeNetworkDataSource
    private lateinit var localDataSource: FakeTaskDao

    // Class under test
    private lateinit var tasksRepository: DefaultTaskRepository

    // Set the main coroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @ExperimentalCoroutinesApi
    @Before
    fun createRepository() {
        networkDataSource = FakeNetworkDataSource(networkTasks.toMutableList())
        localDataSource = FakeTaskDao(localTasks)
        // Get a reference to the class under test
        tasksRepository = DefaultTaskRepository(
            networkDataSource, localDataSource
        )
    }

    @ExperimentalCoroutinesApi
    @Test
    fun getTasks_emptyRepositoryAndUninitializedCache() = runTest {
        val emptyRemoteSource = FakeNetworkDataSource()
        val emptyLocalSource = FakeTaskDao()

        val tasksRepository = DefaultTaskRepository(
            emptyRemoteSource, emptyLocalSource
        )

        assertThat(tasksRepository.getTasks().size).isEqualTo(0)
    }

    @Test
    fun getTasks_repositoryCachesAfterFirstApiCall() = runTest {
        // Trigger the repository to load tasks from the remote data source
        val initial = tasksRepository.getTasks(forceUpdate = true)

        // Change the remote data source
        networkDataSource.tasks = newTasks.toNetwork().toMutableList()

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
        assertThat(tasks).isEqualTo(networkTasks.toExternal())
    }

    @Test
    fun saveTask_savesToLocalAndRemote() = runTest {
        // Make sure newTask is not in the remote or local datasources
        assertThat(networkDataSource.tasks).doesNotContain(newTask.toNetwork())
        assertThat(localDataSource.tasks).doesNotContain(newTask.toLocal())

        // When a task is saved to the tasks repository
        val newTask = tasksRepository.createTask(newTask.title, newTask.description)

        // Then the remote and local sources are called
        assertThat(networkDataSource.tasks).contains(newTask.toNetwork())
        assertThat(localDataSource.tasks?.contains(newTask.toLocal()))
    }

    @Test
    fun getTasks_WithDirtyCache_tasksAreRetrievedFromRemote() = runTest {
        // First call returns from REMOTE
        val tasks = tasksRepository.getTasks()

        // Set a different list of tasks in REMOTE
        networkDataSource.tasks = newTasks.toNetwork().toMutableList()

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
        networkDataSource.tasks = null

        // Load tasks forcing remote load
        tasksRepository.getTasks(true)

        // Exception should be thrown
    }

    @Test
    fun getTasks_WithRemoteDataSourceUnavailable_tasksAreRetrievedFromLocal() =
        runTest {
            // When the remote data source is unavailable
            networkDataSource.tasks = null

            // The repository fetches from the local source
            assertThat(tasksRepository.getTasks()).isEqualTo(localTasks.toExternal())
        }

    @Test(expected = Exception::class)
    fun getTasks_WithBothDataSourcesUnavailable_throwsError() = runTest {
        // When both sources are unavailable
        networkDataSource.tasks = null
        localDataSource.tasks = null

        // The repository throws an error
        tasksRepository.getTasks()
    }

    @Test
    fun getTasks_refreshesLocalDataSource() = runTest {
        // Forcing an update will fetch tasks from remote
        val expectedTasks = networkTasks.toExternal()

        val newTasks = tasksRepository.getTasks(true)

        assertEquals(expectedTasks, newTasks)
        assertEquals(expectedTasks, localDataSource.tasks?.toExternal())
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
        localDataSource = FakeTaskDao(mutableListOf(task1.toLocal()))
        val initial = tasksRepository.getTask(task1.id)

        // Change the tasks on the remote
        networkDataSource.tasks = newTasks.toNetwork().toMutableList()

        // Obtain the same task again
        val second = tasksRepository.getTask(task1.id)

        // Initial and second tasks should match because we didn't force a refresh
        assertThat(second).isEqualTo(initial)
    }

    @Test
    fun getTask_forceRefresh() = runTest {
        // Trigger the repository to load data, which loads from remote and caches
        networkDataSource.tasks = mutableListOf(task1.toNetwork())
        val task1FirstTime = tasksRepository.getTask(task1.id, forceUpdate = true)
        assertThat(task1FirstTime?.id).isEqualTo(task1.id)

        // Configure the remote data source to return a different task
        networkDataSource.tasks = mutableListOf(task2.toNetwork())

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
        localDataSource.tasks = listOf(completedTask.toLocal(), task2.toLocal())
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
