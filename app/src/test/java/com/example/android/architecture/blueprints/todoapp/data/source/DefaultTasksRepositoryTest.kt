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
package com.example.android.architecture.blueprints.todoapp.data.source

import com.example.android.architecture.blueprints.todoapp.assertNonNullEquals
import com.example.android.architecture.blueprints.todoapp.assertNonNullFalse
import com.example.android.architecture.blueprints.todoapp.assertNonNullTrue
import com.example.android.architecture.blueprints.todoapp.data.Result
import com.example.android.architecture.blueprints.todoapp.data.Result.Success
import com.example.android.architecture.blueprints.todoapp.data.Task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for the implementation of the in-memory repository with cache.
 */
class DefaultTasksRepositoryTest {

    private val task1 = Task("Title1", "Description1")
    private val task2 = Task("Title2", "Description2")
    private val task3 = Task("Title3", "Description3")
    private val newTask = Task("Title new", "Description new")
    private val remoteTasks = listOf(task1, task2).sortedBy { it.id }
    private val localTasks = listOf(task3).sortedBy { it.id }
    private val newTasks = listOf(task3).sortedBy { it.id }
    private lateinit var tasksRemoteDataSource: FakeDataSource
    private lateinit var tasksLocalDataSource: FakeDataSource

    // Class under test
    private lateinit var tasksRepository: DefaultTasksRepository

    @ExperimentalCoroutinesApi
    @Before
    fun createRepository() {
        tasksRemoteDataSource = FakeDataSource(remoteTasks.toMutableList())
        tasksLocalDataSource = FakeDataSource(localTasks.toMutableList())
        // Get a reference to the class under test
        tasksRepository = DefaultTasksRepository(
            tasksRemoteDataSource, tasksLocalDataSource, Dispatchers.Unconfined
        )
    }

    @ExperimentalCoroutinesApi
    @Test
    fun getTasks_emptyRepositoryAndUninitializedCache() = runBlocking {
        val emptySource = FakeDataSource()
        val tasksRepository = DefaultTasksRepository(
            emptySource, emptySource, Dispatchers.Unconfined
        )

        assertTrue(tasksRepository.getTasks() is Success)
    }

    @Test
    fun getTasks_repositoryCachesAfterFirstApiCall() = runBlocking {
        // Trigger the repository to load data, which loads from remote and caches
        val initial = tasksRepository.getTasks()

        tasksRemoteDataSource.tasks = newTasks.toMutableList()

        val second = tasksRepository.getTasks()

        // Initial and second should match because we didn't force a refresh
        assertEquals(initial, second)
    }

    @Test
    fun getTasks_requestsAllTasksFromRemoteDataSource() = runBlocking {
        // When tasks are requested from the tasks repository
        val tasks = tasksRepository.getTasks()

        // Then tasks are loaded from the remote data source
        assertEquals(remoteTasks, (tasks as? Success)?.data)
    }

    @Test
    fun saveTask_savesToCacheLocalAndRemote() = runBlocking {
        // Make sure newTask is not in the remote or local datasources or cache
        assertNonNullFalse(tasksRemoteDataSource.tasks?.contains(newTask))
        assertNonNullFalse(tasksLocalDataSource.tasks?.contains(newTask))
        assertNonNullFalse((tasksRepository.getTasks() as? Success)?.data?.contains(newTask))

        // When a task is saved to the tasks repository
        tasksRepository.saveTask(newTask)

        // Then the remote and local sources are called and the cache is updated
        assertNonNullTrue(tasksRemoteDataSource.tasks?.contains(newTask))
        assertNonNullTrue(tasksLocalDataSource.tasks?.contains(newTask))
        assertNonNullTrue((tasksRepository.getTasks() as? Success)?.data?.contains(newTask))
    }

    @Test
    fun getTasks_WithDirtyCache_tasksAreRetrievedFromRemote() = runBlocking {

        // First call returns from REMOTE
        val tasks = tasksRepository.getTasks()

        // Set a different list of tasks in REMOTE
        tasksRemoteDataSource.tasks = newTasks.toMutableList()

        // But if tasks are cached, subsequent calls load from cache
        val cachedTasks = tasksRepository.getTasks()
        assertEquals(tasks, cachedTasks)

        // Now force remote loading
        val refreshedTasks = tasksRepository.getTasks(true)

        // Tasks must be the recently updated in REMOTE
        assertEquals(newTasks, (refreshedTasks as Success).data)
    }

    @Test
    fun getTasks_WithDirtyCache_remoteUnavailable_error() = runBlocking {
        // Make remote data source unavailable
        tasksRemoteDataSource.tasks = null

        // Load tasks forcing remote load
        val refreshedTasks = tasksRepository.getTasks(true)

        // Result should be an error
        assertTrue(refreshedTasks is Result.Error)
    }

    @Test
    fun getTasks_WithRemoteDataSourceUnavailable_tasksAreRetrievedFromLocal() = runBlocking {
        // When the remote data source is unavailable
        tasksRemoteDataSource.tasks = null

        // The repository fetches from the local source
        assertEquals(localTasks, (tasksRepository.getTasks() as Success).data)
    }

    @Test
    fun getTasks_WithBothDataSourcesUnavailable_returnsError() = runBlocking {
        // When both sources are unavailable
        tasksRemoteDataSource.tasks = null
        tasksLocalDataSource.tasks = null

        // The repository returns an error
        assertTrue(tasksRepository.getTasks() is Result.Error)
    }

