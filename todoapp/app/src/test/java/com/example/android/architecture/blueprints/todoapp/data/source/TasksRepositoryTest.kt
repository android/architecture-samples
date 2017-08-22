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
package com.example.android.architecture.blueprints.todoapp.data.source

import android.content.Context
import com.example.android.architecture.blueprints.todoapp.any
import com.example.android.architecture.blueprints.todoapp.capture
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.eq
import com.google.common.collect.Lists
import org.hamcrest.CoreMatchers.`is`
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThat
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations

/**
 * Unit tests for the implementation of the in-memory repository with cache.
 */
class TasksRepositoryTest {

    private val TASK_TITLE_1 = "title1"
    private val TASK_TITLE_2 = "title2"
    private val TASK_TITLE_3 = "title3"
    private val TASK_GENERIC_DESCRIPTION = "Some task description"
    private val TASKS = Lists.newArrayList(Task(TASK_TITLE_1, TASK_GENERIC_DESCRIPTION),
            Task(TASK_TITLE_2, TASK_GENERIC_DESCRIPTION))
    private lateinit var tasksRepository: TasksRepository

    @Mock private lateinit var tasksRemoteDataSource: TasksDataSource
    @Mock private lateinit var tasksLocalDataSource: TasksDataSource
    @Mock private lateinit var context: Context
    @Mock private lateinit var getTaskCallback: TasksDataSource.GetTaskCallback
    @Mock private lateinit var loadTasksCallback: TasksDataSource.LoadTasksCallback

    /**
     * [ArgumentCaptor] is a powerful Mockito API to capture argument values and use them to
     * perform further actions or assertions on them.
     */
    @Captor private lateinit var tasksCallbackCaptor: ArgumentCaptor<TasksDataSource.LoadTasksCallback>

    /**
     * [ArgumentCaptor] is a powerful Mockito API to capture argument values and use them to
     * perform further actions or assertions on them.
     */
    @Captor private lateinit var taskCallbackCaptor: ArgumentCaptor<TasksDataSource.GetTaskCallback>

