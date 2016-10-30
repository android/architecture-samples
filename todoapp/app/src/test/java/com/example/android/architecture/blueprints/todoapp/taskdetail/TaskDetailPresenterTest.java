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

import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;
import com.example.android.architecture.blueprints.todoapp.util.schedulers.BaseSchedulerProvider;
import com.example.android.architecture.blueprints.todoapp.util.schedulers.ImmediateSchedulerProvider;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import rx.Observable;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    private BaseSchedulerProvider mSchedulerProvider;

    private TaskDetailPresenter mTaskDetailPresenter;

    @Before
    public void setup() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);

        // Make the sure that all schedulers are immediate.
        mSchedulerProvider = new ImmediateSchedulerProvider();

        // The presenter won't update the view unless it's active.
        when(mTaskDetailView.isActive()).thenReturn(true);
    }

    @Test
    public void getActiveTaskFromRepositoryAndLoadIntoView() {
        // When tasks presenter is asked to open a task
        mTaskDetailPresenter = new TaskDetailPresenter(
                ACTIVE_TASK.getId(), mTasksRepository, mTaskDetailView, mSchedulerProvider);
        setTaskAvailable(ACTIVE_TASK);
        mTaskDetailPresenter.subscribe();

        // Then task is loaded from model, callback is captured and progress indicator is shown
        verify(mTasksRepository).getTask(eq(ACTIVE_TASK.getId()));
        verify(mTaskDetailView).setLoadingIndicator(true);

        // Then progress indicator is hidden and title, description and completion status are shown
        // in UI
        verify(mTaskDetailView).setLoadingIndicator(false);
        Task task = new Task(TITLE_TEST, DESCRIPTION_TEST, false);
        verify(mTaskDetailView).showTask(task);
        // verify(mTaskDetailView).showTitle(TITLE_TEST);
        // verify(mTaskDetailView).showDescription(DESCRIPTION_TEST);
        // verify(mTaskDetailView).showCompletionStatus(false);
    }

    @Test
    public void getCompletedTaskFromRepositoryAndLoadIntoView() {
        mTaskDetailPresenter = new TaskDetailPresenter(
                COMPLETED_TASK.getId(), mTasksRepository, mTaskDetailView, mSchedulerProvider);
        setTaskAvailable(COMPLETED_TASK);
        mTaskDetailPresenter.subscribe();

        // Then task is loaded from model, callback is captured and progress indicator is shown
        verify(mTasksRepository).getTask(eq(COMPLETED_TASK.getId()));
        verify(mTaskDetailView).setLoadingIndicator(true);

        // Then progress indicator is hidden and title, description and completion status are shown
        // in UI
        verify(mTaskDetailView).setLoadingIndicator(false);
        Task task = new Task(TITLE_TEST, DESCRIPTION_TEST, true);
        verify(mTaskDetailView).showTask(task);
        // verify(mTaskDetailView).showTitle(TITLE_TEST);
        // verify(mTaskDetailView).showDescription(DESCRIPTION_TEST);
        // verify(mTaskDetailView).showCompletionStatus(true);
    }

    @Test
    public void getUnknownTaskFromRepositoryAndLoadIntoView() {
        // When loading of a task is requested with an invalid task ID.
        mTaskDetailPresenter = new TaskDetailPresenter(
                INVALID_TASK_ID, mTasksRepository, mTaskDetailView, mSchedulerProvider);
        mTaskDetailPresenter.subscribe();
        verify(mTaskDetailView).showMissingTask();
    }

    @Test
    public void deleteTask() {
        // Given an initialized TaskDetailPresenter with stubbed task
        Task task = new Task(TITLE_TEST, DESCRIPTION_TEST);

        // When the deletion of a task is requested
        mTaskDetailPresenter = new TaskDetailPresenter(
                task.getId(), mTasksRepository, mTaskDetailView, mSchedulerProvider);
        mTaskDetailPresenter.deleteTask();

        // Then the repository and the view are notified
        verify(mTasksRepository).deleteTask(task.getId());
        verify(mTaskDetailView).showTaskDeleted();
    }

    @Test
    public void completeTask() {
        // Given an initialized presenter with an active task
        Task task = new Task(TITLE_TEST, DESCRIPTION_TEST);
        mTaskDetailPresenter = new TaskDetailPresenter(
                task.getId(), mTasksRepository, mTaskDetailView, mSchedulerProvider);
        mTaskDetailPresenter.subscribe();

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
        mTaskDetailPresenter = new TaskDetailPresenter(
                task.getId(), mTasksRepository, mTaskDetailView, mSchedulerProvider);
        mTaskDetailPresenter.subscribe();

        // When the presenter is asked to activate the task
        mTaskDetailPresenter.activateTask();

        // Then a request is sent to the task repository and the UI is updated
        verify(mTasksRepository).activateTask(task.getId());
        verify(mTaskDetailView).showTaskMarkedActive();
    }

    @Test
    public void activeTaskIsShownWhenEditing() {
        // When the edit of an ACTIVE_TASK is requested
        mTaskDetailPresenter = new TaskDetailPresenter(
                ACTIVE_TASK.getId(), mTasksRepository, mTaskDetailView, mSchedulerProvider);
        mTaskDetailPresenter.editTask();

        // Then the view is notified
        verify(mTaskDetailView).showEditTask(ACTIVE_TASK.getId());
    }

    @Test
    public void invalidTaskIsNotShownWhenEditing() {
        // When the edit of an invalid task id is requested
        mTaskDetailPresenter = new TaskDetailPresenter(
                INVALID_TASK_ID, mTasksRepository, mTaskDetailView, mSchedulerProvider);
        mTaskDetailPresenter.editTask();

        // Then the edit mode is never started
        verify(mTaskDetailView, never()).showEditTask(INVALID_TASK_ID);
        // instead, the error is shown.
        verify(mTaskDetailView).showMissingTask();
    }

    private void setTaskAvailable(Task task) {
        when(mTasksRepository.getTask(eq(task.getId()))).thenReturn(Observable.just(task));
    }

}
