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

import com.example.android.architecture.blueprints.todoapp.data.Result
import com.example.android.architecture.blueprints.todoapp.data.Result.Success
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for the implementation of the in-memory repository with cache.
 */
@ExperimentalCoroutinesApi
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
    fun getTasks_emptyRepositoryAndUninitializedCache() = runBlockingTest {
        val emptySource = FakeDataSource()
        val tasksRepository = DefaultTasksRepository(
            emptySource, emptySource, Dispatchers.Unconfined
        )

        assertThat(tasksRepository.getTasks() is Success).isTrue()
    }

    @Test
    fun getTasks_repositoryCachesAfterFirstApiCall() = runBlockingTest {
        // Trigger the repository to load data, which loads from remote and caches
        val initial = tasksRepository.getTasks()

        tasksRemoteDataSource.tasks = newTasks.toMutableList()

        val second = tasksRepository.getTasks()

        // Initial and second should match because we didn't force a refresh
        assertThat(second).isEqualTo(initial)
    }

    @Test
    fun getTasks_requestsAllTasksFromRemoteDataSource() = runBlockingTest {
        // When tasks are requested from the tasks repository
        val tasks = tasksRepository.getTasks(true) as Success

        // Then tasks are loaded from the remote data source
        assertThat(tasks.data).isEqualTo(remoteTasks)
    }

    @Test
    fun saveTask_savesToLocalAndRemote() = runBlockingTest {
        // Make sure newTask is not in the remote or local datasources
        assertThat(tasksRemoteDataSource.tasks).doesNotContain(newTask)
        assertThat(tasksLocalDataSource.tasks).doesNotContain(newTask)

        // When a task is saved to the tasks repository
        tasksRepository.saveTask(newTask)

        // Then the remote and local sources are called
        assertThat(tasksRemoteDataSource.tasks).contains(newTask)
        assertThat(tasksLocalDataSource.tasks).contains(newTask)
    }

    @Test
    fun getTasks_WithDirtyCache_tasksAreRetrievedFromRemote() = runBlockingTest {
        // First call returns from REMOTE
        val tasks = tasksRepository.getTasks()

        // Set a different list of tasks in REMOTE
        tasksRemoteDataSource.tasks = newTasks.toMutableList()

        // But if tasks are cached, subsequent calls load from cache
        val cachedTasks = tasksRepository.getTasks()
        assertThat(cachedTasks).isEqualTo(tasks)

        // Now force remote loading
        val refreshedTasks = tasksRepository.getTasks(true) as Success

        // Tasks must be the recently updated in REMOTE
        assertThat(refreshedTasks.data).isEqualTo(newTasks)
    }

    @Test
    fun getTasks_WithDirtyCache_remoteUnavailable_error() = runBlockingTest {
        // Make remote data source unavailable
        tasksRemoteDataSource.tasks = null

        // Load tasks forcing remote load
        val refreshedTasks = tasksRepository.getTasks(true)

        // Result should be an error
        assertThat(refreshedTasks).isInstanceOf(Result.Error::class.java)
    }

    @Test
    fun getTasks_WithRemoteDataSourceUnavailable_tasksAreRetrievedFromLocal() = runBlockingTest {
        // When the remote data source is unavailable
        tasksRemoteDataSource.tasks = null

        // The repository fetches from the local source
        assertThat((tasksRepository.getTasks() as Success).data).isEqualTo(localTasks)
    }

    @Test
    fun getTasks_WithBothDataSourcesUnavailable_returnsError() = runBlockingTest {
        // When both sources are unavailable
        tasksRemoteDataSource.tasks = null
        tasksLocalDataSource.tasks = null

        // The repository returns an error
        assertThat(tasksRepository.getTasks()).isInstanceOf(Result.Error::class.java)
    }

    @Test
    fun getTasks_refreshesLocalDataSource() = runBlockingTest {
        val initialLocal = tasksLocalDataSource.tasks

        // First load will fetch from remote
        val newTasks = (tasksRepository.getTasks(true) as Success).data

        assertThat(newTasks).isEqualTo(remoteTasks)
        assertThat(newTasks).isEqualTo(tasksLocalDataSource.tasks)
        assertThat(tasksLocalDataSource.tasks).isEqualTo(initialLocal)
    }

    @Test
    fun completeTask_completesTaskToServiceAPIUpdatesCache() = runBlockingTest {
        // Save a task
        tasksRepository.saveTask(newTask)

        // Make sure it's active
        assertThat((tasksRepository.getTask(newTask.id) as Success).data.isCompleted).isFalse()

        // Mark is as complete
        tasksRepository.completeTask(newTask.id)

        // Verify it's now completed
        assertThat((tasksRepository.getTask(newTask.id) as Success).data.isCompleted).isTrue()
    }

    @Test
    fun completeTask_activeTaskToServiceAPIUpdatesCache() = runBlockingTest {
        // Save a task
        tasksRepository.saveTask(newTask)
        tasksRepository.completeTask(newTask.id)

        // Make sure it's completed
        assertThat((tasksRepository.getTask(newTask.id) as Success).data.isActive).isFalse()

        // Mark is as active
        tasksRepository.activateTask(newTask.id)

        // Verify it's now activated
        val result = tasksRepository.getTask(newTask.id) as Success
        assertThat(result.data.isActive).isTrue()
    }

    @Test
    fun getTask_repositoryCachesAfterFirstApiCall() = runBlockingTest {
        // Trigger the repository to load data, which loads from remote
        tasksRemoteDataSource.tasks = mutableListOf(task1)
        val initial = tasksRepository.getTask(task1.id, true)

        // Configure the remote data source to store a different task
        tasksRemoteDataSource.tasks = mutableListOf(task2)

        val task1SecondTime = tasksRepository.getTask(task1.id, true) as Success
        val task2SecondTime = tasksRepository.getTask(task2.id, true) as Success

        // Both work because one is in remote and the other in cache
        assertThat(task1SecondTime.data.id).isEqualTo(task1.id)
        assertThat(task2SecondTime.data.id).isEqualTo(task2.id)
    }

    @Test
    fun getTask_forceRefresh() = runBlockingTest {
        // Trigger the repository to load data, which loads from remote and caches
        tasksRemoteDataSource.tasks = mutableListOf(task1)
        val initial = tasksRepository.getTask(task1.id)

        // Configure the remote data source to return a different task
        tasksRemoteDataSource.tasks = mutableListOf(task2)

        // Force refresh
        val task1SecondTime = tasksRepository.getTask(task1.id, true)
        val task2SecondTime = tasksRepository.getTask(task2.id, true)

        // Only task2 works because the cache and local were invalidated
        assertThat((task1SecondTime as? Success)?.data?.id).isNull()
        assertThat((task2SecondTime as? Success)?.data?.id).isEqualTo(task2.id)
    }

    @Test
    fun clearCompletedTasks() = runBlockingTest {
        val completedTask = task1.copy().apply { isCompleted = true }
        tasksRemoteDataSource.tasks = mutableListOf(completedTask, task2)
        tasksRepository.clearCompletedTasks()

        val tasks = (tasksRepository.getTasks(true) as? Success)?.data

        assertThat(tasks).hasSize(1)
        assertThat(tasks).contains(task2)
        assertThat(tasks).doesNotContain(completedTask)
    }

    @Test
    fun deleteAllTasks() = runBlockingTest {
        val initialTasks = (tasksRepository.getTasks() as? Success)?.data

        // Delete all tasks
        tasksRepository.deleteAllTasks()

        // Fetch data again
        val afterDeleteTasks = (tasksRepository.getTasks() as? Success)?.data

        // Verify tasks are empty now
        assertThat(initialTasks).isNotEmpty()
        assertThat(afterDeleteTasks).isEmpty()
    }

    @Test
    fun deleteSingleTask() = runBlockingTest {
        val initialTasks = (tasksRepository.getTasks(true) as? Success)?.data

        // Delete first task
        tasksRepository.deleteTask(task1.id)

        // Fetch data again
        val afterDeleteTasks = (tasksRepository.getTasks(true) as? Success)?.data

        // Verify only one task was deleted
        assertThat(afterDeleteTasks?.size).isEqualTo(initialTasks!!.size - 1)
        assertThat(afterDeleteTasks).doesNotContain(task1)
    }
}

