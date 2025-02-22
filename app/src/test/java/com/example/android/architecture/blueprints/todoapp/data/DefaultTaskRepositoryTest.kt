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

import com.example.android.architecture.blueprints.todoapp.data.source.local.FakeTaskDao
import com.example.android.architecture.blueprints.todoapp.data.source.network.FakeNetworkDataSource
import com.google.common.truth.Truth.assertThat
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for the implementation of the in-memory repository with cache.
 */
@ExperimentalCoroutinesApi
class DefaultTaskRepositoryTest {

    private val task1 = Task(id = "1", title = "Title1", description = "Description1")
    private val task2 = Task(id = "2", title = "Title2", description = "Description2")
    private val task3 = Task(id = "3", title = "Title3", description = "Description3")

    private val newTaskTitle = "Title new"
    private val newTaskDescription = "Description new"
    private val newTask = Task(id = "new", title = newTaskTitle, description = newTaskDescription)
    private val newTasks = listOf(newTask)

    private val networkTasks = listOf(task1, task2).toNetwork()
    private val localTasks = listOf(task3.toLocal())

    // Test dependencies
    private lateinit var networkDataSource: FakeNetworkDataSource
    private lateinit var localDataSource: FakeTaskDao

    private var testDispatcher = UnconfinedTestDispatcher()
    private var testScope = TestScope(testDispatcher)

    // Class under test
    private lateinit var taskRepository: DefaultTaskRepository

    @ExperimentalCoroutinesApi
    @Before
    fun createRepository() {
        networkDataSource = FakeNetworkDataSource(networkTasks.toMutableList())
        localDataSource = FakeTaskDao(localTasks)
        // Get a reference to the class under test
        taskRepository = DefaultTaskRepository(
            networkDataSource = networkDataSource,
            localDataSource = localDataSource,
            dispatcher = testDispatcher,
            scope = testScope
        )
    }

    @ExperimentalCoroutinesApi
    @Test
    fun whenGettingAllTasks_emptyRepositoryAndUninitializedCache() = testScope.runTest {
        networkDataSource.tasks?.clear()
        localDataSource.deleteAll()

        assertThat(taskRepository.getAll().size).isEqualTo(0)
    }

    @Test
    fun whenGettingAllTasks_repositoryCachesAfterFirstApiCall() = testScope.runTest {
        // Trigger the repository to load tasks from the remote data source
        val initial = taskRepository.getAll(forceUpdate = true)

        // Change the remote data source
        networkDataSource.tasks = newTasks.toNetwork().toMutableList()

        // Load the tasks again without forcing a refresh
        val second = taskRepository.getAll()

        // Initial and second should match because we didn't force a refresh (no tasks were loaded
        // from the remote data source)
        assertThat(second).isEqualTo(initial)
    }

    @Test
    fun whenGettingAllTasks_requestsAllTasksFromRemoteDataSource() = testScope.runTest {
        // When tasks are requested from the tasks repository
        val tasks = taskRepository.getAll(true)

        // Then tasks are loaded from the remote data source
        assertThat(tasks).isEqualTo(networkTasks.toExternal())
    }

    @Test
    fun saveTask_savesToLocalAndRemote() = testScope.runTest {
        // When a task is saved to the tasks repository
        val newTaskId = taskRepository.create(newTask.title, newTask.description)

        // Then the remote and local sources contain the new task
        assertThat(networkDataSource.tasks?.map { it.id }?.contains(newTaskId))
        assertThat(localDataSource.tasks?.map { it.id }?.contains(newTaskId))
    }

    @Test
    fun whenGettingAllTasks_WithDirtyCache_tasksAreRetrievedFromRemote() = testScope.runTest {
        // First call returns from REMOTE
        val tasks = taskRepository.getAll()

        // Set a different list of tasks in REMOTE
        networkDataSource.tasks = newTasks.toNetwork().toMutableList()

        // But if tasks are cached, subsequent calls load from cache
        val cachedTasks = taskRepository.getAll()
        assertThat(cachedTasks).isEqualTo(tasks)

        // Now force remote loading
        val refreshedTasks = taskRepository.getAll(true)

        // Tasks must be the recently updated in REMOTE
        assertThat(refreshedTasks).isEqualTo(newTasks)
    }

    @Test(expected = Exception::class)
    fun whenGettingAllTasks_WithDirtyCache_remoteUnavailable_throwsException() = testScope.runTest {
        // Make remote data source unavailable
        networkDataSource.tasks = null

        // Load tasks forcing remote load
        taskRepository.getAll(true)

        // Exception should be thrown
    }

