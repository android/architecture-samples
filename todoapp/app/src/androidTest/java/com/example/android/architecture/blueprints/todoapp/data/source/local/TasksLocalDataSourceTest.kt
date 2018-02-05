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

import android.arch.persistence.room.Room
import android.support.test.InstrumentationRegistry
import android.support.test.filters.LargeTest
import android.support.test.runner.AndroidJUnit4
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.DataNotAvailableException
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource
import com.example.android.architecture.blueprints.todoapp.utils.SingleExecutors
import kotlinx.coroutines.experimental.runBlocking
import org.hamcrest.core.Is.`is`
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration test for the [TasksDataSource].
 */
@RunWith(AndroidJUnit4::class) @LargeTest class TasksLocalDataSourceTest {

    private val TITLE = "title"
    private val TITLE2 = "title2"
    private val TITLE3 = "title3"

    private lateinit var localDataSource: TasksLocalDataSource
    private lateinit var database: ToDoDatabase

    @Before
    fun setup() {
        // using an in-memory database for testing, since it doesn't survive killing the process
        database = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getContext(),
                ToDoDatabase::class.java)
                .build()

        // Make sure that we're not keeping a reference to the wrong instance.
        TasksLocalDataSource.clearInstance()
        localDataSource = TasksLocalDataSource.getInstance(SingleExecutors(), database.taskDao())
    }

    @After
    fun cleanUp() {
        database.close()
        TasksLocalDataSource.clearInstance()
    }

    @Test fun testPreConditions() {
        assertNotNull(localDataSource)
    }

    @Test fun saveTask_retrievesTask() {
        // Given a new task
        val newTask = Task(TITLE)

        with(localDataSource) {
            // When saved into the persistent repository
            saveTask(newTask)

            // Then the task can be retrieved from the persistent repository
            runBlocking {
                val task = getTask(newTask.id)
                assertThat(task, `is`(newTask))
            }
        }
    }

    @Test fun completeTask_retrievedTaskIsComplete() {
        // Given a new task in the persistent repository
        val newTask = Task(TITLE)
        localDataSource.saveTask(newTask)

        // When completed in the persistent repository
        localDataSource.completeTask(newTask)

        runBlocking {
            // Then the task can be retrieved from the persistent repository and is complete
            val task = localDataSource.getTask(newTask.id)
            assertThat(task, `is`(newTask))
            assertThat(task!!.isCompleted, `is`(true))
        }
    }

    @Test
    fun activateTask_retrievedTaskIsActive() {
        // Initialize mock for the callback.

        // Given a new completed task in the persistent repository
        val newTask = Task(TITLE)
        with(localDataSource) {
            saveTask(newTask)
            completeTask(newTask)

            // When activated in the persistent repository
            activateTask(newTask)

            // Then the task can be retrieved from the persistent repository and is active
            runBlocking {
                val task = getTask(newTask.id)
                assertThat(newTask.isCompleted, `is`(false))
            }
        }
    }

    @Test fun clearCompletedTask_taskNotRetrievable() {

        // Given 2 new completed tasks and 1 active task in the persistent repository
        val newTask1 = Task(TITLE)
        val newTask2 = Task(TITLE2)
        val newTask3 = Task(TITLE3)

        with(localDataSource) {
            saveTask(newTask1)
            completeTask(newTask1)
            saveTask(newTask2)
            completeTask(newTask2)
            saveTask(newTask3)
            // When completed tasks are cleared in the repository
            clearCompletedTasks()

            runBlocking {
                // Then the completed tasks cannot be retrieved and the active one can
                try {
                    getTask(newTask1.id)
                    fail("Should not return task here")
                } catch (e : DataNotAvailableException) {
                    // expected
                }

                try {
                    getTask(newTask2.id)
                    fail("Should not return task here")
                } catch (e : DataNotAvailableException) {
                    // expected
                }

                val task3 = getTask(newTask3.id)
                assertEquals(newTask3.id, task3!!.id)
            }
        }
    }

    @Test fun deleteAllTasks_emptyListOfRetrievedTask() {
        with(localDataSource) {
            // Given a new task in the persistent repository and a mocked callback
            saveTask(Task(TITLE))

            // When all tasks are deleted
            deleteAllTasks()

            runBlocking {
                try {
                    getTasks()
                    fail("Should not return task here")
                } catch (e: DataNotAvailableException) {
                    // expected
                }
            }
        }
    }

    @Test fun getTasks_retrieveSavedTasks() {
        with(localDataSource) {
            // Given 2 new tasks in the persistent repository
            val newTask1 = Task(TITLE)
            saveTask(newTask1)
            val newTask2 = Task(TITLE)
            saveTask(newTask2)

            // Then the tasks can be retrieved from the persistent repository
            runBlocking {
                val tasks = getTasks()
                assertNotNull(tasks)
                assertTrue(tasks.size >= 2)

                var newTask1IdFound = false
                var newTask2IdFound = false
                for (task in tasks) {
                    if (task.id == newTask1.id) {
                        newTask1IdFound = true
                    }
                    if (task.id == newTask2.id) {
                        newTask2IdFound = true
                    }
                }
                assertTrue(newTask1IdFound)
                assertTrue(newTask2IdFound)
            }

        }
    }
}
