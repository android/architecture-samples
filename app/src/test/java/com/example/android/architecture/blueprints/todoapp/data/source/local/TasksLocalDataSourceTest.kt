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
package com.example.android.architecture.blueprints.todoapp.data.source.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.example.android.architecture.blueprints.todoapp.data.Result
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource
import com.example.android.architecture.blueprints.todoapp.data.succeeded
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration test for the [TasksDataSource].
 */
@RunWith(AndroidJUnit4::class)
@MediumTest
class TasksLocalDataSourceTest {

    private lateinit var localDataSource: TasksLocalDataSource
    private lateinit var database: ToDoDatabase

    @Before
    fun setup() {
        // using an in-memory database for testing, since it doesn't survive killing the process
        database = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(),
            ToDoDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        localDataSource = TasksLocalDataSource(database.taskDao())
    }

    @After
    fun cleanUp() {
        database.close()
    }

    @Test
    fun saveTask_retrievesTask() = runBlocking {
        // GIVEN - a new task saved in the database
        val newTask = Task("title", "description", true)
        localDataSource.saveTask(newTask)

        // WHEN  - Task retrieved by ID
        val result = localDataSource.getTask(newTask.id)

        // THEN - Same task is returned
        assertThat(result.succeeded).isTrue()
        result as Result.Success
        assertThat(result.data.title).isEqualTo("title")
        assertThat(result.data.description).isEqualTo("description")
        assertThat(result.data.isCompleted).isTrue()
    }

    @Test
    fun completeTask_retrievedTaskIsComplete() = runBlocking {
        // Given a new task in the persistent repository
        val newTask = Task("title")
        localDataSource.saveTask(newTask)

        // When completed in the persistent repository
        localDataSource.completeTask(newTask)
        val result = localDataSource.getTask(newTask.id)

        // Then the task can be retrieved from the persistent repository and is complete
        assertThat(result.succeeded).isTrue()
        result as Result.Success
        assertThat(result.data.title).isEqualTo(newTask.title)
        assertThat(result.data.isCompleted).isTrue()
    }

    @Test
    fun activateTask_retrievedTaskIsActive() = runBlocking {
        // Given a new completed task in the persistent repository
        val newTask = Task("Some title", "Some description", true)
        localDataSource.saveTask(newTask)

        localDataSource.activateTask(newTask)

        // Then the task can be retrieved from the persistent repository and is active
        val result = localDataSource.getTask(newTask.id)

        assertThat(result.succeeded).isTrue()
        result as Result.Success

        assertThat(result.data.title).isEqualTo("Some title")
        assertThat(result.data.isCompleted).isFalse()
    }

    @Test
    fun clearCompletedTask_taskNotRetrievable() = runBlocking {
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
        assertThat(localDataSource.getTask(newTask1.id).succeeded).isFalse()
        assertThat(localDataSource.getTask(newTask2.id).succeeded).isFalse()

        val result3 = localDataSource.getTask(newTask3.id)

        assertThat(result3.succeeded).isTrue()
        result3 as Result.Success

        assertThat(result3.data).isEqualTo(newTask3)
    }

    @Test
    fun deleteAllTasks_emptyListOfRetrievedTask() = runBlocking {
        // Given a new task in the persistent repository and a mocked callback
        val newTask = Task("title")

        localDataSource.saveTask(newTask)

        // When all tasks are deleted
        localDataSource.deleteAllTasks()

        // Then the retrieved tasks is an empty list
        val result = localDataSource.getTasks() as Result.Success
        assertThat(result.data).isEmpty()

    }

    @Test
    fun getTasks_retrieveSavedTasks() = runBlocking {
        // Given 2 new tasks in the persistent repository
        val newTask1 = Task("title")
        val newTask2 = Task("title")

        localDataSource.saveTask(newTask1)
        localDataSource.saveTask(newTask2)
        // Then the tasks can be retrieved from the persistent repository
        val results = localDataSource.getTasks() as Result.Success<List<Task>>
        val tasks = results.data
        assertThat(tasks).hasSize(2)
    }
}