    @Test
    fun whenGettingAllTasks_WithRemoteDataSourceUnavailable_tasksAreRetrievedFromLocal() =
        testScope.runTest {
            // When the remote data source is unavailable
            networkDataSource.tasks = null

            // The repository fetches from the local source
            assertThat(taskRepository.getAll()).isEqualTo(localTasks.toExternal())
        }

    @Test(expected = Exception::class)
    fun whenGettingAllTasks_WithBothDataSourcesUnavailable_throwsError() = testScope.runTest {
        // When both sources are unavailable
        networkDataSource.tasks = null
        localDataSource.tasks = null

        // The repository throws an error
        taskRepository.getAll()
    }

    @Test
    fun whenGettingAllTasks_refreshesLocalDataSource() = testScope.runTest {
        // Forcing an update will fetch tasks from remote
        val expectedTasks = networkTasks.toExternal()

        val newTasks = taskRepository.getAll(true)

        assertEquals(expectedTasks, newTasks)
        assertEquals(expectedTasks, localDataSource.tasks?.toExternal())
    }

    @Test
    fun completeTask_completesTaskToServiceAPIUpdatesCache() = testScope.runTest {
        // Save a task
        val newTaskId = taskRepository.create(newTask.title, newTask.description)

        // Make sure it's active
        assertThat(taskRepository.get(newTaskId)?.isCompleted).isFalse()

        // Mark is as complete
        taskRepository.complete(newTaskId)

        // Verify it's now completed
        assertThat(taskRepository.get(newTaskId)?.isCompleted).isTrue()
    }

    @Test
    fun completeTask_activeTaskToServiceAPIUpdatesCache() = testScope.runTest {
        // Save a task
        val newTaskId = taskRepository.create(newTask.title, newTask.description)
        taskRepository.complete(newTaskId)

        // Make sure it's completed
        assertThat(taskRepository.get(newTaskId)?.isActive).isFalse()

        // Mark is as active
        taskRepository.activate(newTaskId)

        // Verify it's now activated
        assertThat(taskRepository.get(newTaskId)?.isActive).isTrue()
    }

    @Test
    fun whenGettingATask_repositoryCachesAfterFirstApiCall() = testScope.runTest {
        // Obtain a task from the local data source
        localDataSource = FakeTaskDao(mutableListOf(task1.toLocal()))
        val initial = taskRepository.get(task1.id)

        // Change the tasks on the remote
        networkDataSource.tasks = newTasks.toNetwork().toMutableList()

        // Obtain the same task again
        val second = taskRepository.get(task1.id)

        // Initial and second tasks should match because we didn't force a refresh
        assertThat(second).isEqualTo(initial)
    }

    @Test
    fun whenGettingATask_aRefreshOccurs() = testScope.runTest {
        // Trigger the repository to load data, which loads from remote and caches
        networkDataSource.tasks = mutableListOf(task1.toNetwork())
        val task1FirstTime = taskRepository.get(task1.id, forceUpdate = true)
        assertThat(task1FirstTime?.id).isEqualTo(task1.id)

        // Configure the remote data source to return a different task
        networkDataSource.tasks = mutableListOf(task2.toNetwork())

        // Force refresh
        val task1SecondTime = taskRepository.get(task1.id, true)
        val task2SecondTime = taskRepository.get(task2.id, true)

        // Only task2 works because task1 does not exist on the remote
        assertThat(task1SecondTime).isNull()
        assertThat(task2SecondTime?.id).isEqualTo(task2.id)
    }

    @Test
    fun clearCompletedTasks() = testScope.runTest {
        val completedTask = task1.copy(isCompleted = true)
        localDataSource.tasks = listOf(completedTask.toLocal(), task2.toLocal())
        taskRepository.clearAllCompleted()

        val tasks = taskRepository.getAll(true)

        assertThat(tasks).hasSize(1)
        assertThat(tasks).contains(task2)
        assertThat(tasks).doesNotContain(completedTask)
    }

    @Test
    fun deleteAllTasks() = testScope.runTest {
        val initialTasks = taskRepository.getAll()

        // Verify tasks are returned
        assertThat(initialTasks.size).isEqualTo(1)

        // Delete all tasks
        taskRepository.deleteAll()

        // Verify tasks are empty now
        val afterDeleteTasks = taskRepository.getAll()
        assertThat(afterDeleteTasks).isEmpty()
    }

    @Test
    fun deleteSingleTask() = testScope.runTest {
        val initialTasksSize = taskRepository.getAll(true).size

        // Delete first task
        taskRepository.delete(task1.id)

        // Fetch data again
        val afterDeleteTasks = taskRepository.getAll(true)

        // Verify only one task was deleted
        assertThat(afterDeleteTasks.size).isEqualTo(initialTasksSize - 1)
        assertThat(afterDeleteTasks).doesNotContain(task1)
    }
}
