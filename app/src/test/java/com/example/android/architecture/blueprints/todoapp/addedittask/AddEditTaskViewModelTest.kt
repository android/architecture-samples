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
//package com.example.android.architecture.blueprints.todoapp.addedittask
//
//
//import android.app.Application
//import androidx.arch.core.executor.testing.InstantTaskExecutorRule
//import com.example.android.architecture.blueprints.todoapp.LiveDataTestUtil
//import com.example.android.architecture.blueprints.todoapp.data.Task
//import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource
//import com.example.android.architecture.blueprints.todoapp.data.source.DefaultTasksRepository
//import com.example.android.architecture.blueprints.todoapp.util.any
//import com.example.android.architecture.blueprints.todoapp.util.capture
//import com.example.android.architecture.blueprints.todoapp.util.eq
//import com.example.android.architecture.blueprints.todoapp.util.mock
//import org.hamcrest.Matchers.`is`
//import org.junit.Assert.assertThat
//import org.junit.Before
//import org.junit.Rule
//import org.junit.Test
//import org.mockito.ArgumentCaptor
//import org.mockito.Captor
//import org.mockito.Mock
//import org.mockito.Mockito.verify
//import org.mockito.MockitoAnnotations
//
///**
// * Unit tests for the implementation of [AddEditTaskViewModel].
// */
//class AddEditTaskViewModelTest {
//
//    // Executes each task synchronously using Architecture Components.
//    @get:Rule var instantExecutorRule = InstantTaskExecutorRule()
//    @Mock private lateinit var tasksRepository: DefaultTasksRepository
//    /**
//     * [ArgumentCaptor] is a powerful Mockito API to capture argument values and use them to
//     * perform further actions or assertions on them.
//     */
//    @Captor private lateinit var getTaskCallbackCaptor:
//            ArgumentCaptor<TasksDataSource.GetTaskCallback>
//    @Captor private lateinit var saveTaskTaskCaptor: ArgumentCaptor<Task>
//    private lateinit var addEditTaskViewModel: AddEditTaskViewModel
//
//    @Before fun setupAddEditTaskViewModel() {
//        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
//        // inject the mocks in the test the initMocks method needs to be called.
//        MockitoAnnotations.initMocks(this)
//
//        // Get a reference to the class under test
//        addEditTaskViewModel = AddEditTaskViewModel(tasksRepository)
//    }
//
//    @Test fun saveNewTaskToRepository_showsSuccessMessageUi() {
//        // When the ViewModel is asked to save a task
//        with(addEditTaskViewModel) {
//            description.value = "Some Task Description"
//            title.value = "New Task Title"
//            saveTask()
//        }
//
//        // Then a task is saved in the repository and the view updated
//        verify(tasksRepository).saveTask(capture(saveTaskTaskCaptor))
//        assertThat(saveTaskTaskCaptor.value.title, `is`("New Task Title"))
//        assertThat(saveTaskTaskCaptor.value.description, `is`("Some Task Description"))
//    }
//
//    @Test fun populateTask_callsRepoAndUpdatesView() {
//        val testTask = Task("TITLE", "DESCRIPTION", "1")
//
//        // Get a reference to the class under test
//        addEditTaskViewModel = AddEditTaskViewModel(tasksRepository).apply {
//            // When the ViewModel is asked to populate an existing task
//            start(testTask.id)
//        }
//
//        // Then the task repository is queried and the view updated
//        verify(tasksRepository).getTask(eq(testTask.id),
//                capture(getTaskCallbackCaptor))
//
//        // Simulate callback
//        getTaskCallbackCaptor.value.onTaskLoaded(testTask)
//
//        // Verify the fields were updated
//        assertThat(
//            LiveDataTestUtil.getValue(addEditTaskViewModel.title), `is`(testTask.title))
//        assertThat(
//            LiveDataTestUtil.getValue(addEditTaskViewModel.description), `is`(testTask.description))
//    }
//}