    @Before fun setupTasksRepository() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this)

        // Get a reference to the class under test
        tasksRepository = TasksRepository.getInstance(tasksRemoteDataSource,
                tasksLocalDataSource)
    }

    @After fun destroyRepositoryInstance() {
        TasksRepository.destroyInstance()
    }

    @Test fun getTasks_repositoryCachesAfterFirstApiCall() {
        // Given a setup Captor to capture callbacks
        // When two calls are issued to the tasks repository
        twoTasksLoadCallsToRepository(loadTasksCallback)

        // Then tasks were only requested once from Service API
        verify(tasksRemoteDataSource)
                .getTasks(any<TasksDataSource.LoadTasksCallback>())
    }

    @Test fun getTasks_requestsAllTasksFromLocalDataSource() {
        // When tasks are requested from the tasks repository
        tasksRepository.getTasks(loadTasksCallback)

        // Then tasks are loaded from the local data source
        verify(tasksLocalDataSource)
                .getTasks(any<TasksDataSource.LoadTasksCallback>())
    }

    @Test fun saveTask_savesTaskToServiceAPI() {
        // Given a stub task with title and description
        val newTask = Task(TASK_TITLE_1, TASK_GENERIC_DESCRIPTION)

        // When a task is saved to the tasks repository
        tasksRepository.saveTask(newTask)

        // Then the service API and persistent repository are called and the cache is updated
        verify(tasksRemoteDataSource).saveTask(newTask)
        verify(tasksLocalDataSource).saveTask(newTask)
        assertThat(tasksRepository.cachedTasks.size, `is`(1))
    }

    @Test fun completeTask_completesTaskToServiceAPIUpdatesCache() {
        with(tasksRepository) {
            // Given a stub active task with title and description added in the repository
            val newTask = Task(TASK_TITLE_1, TASK_GENERIC_DESCRIPTION)
            saveTask(newTask)

            // When a task is completed to the tasks repository
            completeTask(newTask)

            // Then the service API and persistent repository are called and the cache is updated
            verify(tasksRemoteDataSource).completeTask(newTask)
            verify(tasksLocalDataSource).completeTask(newTask)
            assertThat(cachedTasks.size, `is`(1))
            val cachedNewTask = cachedTasks[newTask.id]
            assertNotNull(cachedNewTask as Task)
            assertThat(cachedNewTask.isActive, `is`(false))
        }
    }

    @Test fun completeTaskId_completesTaskToServiceAPIUpdatesCache() {
        // Given a stub active task with title and description added in the repository
        val newTask = Task(TASK_TITLE_1, TASK_GENERIC_DESCRIPTION)
        with(tasksRepository) {
            saveTask(newTask)

            // When a task is completed using its id to the tasks repository
            completeTask(newTask.id)

            // Then the service API and persistent repository are called and the cache is updated
            verify(tasksRemoteDataSource).completeTask(newTask)
            verify(tasksLocalDataSource).completeTask(newTask)
            assertThat(cachedTasks.size, `is`(1))
            val cachedNewTask = cachedTasks[newTask.id]
            assertNotNull(cachedNewTask as Task)
            assertThat(cachedNewTask.isActive, `is`(false))
        }
    }

    @Test fun activateTask_activatesTaskToServiceAPIUpdatesCache() {
        // Given a stub completed task with title and description in the repository
        val newTask = Task(TASK_TITLE_1, TASK_GENERIC_DESCRIPTION).apply { isCompleted = true }
        with(tasksRepository) {
            tasksRepository.saveTask(newTask)
            // When a completed task is activated to the tasks repository
            tasksRepository.activateTask(newTask)
            // Then the service API and persistent repository are called and the cache is updated
            verify(tasksRemoteDataSource).activateTask(newTask)
            verify(tasksLocalDataSource).activateTask(newTask)
            assertThat(cachedTasks.size, `is`(1))
            val cachedNewTask = cachedTasks[newTask.id]
            assertNotNull(cachedNewTask as Task)
            assertThat(cachedNewTask.isActive, `is`(true))
        }
    }

    @Test fun activateTaskId_activatesTaskToServiceAPIUpdatesCache() {
        // Given a stub completed task with title and description in the repository
        val newTask = Task(TASK_TITLE_1, TASK_GENERIC_DESCRIPTION).apply { isCompleted = true }
        with(tasksRepository) {
            saveTask(newTask)

            // When a completed task is activated with its id to the tasks repository
            activateTask(newTask.id)

            // Then the service API and persistent repository are called and the cache is updated
            verify(tasksRemoteDataSource).activateTask(newTask)
            verify(tasksLocalDataSource).activateTask(newTask)
            assertThat(cachedTasks.size, `is`(1))
            val cachedNewTask = cachedTasks[newTask.id]
            assertNotNull(cachedNewTask as Task)
            assertThat(cachedNewTask.isActive, `is`(true))
        }
    }

    @Test fun getTask_requestsSingleTaskFromLocalDataSource() {
        // When a task is requested from the tasks repository
        tasksRepository.getTask(TASK_TITLE_1, getTaskCallback)

        // Then the task is loaded from the database
        verify(tasksLocalDataSource).getTask(eq(TASK_TITLE_1), any<TasksDataSource.GetTaskCallback>())
    }

    @Test fun deleteCompletedTasks_deleteCompletedTasksToServiceAPIUpdatesCache() {
        with(tasksRepository) {
            // Given 2 stub completed tasks and 1 stub active tasks in the repository
            val newTask = Task(TASK_TITLE_1, TASK_GENERIC_DESCRIPTION).apply { isCompleted = true }
            saveTask(newTask)
            val newTask2 = Task(TASK_TITLE_2, TASK_GENERIC_DESCRIPTION)
            saveTask(newTask2)
            val newTask3 = Task(TASK_TITLE_3, TASK_GENERIC_DESCRIPTION).apply { isCompleted = true }
            saveTask(newTask3)

            // When a completed tasks are cleared to the tasks repository
            clearCompletedTasks()


            // Then the service API and persistent repository are called and the cache is updated
            verify(tasksRemoteDataSource).clearCompletedTasks()
            verify(tasksLocalDataSource).clearCompletedTasks()

            assertThat(cachedTasks.size, `is`(1))
            val task = cachedTasks[newTask2.id]
            assertNotNull(task as Task)
            assertTrue(task.isActive)
            assertThat(task.title, `is`(TASK_TITLE_2))
        }
    }

    @Test fun deleteAllTasks_deleteTasksToServiceAPIUpdatesCache() {
        with(tasksRepository) {
            // Given 2 stub completed tasks and 1 stub active tasks in the repository
            val newTask = Task(TASK_TITLE_1, TASK_GENERIC_DESCRIPTION).apply { isCompleted = true }
            saveTask(newTask)
            val newTask2 = Task(TASK_TITLE_2, TASK_GENERIC_DESCRIPTION)
            saveTask(newTask2)
            val newTask3 = Task(TASK_TITLE_3, TASK_GENERIC_DESCRIPTION).apply { isCompleted = true }
            saveTask(newTask3)

            // When all tasks are deleted to the tasks repository
            deleteAllTasks()

            // Verify the data sources were called
            verify(tasksRemoteDataSource).deleteAllTasks()
            verify(tasksLocalDataSource).deleteAllTasks()

            assertThat(cachedTasks.size, `is`(0))
        }
    }

    @Test fun deleteTask_deleteTaskToServiceAPIRemovedFromCache() {
        with(tasksRepository) {
            // Given a task in the repository
            val newTask = Task(TASK_TITLE_1, TASK_GENERIC_DESCRIPTION).apply { isCompleted }
            saveTask(newTask)
            assertThat(cachedTasks.containsKey(newTask.id), `is`(true))

            // When deleted
            deleteTask(newTask.id)

            // Verify the data sources were called
            verify(tasksRemoteDataSource).deleteTask(newTask.id)
            verify(tasksLocalDataSource).deleteTask(newTask.id)

            // Verify it's removed from repository
            assertThat(cachedTasks.containsKey(newTask.id), `is`(false))
        }
    }

    @Test fun getTasksWithDirtyCache_tasksAreRetrievedFromRemote() {
        with(tasksRepository) {
            // When calling getTasks in the repository with dirty cache
            refreshTasks()
            getTasks(loadTasksCallback)
        }

        // And the remote data source has data available
        setTasksAvailable(tasksRemoteDataSource, TASKS)

        // Verify the tasks from the remote data source are returned, not the local
        verify(tasksLocalDataSource, never()).getTasks(loadTasksCallback)
        verify(loadTasksCallback).onTasksLoaded(TASKS)
    }

    @Test fun getTasksWithLocalDataSourceUnavailable_tasksAreRetrievedFromRemote() {
        // When calling getTasks in the repository
        tasksRepository.getTasks(loadTasksCallback)

        // And the local data source has no data available
        setTasksNotAvailable(tasksLocalDataSource)

        // And the remote data source has data available
        setTasksAvailable(tasksRemoteDataSource, TASKS)

        // Verify the tasks from the local data source are returned
        verify(loadTasksCallback).onTasksLoaded(TASKS)
    }

    @Test fun getTasksWithBothDataSourcesUnavailable_firesOnDataUnavailable() {
        // When calling getTasks in the repository
        tasksRepository.getTasks(loadTasksCallback)

        // And the local data source has no data available
        setTasksNotAvailable(tasksLocalDataSource)

        // And the remote data source has no data available
        setTasksNotAvailable(tasksRemoteDataSource)

        // Verify no data is returned
        verify(loadTasksCallback).onDataNotAvailable()
    }

    @Test fun getTaskWithBothDataSourcesUnavailable_firesOnDataUnavailable() {
        // Given a task id
        val taskId = "123"

        // When calling getTask in the repository
        tasksRepository.getTask(taskId, getTaskCallback)

        // And the local data source has no data available
        setTaskNotAvailable(tasksLocalDataSource, taskId)

        // And the remote data source has no data available
        setTaskNotAvailable(tasksRemoteDataSource, taskId)

        // Verify no data is returned
        verify(getTaskCallback).onDataNotAvailable()
    }

    @Test fun getTasks_refreshesLocalDataSource() {
        with(tasksRepository) {
            // Mark cache as dirty to force a reload of data from remote data source.
            refreshTasks()

            // When calling getTasks in the repository
            getTasks(loadTasksCallback)
        }

        // Make the remote data source return data
        setTasksAvailable(tasksRemoteDataSource, TASKS)

        // Verify that the data fetched from the remote data source was saved in local.
        verify(tasksLocalDataSource, times(TASKS.size)).saveTask(any<Task>())
    }

    /**
     * Convenience method that issues two calls to the tasks repository
     */
    private fun twoTasksLoadCallsToRepository(callback: TasksDataSource.LoadTasksCallback) {
        // When tasks are requested from repository
        tasksRepository.getTasks(callback) // First call to API

        // Use the Mockito Captor to capture the callback
        verify(tasksLocalDataSource).getTasks(capture(tasksCallbackCaptor))

        // Local data source doesn't have data yet
        tasksCallbackCaptor.value.onDataNotAvailable()


        // Verify the remote data source is queried
        verify(tasksRemoteDataSource).getTasks(capture(tasksCallbackCaptor))

        // Trigger callback so tasks are cached
        tasksCallbackCaptor.value.onTasksLoaded(TASKS)

        tasksRepository.getTasks(callback) // Second call to API
    }

    private fun setTasksNotAvailable(dataSource: TasksDataSource) {
        verify(dataSource).getTasks(capture(tasksCallbackCaptor))
        tasksCallbackCaptor.value.onDataNotAvailable()
    }

    private fun setTasksAvailable(dataSource: TasksDataSource, tasks: List<Task>) {
        verify(dataSource).getTasks(capture(tasksCallbackCaptor))
        tasksCallbackCaptor.value.onTasksLoaded(tasks)
    }

    private fun setTaskNotAvailable(dataSource: TasksDataSource, taskId: String) {
        verify(dataSource).getTask(eq(taskId),
                capture(taskCallbackCaptor))
        taskCallbackCaptor.value.onDataNotAvailable()
    }

    private fun setTaskAvailable(dataSource: TasksDataSource, task: Task) {
        verify(dataSource).getTask(eq(task.id),
                capture(taskCallbackCaptor))
        taskCallbackCaptor.value.onTaskLoaded(task)
    }
}
