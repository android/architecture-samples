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
package com.example.android.architecture.blueprints.todoapp.taskdetail

import com.example.android.architecture.blueprints.todoapp.MockitoHelper
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.inOrder
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations

/**
 * Unit tests for the implementation of [TaskDetailPresenter]
 */
class TaskDetailPresenterTest {

    private val TITLE_TEST = "title"

    private val DESCRIPTION_TEST = "description"

    private val INVALID_TASK_ID = ""

    private val ACTIVE_TASK = Task(TITLE_TEST, DESCRIPTION_TEST)

    private val COMPLETED_TASK = Task(TITLE_TEST, DESCRIPTION_TEST).apply { isCompleted = true }

    @Mock private lateinit var tasksRepository: TasksRepository

    @Mock private lateinit var taskDetailView: TaskDetailContract.View

    /**
     * [ArgumentCaptor] is a powerful Mockito API to capture argument values and use them to
     * perform further actions or assertions on them.
     */
    @Captor private lateinit var getTaskCallbackCaptor:
            ArgumentCaptor<TasksDataSource.GetTaskCallback>

    private lateinit var taskDetailPresenter: TaskDetailPresenter

    @Before fun setup() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this)

        // The presenter won't update the view unless it's active.
        `when`(taskDetailView.isActive).thenReturn(true)
    }

    @Test fun createPresenter_setsThePresenterToView() {
        // Get a reference to the class under test
        taskDetailPresenter = TaskDetailPresenter(
                ACTIVE_TASK.id, tasksRepository, taskDetailView)

        // Then the presenter is set to the view
        verify(taskDetailView).presenter = taskDetailPresenter
    }

    @Test fun getActiveTaskFromRepositoryAndLoadIntoView() {
        // When tasks presenter is asked to open a task
        taskDetailPresenter = TaskDetailPresenter(
                ACTIVE_TASK.id, tasksRepository, taskDetailView).apply { start() }

        // Then task is loaded from model, callback is captured and progress indicator is shown
        verify(tasksRepository).getTask(MockitoHelper.eq(ACTIVE_TASK.id),
                MockitoHelper.capture(getTaskCallbackCaptor))
        val inOrder = inOrder(taskDetailView)
        inOrder.verify(taskDetailView).setLoadingIndicator(true)

        // When task is finally loaded
        getTaskCallbackCaptor.value.onTaskLoaded(ACTIVE_TASK) // Trigger callback

        // Then progress indicator is hidden and title, description and completion status are shown
        // in UI
        inOrder.verify(taskDetailView).setLoadingIndicator(false)
        verify(taskDetailView).showTitle(TITLE_TEST)
        verify(taskDetailView).showDescription(DESCRIPTION_TEST)
        verify(taskDetailView).showCompletionStatus(false)
    }

    @Test fun getCompletedTaskFromRepositoryAndLoadIntoView() {
        taskDetailPresenter = TaskDetailPresenter(
                COMPLETED_TASK.id, tasksRepository, taskDetailView).apply { start() }

        // Then task is loaded from model, callback is captured and progress indicator is shown
        verify(tasksRepository).getTask(
                MockitoHelper.eq(COMPLETED_TASK.id), MockitoHelper.capture(getTaskCallbackCaptor))
        val inOrder = inOrder(taskDetailView)
        inOrder.verify(taskDetailView).setLoadingIndicator(true)

        // When task is finally loaded
        getTaskCallbackCaptor.value.onTaskLoaded(COMPLETED_TASK) // Trigger callback

        // Then progress indicator is hidden and title, description and completion status are shown
        // in UI
        inOrder.verify(taskDetailView).setLoadingIndicator(false)
        verify(taskDetailView).showTitle(TITLE_TEST)
        verify(taskDetailView).showDescription(DESCRIPTION_TEST)
        verify(taskDetailView).showCompletionStatus(true)
    }

    @Test
    fun getUnknownTaskFromRepositoryAndLoadIntoView() {
        // When loading of a task is requested with an invalid task ID.
        taskDetailPresenter = TaskDetailPresenter(
                INVALID_TASK_ID, tasksRepository, taskDetailView).apply { start() }
        verify(taskDetailView).showMissingTask()
    }

    @Test fun deleteTask() {
        // Given an initialized TaskDetailPresenter with stubbed task
        val task = Task(TITLE_TEST, DESCRIPTION_TEST)

        // When the deletion of a task is requested
        taskDetailPresenter = TaskDetailPresenter(
                task.id, tasksRepository, taskDetailView).apply { deleteTask() }

        // Then the repository and the view are notified
        verify(tasksRepository).deleteTask(task.id)
        verify(taskDetailView).showTaskDeleted()
    }

    @Test
    fun completeTask() {
        // Given an initialized presenter with an active task
        val task = Task(TITLE_TEST, DESCRIPTION_TEST)
        taskDetailPresenter = TaskDetailPresenter(
                task.id, tasksRepository, taskDetailView).apply {
            start()
            completeTask()
        }

        // Then a request is sent to the task repository and the UI is updated
        verify(tasksRepository).completeTask(task.id)
        verify(taskDetailView).showTaskMarkedComplete()
    }

    @Test fun activateTask() {
        // Given an initialized presenter with a completed task
        val task = Task(TITLE_TEST, DESCRIPTION_TEST).apply { isCompleted = true }
        taskDetailPresenter = TaskDetailPresenter(
                task.id, tasksRepository, taskDetailView).apply {
            start()
            activateTask()
        }

        // Then a request is sent to the task repository and the UI is updated
        verify(tasksRepository).activateTask(task.id)
        verify(taskDetailView).showTaskMarkedActive()
    }

    @Test fun activeTaskIsShownWhenEditing() {
        // When the edit of an ACTIVE_TASK is requested
        taskDetailPresenter = TaskDetailPresenter(
                ACTIVE_TASK.id, tasksRepository, taskDetailView).apply { editTask() }

        // Then the view is notified
        verify(taskDetailView).showEditTask(ACTIVE_TASK.id)
    }

    @Test fun invalidTaskIsNotShownWhenEditing() {
        // When the edit of an invalid task id is requested
        taskDetailPresenter = TaskDetailPresenter(
                INVALID_TASK_ID, tasksRepository, taskDetailView).apply { editTask() }

        // Then the edit mode is never started
        verify(taskDetailView, never()).showEditTask(INVALID_TASK_ID)
        // instead, the error is shown.
        verify(taskDetailView).showMissingTask()
    }
}
