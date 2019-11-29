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
package com.example.android.architecture.blueprints.todoapp.data.source.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.example.android.architecture.blueprints.todoapp.MainCoroutineRule
import com.example.android.architecture.blueprints.todoapp.data.Result.Success
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource
import com.example.android.architecture.blueprints.todoapp.data.succeeded
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.junit.After
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration test for the [TasksDataSource].
 */
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@MediumTest
class TasksLocalDataSourceTest {

    private lateinit var localDataSource: TasksLocalDataSource
    private lateinit var database: ToDoDatabase

    // Set the main coroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        // using an in-memory database for testing, since it doesn't survive killing the process
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            ToDoDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        localDataSource = TasksLocalDataSource(database.taskDao(), Dispatchers.Main)
    }

    @After
    fun cleanUp() {
        database.close()
    }

    @Test
    fun saveTask_retrievesTask() = runBlockingTest {
        // GIVEN - a new task saved in the database
        val newTask = Task("title", "description", true)
        localDataSource.saveTask(newTask)

        // WHEN  - Task retrieved by ID
        val result = localDataSource.getTask(newTask.id)

        // THEN - Same task is returned
        assertThat(result.succeeded, `is`(true))
        result as Success
        assertThat(result.data.title, `is`("title"))
        assertThat(result.data.description, `is`("description"))
        assertThat(result.data.isCompleted, `is`(true))
    }

    @Test
    fun completeTask_retrievedTaskIsComplete() = runBlockingTest {
        // Given a new task in the persistent repository
        val newTask = Task("title")
        localDataSource.saveTask(newTask)

        // When completed in the persistent repository
        localDataSource.completeTask(newTask)
        val result = localDataSource.getTask(newTask.id)

        // Then the task can be retrieved from the persistent repository and is complete
        assertThat(result.succeeded, `is`(true))
        result as Success
        assertThat(result.data.title, `is`(newTask.title))
        assertThat(result.data.isCompleted, `is`(true))
    }

    @Test
    fun activateTask_retrievedTaskIsActive() = runBlockingTest {
        // Given a new completed task in the persistent repository
        val newTask = Task("Some title", "Some description", true)
        localDataSource.saveTask(newTask)

        localDataSource.activateTask(newTask)

        // Then the task can be retrieved from the persistent repository and is active
        val result = localDataSource.getTask(newTask.id)

        assertThat(result.succeeded, `is`(true))
        result as Success

        assertThat(result.data.title, `is`("Some title"))
        assertThat(result.data.isCompleted, `is`(false))
    }

    @Test
    fun clearCompletedTask_taskNotRetrievable() = runBlockingTest {
        // Given 2 new completed tasks and 1 active task in the persistent repository
        val newTask1 = Task("title")
        val newTask2 = Task("title2")
        val newTask3 = Task("title3")
        localDataSource.saveTask(newTask1)
        localDataSource.completeTask(newTask1)
        localDataSource.saveTask(newTask2)
        localDataSource.completeTask(newTask2)
        localDataSource.saveTask(newTask3)
        // When completed tasks are cleared in the repository
        localDataSource.clearCompletedTasks()

        // Then the completed tasks cannot be retrieved and the active one can
        assertThat(localDataSource.getTask(newTask1.id).succeeded, `is`(false))
        assertThat(localDataSource.getTask(newTask2.id).succeeded, `is`(false))

        val result3 = localDataSource.getTask(newTask3.id)

        assertThat(result3.succeeded, `is`(true))
        result3 as Success

        assertThat(result3.data, `is`(newTask3))
    }

    @Test
    fun deleteAllTasks_emptyListOfRetrievedTask() = runBlockingTest {
        // Given a new task in the persistent repository and a mocked callback
        val newTask = Task("title")

        localDataSource.saveTask(newTask)

        // When all tasks are deleted
        localDataSource.deleteAllTasks()

        // Then the retrieved tasks is an empty list
        val result = localDataSource.getTasks() as Success
        assertThat(result.data.isEmpty(), `is`(true))
    }

    @Test
    fun getTasks_retrieveSavedTasks() = runBlockingTest {
        // Given 2 new tasks in the persistent repository
        val newTask1 = Task("title")
        val newTask2 = Task("title")

        localDataSource.saveTask(newTask1)
        localDataSource.saveTask(newTask2)
        // Then the tasks can be retrieved from the persistent repository
        val results = localDataSource.getTasks() as Success<List<Task>>
        val tasks = results.data
        assertThat(tasks.size, `is`(2))
    }
}
