///*
// * Copyright 2017, The Android Open Source Project
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *      http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package com.example.android.architecture.blueprints.todoapp.data.source.local
//
//import androidx.test.InstrumentationRegistry
//import androidx.test.filters.LargeTest
//import androidx.test.ext.junit.runners.AndroidJUnit4
//import com.example.android.architecture.blueprints.todoapp.data.Task
//import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource
//import com.example.android.architecture.blueprints.todoapp.util.any
//import com.example.android.architecture.blueprints.todoapp.util.mock
//import org.hamcrest.core.Is.`is`
//import org.junit.After
//import org.junit.Assert.assertNotNull
//import org.junit.Assert.assertThat
//import org.junit.Assert.assertTrue
//import org.junit.Assert.fail
//import org.junit.Before
//import org.junit.Test
//import org.junit.runner.RunWith
//import org.mockito.Mockito.mock
//import org.mockito.Mockito.never
//import org.mockito.Mockito.verify
//import androidx.room.Room
//import com.example.android.architecture.blueprints.todoapp.utils.SingleExecutors
//
//
///**
// * Integration test for the [TasksDataSource].
// */
//@RunWith(AndroidJUnit4::class) @LargeTest class TasksLocalDataSourceTest {
//
//    private val TITLE = "title"
//    private val TITLE2 = "title2"
//    private val TITLE3 = "title3"
//    private lateinit var localDataSource: TasksLocalDataSource
//    private lateinit var database: ToDoDatabase
//
//    @Before
//    fun setup() {
//        // using an in-memory database for testing, since it doesn't survive killing the process
//        database = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getContext(),
//                ToDoDatabase::class.java)
//                .build()
//
//        // Make sure that we're not keeping a reference to the wrong instance.
//        TasksLocalDataSource.clearInstance()
//        localDataSource = TasksLocalDataSource.getInstance(SingleExecutors(), database.taskDao())
//    }
//
//    @After
//    fun cleanUp() {
//        database.close()
//        TasksLocalDataSource.clearInstance()
//    }
//
//    @Test fun testPreConditions() {
//        assertNotNull(localDataSource)
//    }
//
//    @Test fun saveTask_retrievesTask() {
//        // Given a new task
//        val newTask = Task(TITLE)
//
//        with(localDataSource) {
//            // When saved into the persistent repository
//            saveTask(newTask)
//
//            // Then the task can be retrieved from the persistent repository
//            getTask(newTask.id, object : TasksDataSource.GetTaskCallback {
//                override fun onTaskLoaded(task: Task) {
//                    assertThat(task, `is`(newTask))
//                }
//
//                override fun onDataNotAvailable() {
//                    fail("Callback error")
//                }
//            })
//        }
//    }
//
//    @Test fun completeTask_retrievedTaskIsComplete() {
//        // Given a new task in the persistent repository
//        val newTask = Task(TITLE)
//        with(localDataSource) {
//            saveTask(newTask)
//
//            // When completed in the persistent repository
//            completeTask(newTask)
//
//            // Then the task can be retrieved from the persistent repository and is complete
//            getTask(newTask.id, object : TasksDataSource.GetTaskCallback {
//                override fun onTaskLoaded(task: Task) {
//                    assertThat(task, `is`(newTask))
//                    assertThat(task.isCompleted, `is`(true))
//                }
//
//                override fun onDataNotAvailable() {
//                    fail("Callback error")
//                }
//            })
//        }
//    }
//
//    @Test fun activateTask_retrievedTaskIsActive() {
//        // Initialize mock for the callback.
//        val callback = mock<TasksDataSource.GetTaskCallback>()
//
//        // Given a new completed task in the persistent repository
//        val newTask = Task(TITLE)
//        with(localDataSource) {
//            saveTask(newTask)
//            completeTask(newTask)
//
//            // When activated in the persistent repository
//            activateTask(newTask)
//
//            // Then the task can be retrieved from the persistent repository and is active
//            getTask(newTask.id, callback)
//        }
//        verify(callback, never()).onDataNotAvailable()
//        verify(callback).onTaskLoaded(newTask)
//
//        assertThat(newTask.isCompleted, `is`(false))
//    }
//
//    @Test fun clearCompletedTask_taskNotRetrievable() {
//        // Initialize mocks for the callbacks.
//        val callback1 = mock(TasksDataSource.GetTaskCallback::class.java)
//        val callback2 = mock(TasksDataSource.GetTaskCallback::class.java)
//        val callback3 = mock(TasksDataSource.GetTaskCallback::class.java)
//
//        // Given 2 new completed tasks and 1 active task in the persistent repository
//        val newTask1 = Task(TITLE)
//        val newTask2 = Task(TITLE2)
//        val newTask3 = Task(TITLE3)
//        with(localDataSource) {
//            saveTask(newTask1)
//            completeTask(newTask1)
//            saveTask(newTask2)
//            completeTask(newTask2)
//            saveTask(newTask3)
//            // When completed tasks are cleared in the repository
//            clearCompletedTasks()
//
//            // Then the completed tasks cannot be retrieved and the active one can
//            getTask(newTask1.id, callback1)
//
//            verify(callback1).onDataNotAvailable()
//            verify(callback1, never()).onTaskLoaded(newTask1)
//
//            getTask(newTask2.id, callback2)
//
//            verify(callback2).onDataNotAvailable()
//            verify(callback2, never()).onTaskLoaded(newTask1)
//
//            getTask(newTask3.id, callback3)
//
//            verify(callback3, never()).onDataNotAvailable()
//            verify(callback3).onTaskLoaded(newTask3)
//        }
//    }
//
//    @Test fun deleteAllTasks_emptyListOfRetrievedTask() {
//        val callback = mock(TasksDataSource.LoadTasksCallback::class.java)
//
//        // Given a new task in the persistent repository and a mocked callback
//        val newTask = Task(TITLE)
//
//        with(localDataSource) {
//            saveTask(newTask)
//
//            // When all tasks are deleted
//            deleteAllTasks()
//
//            // Then the retrieved tasks is an empty list
//            getTasks(callback)
//        }
//        verify<TasksDataSource.LoadTasksCallback>(callback).onDataNotAvailable()
//        verify<TasksDataSource.LoadTasksCallback>(callback, never())
//                .onTasksLoaded(any<List<Task>>())
//    }
//
//    @Test fun getTasks_retrieveSavedTasks() {
//        // Given 2 new tasks in the persistent repository
//        val newTask1 = Task(TITLE)
//        val newTask2 = Task(TITLE)
//
//        with(localDataSource) {
//            saveTask(newTask1)
//            saveTask(newTask2)
//            // Then the tasks can be retrieved from the persistent repository
//            getTasks(object : TasksDataSource.LoadTasksCallback {
//                override fun onTasksLoaded(tasks: List<Task>) {
//                    assertNotNull(tasks)
//                    assertTrue(tasks.size >= 2)
//
//                    var newTask1IdFound = false
//                    var newTask2IdFound = false
//                    for ((_, _, id) in tasks) {
//                        if (id == newTask1.id) {
//                            newTask1IdFound = true
//                        }
//                        if (id == newTask2.id) {
//                            newTask2IdFound = true
//                        }
//                    }
//                    assertTrue(newTask1IdFound)
//                    assertTrue(newTask2IdFound)
//                }
//
//                override fun onDataNotAvailable() {
//                    fail()
//                }
//            })
//        }
//    }
//}
