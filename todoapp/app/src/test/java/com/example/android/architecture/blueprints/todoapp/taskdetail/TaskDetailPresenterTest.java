/*
 * Copyright 2016, The Android Open Source Project
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

package com.example.android.architecture.blueprints.todoapp.taskdetail;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.android.architecture.blueprints.todoapp.TestUseCaseScheduler;
import com.example.android.architecture.blueprints.todoapp.UseCaseHandler;
import com.example.android.architecture.blueprints.todoapp.addedittask.domain.usecase.DeleteTask;
import com.example.android.architecture.blueprints.todoapp.addedittask.domain.usecase.GetTask;
import com.example.android.architecture.blueprints.todoapp.tasks.domain.model.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;
import com.example.android.architecture.blueprints.todoapp.tasks.domain.usecase.ActivateTask;
import com.example.android.architecture.blueprints.todoapp.tasks.domain.usecase.CompleteTask;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Unit tests for the implementation of {@link TaskDetailPresenter}
 */
public class TaskDetailPresenterTest {

    public static final String TITLE_TEST = "title";

    public static final String DESCRIPTION_TEST = "description";

    public static final String INVALID_TASK_ID = "";

    public static final Task ACTIVE_TASK = new Task(TITLE_TEST, DESCRIPTION_TEST);

    public static final Task COMPLETED_TASK = new Task(TITLE_TEST, DESCRIPTION_TEST, true);

    @Mock
    private TasksRepository mTasksRepository;

    @Mock
    private TaskDetailContract.View mTaskDetailView;

    /**
     * {@link ArgumentCaptor} is a powerful Mockito API to capture argument values and use them to
     * perform further actions or assertions on them.
     */
    @Captor
    private ArgumentCaptor<TasksDataSource.GetTaskCallback> mGetTaskCallbackCaptor;

    private TaskDetailPresenter mTaskDetailPresenter;

    @Before
    public void setup() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);

        // The presenter won't update the view unless it's active.
        when(mTaskDetailView.isActive()).thenReturn(true);
    }

    @Test
    public void getActiveTaskFromRepositoryAndLoadIntoView() {
        // When tasks presenter is asked to open a task
        mTaskDetailPresenter = givenTaskDetailPresenter(ACTIVE_TASK.getId());
        mTaskDetailPresenter.start();

        // Then task is loaded from model, callback is captured and progress indicator is shown
        verify(mTasksRepository).getTask(eq(ACTIVE_TASK.getId()), mGetTaskCallbackCaptor.capture());
        verify(mTaskDetailView).setLoadingIndicator(true);

        // When task is finally loaded
        mGetTaskCallbackCaptor.getValue().onTaskLoaded(ACTIVE_TASK); // Trigger callback

        // Then progress indicator is hidden and title, description and completion status are shown
        // in UI
        verify(mTaskDetailView).setLoadingIndicator(false);
        verify(mTaskDetailView).showTitle(TITLE_TEST);
        verify(mTaskDetailView).showDescription(DESCRIPTION_TEST);
        verify(mTaskDetailView).showCompletionStatus(false);
    }

    @Test
    public void getCompletedTaskFromRepositoryAndLoadIntoView() {
        mTaskDetailPresenter = givenTaskDetailPresenter(COMPLETED_TASK.getId());
        mTaskDetailPresenter.start();

        // Then task is loaded from model, callback is captured and progress indicator is shown
        verify(mTasksRepository).getTask(
                eq(COMPLETED_TASK.getId()), mGetTaskCallbackCaptor.capture());
        verify(mTaskDetailView).setLoadingIndicator(true);

        // When task is finally loaded
        mGetTaskCallbackCaptor.getValue().onTaskLoaded(COMPLETED_TASK); // Trigger callback

        // Then progress indicator is hidden and title, description and completion status are shown
        // in UI
        verify(mTaskDetailView).setLoadingIndicator(false);
        verify(mTaskDetailView).showTitle(TITLE_TEST);
        verify(mTaskDetailView).showDescription(DESCRIPTION_TEST);
        verify(mTaskDetailView).showCompletionStatus(true);
    }

    @Test
    public void getUnknownTaskFromRepositoryAndLoadIntoView() {
        // When loading of a task is requested with an invalid task ID.
        mTaskDetailPresenter = givenTaskDetailPresenter(INVALID_TASK_ID);
        mTaskDetailPresenter.start();
        verify(mTaskDetailView).showMissingTask();
    }

    @Test
    public void deleteTask() {
        // Given an initialized TaskDetailPresenter with stubbed task
        Task task = new Task(TITLE_TEST, DESCRIPTION_TEST);

        // When the deletion of a task is requested
        mTaskDetailPresenter = givenTaskDetailPresenter(task.getId());
        mTaskDetailPresenter.deleteTask();

        // Then the repository and the view are notified
        verify(mTasksRepository).deleteTask(task.getId());
        verify(mTaskDetailView).showTaskDeleted();
    }

    @Test
    public void completeTask() {
        // Given an initialized presenter with an active task
        Task task = new Task(TITLE_TEST, DESCRIPTION_TEST);
        mTaskDetailPresenter = givenTaskDetailPresenter(task.getId());
        mTaskDetailPresenter.start();

        // When the presenter is asked to complete the task
        mTaskDetailPresenter.completeTask();

        // Then a request is sent to the task repository and the UI is updated
        verify(mTasksRepository).completeTask(task.getId());
        verify(mTaskDetailView).showTaskMarkedComplete();
    }

    @Test
    public void activateTask() {
        // Given an initialized presenter with a completed task
        Task task = new Task(TITLE_TEST, DESCRIPTION_TEST, true);
        mTaskDetailPresenter = givenTaskDetailPresenter(task.getId());
        mTaskDetailPresenter.start();

        // When the presenter is asked to activate the task
        mTaskDetailPresenter.activateTask();

        // Then a request is sent to the task repository and the UI is updated
        verify(mTasksRepository).activateTask(task.getId());
        verify(mTaskDetailView).showTaskMarkedActive();
    }

    @Test
    public void activeTaskIsShownWhenEditing() {
        // When the edit of an ACTIVE_TASK is requested
        mTaskDetailPresenter = givenTaskDetailPresenter(ACTIVE_TASK.getId());
        mTaskDetailPresenter.editTask();

        // Then the view is notified
        verify(mTaskDetailView).showEditTask(ACTIVE_TASK.getId());
    }

    @Test
    public void invalidTaskIsNotShownWhenEditing() {
        // When the edit of an invalid task id is requested
        mTaskDetailPresenter = givenTaskDetailPresenter(INVALID_TASK_ID);
        mTaskDetailPresenter.editTask();

        // Then the edit mode is never started
        verify(mTaskDetailView, never()).showEditTask(INVALID_TASK_ID);
        // instead, the error is shown.
        verify(mTaskDetailView).showMissingTask();
    }

    private TaskDetailPresenter givenTaskDetailPresenter(String id) {
        UseCaseHandler useCaseHandler = new UseCaseHandler(new TestUseCaseScheduler(), new TestUseCaseScheduler());
        GetTask getTask = new GetTask(mTasksRepository);
        CompleteTask completeTask = new CompleteTask(mTasksRepository);
        ActivateTask activateTask = new ActivateTask(mTasksRepository);
        DeleteTask deleteTask = new DeleteTask(mTasksRepository);

        return new TaskDetailPresenter(useCaseHandler, id, mTaskDetailView,
                getTask, completeTask, activateTask, deleteTask);
    }

}