    @Test
    fun getTasks_refreshesLocalDataSource() = runBlocking {
        val initialLocal = tasksLocalDataSource.tasks

        // First load will fetch from remote
        val newTasks = (tasksRepository.getTasks() as Success).data

        assertEquals(remoteTasks, newTasks)
        assertEquals(tasksLocalDataSource.tasks, newTasks)
        assertNotEquals(tasksLocalDataSource.tasks, initialLocal)
    }

    @Test
    fun saveTask_savesTaskToRemoteAndUpdatesCache() = runBlocking {
        // Save a task
        tasksRepository.saveTask(newTask)

        // Verify it's in all the data sources
        assertNonNullTrue(tasksLocalDataSource.tasks?.contains(newTask))
        assertNonNullTrue(tasksRemoteDataSource.tasks?.contains(newTask))

        // Verify it's in the cache
        tasksLocalDataSource.deleteAllTasks() // Make sure they don't come from local
        tasksRemoteDataSource.deleteAllTasks() // Make sure they don't come from remote
        assertNonNullTrue((tasksRepository.getTasks() as Success).data.contains(newTask))
    }

    @Test
    fun completeTask_completesTaskToServiceAPIUpdatesCache() = runBlocking {
        // Save a task
        tasksRepository.saveTask(newTask)

        // Make sure it's active
        assertNonNullFalse((tasksRepository.getTask(newTask.id) as Success).data.isCompleted)

        // Mark is as complete
        tasksRepository.completeTask(newTask.id)

        // Verify it's now completed
        assertNonNullTrue((tasksRepository.getTask(newTask.id) as Success).data.isCompleted)
    }

    @Test
    fun completeTask_activeTaskToServiceAPIUpdatesCache() = runBlocking {
        // Save a task
        tasksRepository.saveTask(newTask)
        tasksRepository.completeTask(newTask.id)

        // Make sure it's completed
        assertNonNullFalse((tasksRepository.getTask(newTask.id) as Success).data.isActive)

        // Mark is as active
        tasksRepository.activateTask(newTask.id)

        // Verify it's now activated
        assertNonNullTrue((tasksRepository.getTask(newTask.id) as Success).data.isActive)
    }

    @Test
    fun getTask_repositoryCachesAfterFirstApiCall() = runBlocking {
        // Trigger the repository to load data, which loads from remote
        tasksRemoteDataSource.tasks = mutableListOf(task1)
        val initial = tasksRepository.getTask(task1.id)

        // Configure the remote data source to store a different task
        tasksRemoteDataSource.tasks = mutableListOf(task2)

        val task1SecondTime = tasksRepository.getTask(task1.id)
        val task2SecondTime = tasksRepository.getTask(task2.id)

        // Both work because one is in remote and the other in cache
        assertNonNullTrue((task1SecondTime as? Success)?.data?.id == task1.id)
        assertNonNullTrue((task2SecondTime as? Success)?.data?.id == task2.id)
    }

    @Test
    fun getTask_forceRefresh() = runBlocking {
        // Trigger the repository to load data, which loads from remote and caches
        tasksRemoteDataSource.tasks = mutableListOf(task1)
        val initial = tasksRepository.getTask(task1.id)

        // Configure the remote data source to return a different task
        tasksRemoteDataSource.tasks = mutableListOf(task2)

        // Force refresh
        val task1SecondTime = tasksRepository.getTask(task1.id, true)
        val task2SecondTime = tasksRepository.getTask(task2.id, true)

        // Only task2 works because the cache and local were invalidated
        assertNonNullFalse((task1SecondTime as? Success)?.data?.id == task1.id)
        assertNonNullTrue((task2SecondTime as? Success)?.data?.id == task2.id)
    }

    // TODO: Add getTask* to cover the same cases as getTasks*

    @Test
    fun clearCompletedTasks() = runBlocking {
        val completedTask = task1.copy().apply { isCompleted = true }
        tasksRemoteDataSource.tasks = mutableListOf(completedTask, task2)
        tasksRepository.clearCompletedTasks()

        val tasks = (tasksRepository.getTasks() as? Success)?.data

        assertNonNullTrue(tasks?.size == 1)
        assertNonNullTrue(tasks?.contains(task2))
        assertNonNullFalse(tasks?.contains(completedTask))
    }

    @Test
    fun deleteAllTasks() = runBlocking {
        val initialTasks = (tasksRepository.getTasks() as? Success)?.data

        // Delete all tasks
        tasksRepository.deleteAllTasks()

        // Fetch data again
        val afterDeleteTasks = (tasksRepository.getTasks() as? Success)?.data

        // Verify tasks are empty now
        assertNonNullFalse(initialTasks?.isEmpty())
        assertNonNullTrue(afterDeleteTasks?.isEmpty())
    }

    @Test
    fun deleteSingleTask() = runBlocking {
        val initialTasks = (tasksRepository.getTasks() as? Success)?.data

        // Delete first task
        tasksRepository.deleteTask(task1.id)

        // Fetch data again
        val afterDeleteTasks = (tasksRepository.getTasks() as? Success)?.data

        // Verify only one task was deleted
        assertNonNullEquals(afterDeleteTasks?.size, initialTasks!!.size - 1)
        assertNonNullFalse(afterDeleteTasks?.contains(task1))
    }
}

