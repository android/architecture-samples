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

package com.example.android.architecture.blueprints.todoapp.data.source

import com.example.android.architecture.blueprints.todoapp.MainCoroutineRule
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.local.TaskEntity
import com.example.android.architecture.blueprints.todoapp.data.source.local.asExternalModel
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

    private val task1Local = TaskEntity("Title1", "Description1")
    private val task2Local = TaskEntity("Title2", "Description2")
    private val task3Local = TaskEntity("Title3", "Description3")

    private val task1Remote = task1Local.asExternalModel()
    private val task2Remote = task2Local.asExternalModel()
    private val task3Remote = task3Local.asExternalModel()

    private val newTaskRemote = Task("Title new", "Description new")
    private val remoteTasks = listOf(task1Remote, task2Remote).sortedBy { it.id }
    private val localTasks = listOf(task3Local).sortedBy { it.id }
    private val newTasksRemote = listOf(newTaskRemote).sortedBy { it.id }
    private lateinit var tasksRemoteDataSource: FakeNetworkDataSource
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
        tasksRemoteDataSource = FakeNetworkDataSource(remoteTasks.toMutableList())
        tasksLocalDataSource = FakeTasksDao(localTasks.toMutableList())
        // Get a reference to the class under test
        tasksRepository = DefaultTasksRepository(
            tasksRemoteDataSource, tasksLocalDataSource
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
        tasksRemoteDataSource.tasks = newTasksRemote.toMutableList()

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
        assertThat(tasks).isEqualTo(remoteTasks)
    }

    @Test
    fun saveTask_savesToLocalAndRemote() = runTest {
        // Make sure newTask is not in the remote or local datasources
        assertThat(tasksRemoteDataSource.tasks).doesNotContain(newTaskRemote)
        assertThat(tasksLocalDataSource.tasks).doesNotContain(newTaskRemote)

        // When a task is saved to the tasks repository
        tasksRepository.saveTask(newTaskRemote)

        // Then the remote and local sources are called
        assertThat(tasksRemoteDataSource.tasks).contains(newTaskRemote)
        assertThat(tasksLocalDataSource.tasks?.asExternalModels()).contains(newTaskRemote)
    }

    @Test
    fun getTasks_WithDirtyCache_tasksAreRetrievedFromRemote() = runTest {
        // First call returns from REMOTE
        val tasks = tasksRepository.getTasks()

        // Set a different list of tasks in REMOTE
        tasksRemoteDataSource.tasks = newTasksRemote.toMutableList()

        // But if tasks are cached, subsequent calls load from cache
        val cachedTasks = tasksRepository.getTasks()
        assertThat(cachedTasks).isEqualTo(tasks)

        // Now force remote loading
        val refreshedTasks = tasksRepository.getTasks(true)

        // Tasks must be the recently updated in REMOTE
        assertThat(refreshedTasks).isEqualTo(newTasksRemote)
    }

    @Test(expected = Exception::class)
    fun getTasks_WithDirtyCache_remoteUnavailable_throwsException() = runTest {
        // Make remote data source unavailable
        tasksRemoteDataSource.tasks = null

        // Load tasks forcing remote load
        tasksRepository.getTasks(true)

        // Exception should be thrown
    }

    @Test
    fun getTasks_WithRemoteDataSourceUnavailable_tasksAreRetrievedFromLocal() =
        runTest {
            // When the remote data source is unavailable
            tasksRemoteDataSource.tasks = null

            // The repository fetches from the local source
            assertThat(tasksRepository.getTasks()).isEqualTo(localTasks.asExternalModels())
        }

    @Test(expected = Exception::class)
    fun getTasks_WithBothDataSourcesUnavailable_throwsError() = runTest {
        // When both sources are unavailable
        tasksRemoteDataSource.tasks = null
        tasksLocalDataSource.tasks = null

        // The repository throws an error
        tasksRepository.getTasks()
    }

    @Test
    fun getTasks_refreshesLocalDataSource() = runTest {
        val initialLocal = tasksLocalDataSource.tasks

        // Forcing an update will fetch tasks from remote
        val newTasks = tasksRepository.getTasks(true)

        assertThat(newTasks).isEqualTo(remoteTasks)
        assertThat(newTasks).isEqualTo(tasksLocalDataSource.tasks?.asExternalModels())
        assertThat(tasksLocalDataSource.tasks).isEqualTo(initialLocal)
    }

    @Test
    fun completeTask_completesTaskToServiceAPIUpdatesCache() = runTest {
        // Save a task
        tasksRepository.saveTask(newTaskRemote)

        // Make sure it's active
        assertThat(tasksRepository.getTask(newTaskRemote.id)?.isCompleted).isFalse()

        // Mark is as complete
        tasksRepository.completeTask(newTaskRemote.id)

        // Verify it's now completed
        assertThat(tasksRepository.getTask(newTaskRemote.id)?.isCompleted).isTrue()
    }

    @Test
    fun completeTask_activeTaskToServiceAPIUpdatesCache() = runTest {
        // Save a task
        tasksRepository.saveTask(newTaskRemote)
        tasksRepository.completeTask(newTaskRemote.id)

        // Make sure it's completed
        assertThat(tasksRepository.getTask(newTaskRemote.id)?.isActive).isFalse()

        // Mark is as active
        tasksRepository.activateTask(newTaskRemote.id)

        // Verify it's now activated
        assertThat(tasksRepository.getTask(newTaskRemote.id)?.isActive).isTrue()
    }

    @Test
    fun getTask_repositoryCachesAfterFirstApiCall() = runTest {
        // Obtain a task from the local data source
        tasksLocalDataSource.tasks = mutableListOf(task1Local)
        val initial = tasksRepository.getTask(task1Remote.id)

        // Change the tasks on the remote
        tasksRemoteDataSource.tasks = newTasksRemote.toMutableList()

        // Obtain the same task again
        val second = tasksRepository.getTask(task1Remote.id)

        // Initial and second tasks should match because we didn't force a refresh
        assertThat(second).isEqualTo(initial)
    }

    @Test
    fun getTask_forceRefresh() = runTest {
        // Trigger the repository to load data, which loads from remote and caches
        tasksRemoteDataSource.tasks = mutableListOf(task1Remote)
        val task1FirstTime = tasksRepository.getTask(task1Remote.id, forceUpdate = true)
        assertThat(task1FirstTime?.id).isEqualTo(task1Remote.id)

        // Configure the remote data source to return a different task
        tasksRemoteDataSource.tasks = mutableListOf(task2Remote)

        // Force refresh
        val task1SecondTime = tasksRepository.getTask(task1Remote.id, true)
        val task2SecondTime = tasksRepository.getTask(task2Remote.id, true)

        // Only task2 works because task1 does not exist on the remote
        assertThat(task1SecondTime).isNull()
        assertThat(task2SecondTime?.id).isEqualTo(task2Remote.id)
    }

    @Test
    fun clearCompletedTasks() = runTest {
        val completedTask = task1Remote.copy(isCompleted = true)
        tasksRemoteDataSource.tasks = mutableListOf(completedTask, task2Remote)
        tasksRepository.clearCompletedTasks()

        val tasks = tasksRepository.getTasks(true)

        assertThat(tasks).hasSize(1)
        assertThat(tasks).contains(task2Remote)
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
        tasksRepository.deleteTask(task1Remote.id)

        // Fetch data again
        val afterDeleteTasks = tasksRepository.getTasks(true)

        // Verify only one task was deleted
        assertThat(afterDeleteTasks.size).isEqualTo(initialTasksSize - 1)
        assertThat(afterDeleteTasks).doesNotContain(task1Remote)
    }
}
