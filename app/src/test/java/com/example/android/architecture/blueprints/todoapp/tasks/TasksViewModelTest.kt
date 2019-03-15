///*
// * Copyright 2016, The Android Open Source Project
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
//
//package com.example.android.architecture.blueprints.todoapp.tasks
//
//import android.app.Application
//import android.content.Context
//import android.content.res.Resources
//import androidx.arch.core.executor.testing.InstantTaskExecutorRule
//import com.example.android.architecture.blueprints.todoapp.LiveDataTestUtil
//import com.example.android.architecture.blueprints.todoapp.R
//import com.example.android.architecture.blueprints.todoapp.R.string.successfully_deleted_task_message
//import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskActivity
//import com.example.android.architecture.blueprints.todoapp.data.Task
//import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource.LoadTasksCallback
//import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository
//import com.example.android.architecture.blueprints.todoapp.util.ADD_EDIT_RESULT_OK
//import com.example.android.architecture.blueprints.todoapp.util.DELETE_RESULT_OK
//import com.example.android.architecture.blueprints.todoapp.util.EDIT_RESULT_OK
//import com.example.android.architecture.blueprints.todoapp.util.any
//import com.example.android.architecture.blueprints.todoapp.util.capture
//import com.google.common.collect.Lists
//import org.junit.Assert.assertEquals
//import org.junit.Assert.assertFalse
//import org.junit.Assert.assertNotNull
//import org.junit.Assert.assertTrue
//import org.junit.Before
//import org.junit.Rule
//import org.junit.Test
//import org.mockito.ArgumentCaptor
//import org.mockito.Captor
//import org.mockito.Mock
//import org.mockito.Mockito.`when`
//import org.mockito.Mockito.mock
//import org.mockito.Mockito.verify
//import org.mockito.MockitoAnnotations
//
///**
// * Unit tests for the implementation of [TasksViewModel]
// */
//class TasksViewModelTest {
//
//    // Executes each task synchronously using Architecture Components.
//    @get:Rule var instantExecutorRule = InstantTaskExecutorRule()
//    @Mock private lateinit var tasksRepository: TasksRepository
//    @Mock private lateinit var context: Application
//    @Captor private lateinit var loadTasksCallbackCaptor: ArgumentCaptor<LoadTasksCallback>
//    private lateinit var tasksViewModel: TasksViewModel
//    private lateinit var tasks: List<Task>
//
//    @Before
//    fun setupTasksViewModel() {
//        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
//        // inject the mocks in the test the initMocks method needs to be called.
//        MockitoAnnotations.initMocks(this)
//
//        setupContext()
//
//        // Get a reference to the class under test
//        tasksViewModel = TasksViewModel(tasksRepository)
//
//        // We initialise the tasks to 3, with one active and two completed
//        val task1 = Task("Title1", "Description1")
//        val task2 = Task("Title2", "Description2").apply {
//            isCompleted = true
//        }
//        val task3 = Task("Title3", "Description3").apply {
//            isCompleted = true
//        }
//        tasks = Lists.newArrayList(task1, task2, task3)
//
//    }
//
//    private fun setupContext() {
//        `when`<Context>(context.applicationContext).thenReturn(context)
//        `when`(context.getString(R.string.successfully_saved_task_message))
//                .thenReturn("EDIT_RESULT_OK")
//        `when`(context.getString(R.string.successfully_added_task_message))
//                .thenReturn("ADD_EDIT_RESULT_OK")
//        `when`(context.getString(successfully_deleted_task_message))
//                .thenReturn("DELETE_RESULT_OK")
//
//        `when`(context.resources).thenReturn(mock(Resources::class.java))
//    }
//
//    @Test
//    fun loadAllTasksFromRepository_dataLoaded() {
//        // Given an initialized TasksViewModel with initialized tasks
//        // When loading of Tasks is requested
//        tasksViewModel.setFiltering(TasksFilterType.ALL_TASKS)
//        tasksViewModel.loadTasks(true)
//
//        // Callback is captured and invoked with stubbed tasks
//        verify<TasksRepository>(tasksRepository).getTasks(capture(loadTasksCallbackCaptor))
//
//        // Then progress indicator is shown
//        assertTrue(LiveDataTestUtil.getValue(tasksViewModel.dataLoading))
//        loadTasksCallbackCaptor.value.onTasksLoaded(tasks)
//
//        // Then progress indicator is hidden
//        assertFalse(LiveDataTestUtil.getValue(tasksViewModel.dataLoading))
//
//        // And data loaded
//        assertFalse(LiveDataTestUtil.getValue(tasksViewModel.items).isEmpty())
//        assertTrue(LiveDataTestUtil.getValue(tasksViewModel.items).size == 3)
//    }
//
//    @Test
//    fun loadActiveTasksFromRepositoryAndLoadIntoView() {
//        // Given an initialized TasksViewModel with initialized tasks
//        // When loading of Tasks is requested
//        tasksViewModel.setFiltering(TasksFilterType.ACTIVE_TASKS)
//        tasksViewModel.loadTasks(true)
//
//        // Callback is captured and invoked with stubbed tasks
//        verify<TasksRepository>(tasksRepository).getTasks(capture(loadTasksCallbackCaptor))
//        loadTasksCallbackCaptor.value.onTasksLoaded(tasks)
//
//        // Then progress indicator is hidden
//        assertFalse(LiveDataTestUtil.getValue(tasksViewModel.dataLoading))
//
//        // And data loaded
//        assertFalse(LiveDataTestUtil.getValue(tasksViewModel.items).isEmpty())
//        assertTrue(LiveDataTestUtil.getValue(tasksViewModel.items).size == 1)
//    }
//
//    @Test
//    fun loadCompletedTasksFromRepositoryAndLoadIntoView() {
//        // Given an initialized TasksViewModel with initialized tasks
//        // When loading of Tasks is requested
//        tasksViewModel.setFiltering(TasksFilterType.COMPLETED_TASKS)
//        tasksViewModel.loadTasks(true)
//
//        // Callback is captured and invoked with stubbed tasks
//        verify<TasksRepository>(tasksRepository).getTasks(capture(loadTasksCallbackCaptor))
//        loadTasksCallbackCaptor.value.onTasksLoaded(tasks)
//
//        // Then progress indicator is hidden
//        assertFalse(LiveDataTestUtil.getValue(tasksViewModel.dataLoading))
//
//        // And data loaded
//        assertFalse(LiveDataTestUtil.getValue(tasksViewModel.items).isEmpty())
//        assertTrue(LiveDataTestUtil.getValue(tasksViewModel.items).size == 2)
//    }
//
//    @Test
//    @Throws(InterruptedException::class)
//    fun clickOnFab_ShowsAddTaskUi() {
//        // When adding a new task
//        tasksViewModel.addNewTask()
//
//        // Then the event is triggered
//        val value = LiveDataTestUtil.getValue(tasksViewModel.newTaskEvent)
//        assertNotNull(value.getContentIfNotHandled())
//    }
//
//    @Test
//    fun clearCompletedTasks_ClearsTasks() {
//        // When completed tasks are cleared
//        tasksViewModel.clearCompletedTasks()
//
//        // Then repository is called and the view is notified
//        verify(tasksRepository).clearCompletedTasks()
//        verify(tasksRepository).getTasks(any())
//    }
//
//    @Test
//    @Throws(InterruptedException::class)
//    fun handleActivityResult_editOK() {
//        // When TaskDetailActivity sends a EDIT_RESULT_OK
//        tasksViewModel.handleActivityResult(
//            AddEditTaskActivity.REQUEST_CODE, EDIT_RESULT_OK
//        )
//
//        // Then the event is triggered
//        val value = LiveDataTestUtil.getValue(tasksViewModel.snackbarMessage)
//        assertEquals(
//            value.getContentIfNotHandled(),
//            R.string.successfully_saved_task_message
//        )
//    }
//
//    @Test
//    @Throws(InterruptedException::class)
//    fun handleActivityResult_addEditOK() {
//        // When TaskDetailActivity sends an EDIT_RESULT_OK
//        tasksViewModel.handleActivityResult(
//            AddEditTaskActivity.REQUEST_CODE, ADD_EDIT_RESULT_OK
//        )
//
//        // Then the snackbar shows the correct message
//        val value = LiveDataTestUtil.getValue(tasksViewModel.snackbarMessage)
//        assertEquals(
//            value.getContentIfNotHandled(),
//            R.string.successfully_added_task_message
//        )
//    }
//
//    @Test
//    @Throws(InterruptedException::class)
//    fun handleActivityResult_deleteOk() {
//        // When TaskDetailActivity sends a DELETE_RESULT_OK
//        tasksViewModel.handleActivityResult(
//            AddEditTaskActivity.REQUEST_CODE, DELETE_RESULT_OK
//        )
//
//        // Then the snackbar shows the correct message
//        val value = LiveDataTestUtil.getValue(tasksViewModel.snackbarMessage)
//        assertEquals(
//            value.getContentIfNotHandled(),
//            R.string.successfully_deleted_task_message
//        )
//    }
//
//    @Test
//    @Throws(InterruptedException::class)
//    fun getTasksAddViewVisible() {
//        // When the filter type is ALL_TASKS
//        tasksViewModel.setFiltering(TasksFilterType.ALL_TASKS)
//
//        // Then the "Add task" action is visible
//        assertTrue(LiveDataTestUtil.getValue(tasksViewModel.tasksAddViewVisible))
//    }
//}
