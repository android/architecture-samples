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
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the implementation of {@link TaskDetailPresenter}
 */
public class TaskDetailPresenterTest {

    public static final String INVALID_ID = "INVALID_ID";

    public static final String TITLE_TEST = "title";

    public static final String DESCRIPTION_TEST = "description";

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
    public void setupTasksPresenter() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);

        // Get a reference to the class under test
        mTaskDetailPresenter = new TaskDetailPresenter(mTasksRepository, mTaskDetailView);

        // The presenter won't update the view unless it's active.
        when(mTaskDetailView.isActive()).thenReturn(true);
    }

    @Test
    public void getActiveTaskFromRepositoryAndLoadIntoView() {
        // Given an initialized TaskDetailPresenter with stubbed task
        Task task = new Task(TITLE_TEST, DESCRIPTION_TEST);

        // When tasks presenter is asked to open a task
        mTaskDetailPresenter.openTask(task.getId());

        // Then task is loaded from model, callback is captured and progress indicator is shown
        verify(mTasksRepository).getTask(eq(task.getId()), mGetTaskCallbackCaptor.capture());
        verify(mTaskDetailView).setProgressIndicator(true);

        // When task is finally loaded
        mGetTaskCallbackCaptor.getValue().onTaskLoaded(task); // Trigger callback

        // Then progress indicator is hidden and title, description and completion status are shown
        // in UI
        verify(mTaskDetailView).setProgressIndicator(false);
        verify(mTaskDetailView).showTitle(TITLE_TEST);
        verify(mTaskDetailView).showDescription(DESCRIPTION_TEST);
        verify(mTaskDetailView).showCompletionStatus(false);
    }

    @Test
    public void getCompletedTaskFromRepositoryAndLoadIntoView() {
        // Given an initialized TaskDetailPresenter with stubbed task
        Task task = new Task(TITLE_TEST, DESCRIPTION_TEST, true);

        // When tasks presenter is asked to open a task
        mTaskDetailPresenter.openTask(task.getId());

        // Then task is loaded from model, callback is captured and progress indicator is shown
        verify(mTasksRepository).getTask(eq(task.getId()), mGetTaskCallbackCaptor.capture());
        verify(mTaskDetailView).setProgressIndicator(true);

        // When task is finally loaded
        mGetTaskCallbackCaptor.getValue().onTaskLoaded(task); // Trigger callback

        // Then progress indicator is hidden and title, description and completion status are shown
        // in UI
        verify(mTaskDetailView).setProgressIndicator(false);
        verify(mTaskDetailView).showTitle(TITLE_TEST);
        verify(mTaskDetailView).showDescription(DESCRIPTION_TEST);
        verify(mTaskDetailView).showCompletionStatus(true);
    }

    @Test
    public void getUnknownTaskFromRepositoryAndLoadIntoView() {
        // When loading of a task is requested with an invalid task ID.
        mTaskDetailPresenter.openTask(INVALID_ID);

        // Then task with invalid id is attempted to load from model, callback is captured and
        // progress indicator is shown.
        verify(mTaskDetailView).setProgressIndicator(true);
        verify(mTasksRepository).getTask(eq(INVALID_ID), mGetTaskCallbackCaptor.capture());

        // When task is finally loaded
        mGetTaskCallbackCaptor.getValue().onTaskLoaded(null); // Trigger callback

        // Then progress indicator is hidden and missing task UI is shown
        verify(mTaskDetailView).setProgressIndicator(false);
        verify(mTaskDetailView).showMissingTask();
    }

    @Test
    public void deleteTask() {
        // Given an initialized TaskDetailPresenter with stubbed task
        Task task = new Task(TITLE_TEST, DESCRIPTION_TEST);

        // When the deletion of a task is requested
        mTaskDetailPresenter.deleteTask(task.getId());

        // Then the repository and the view are notified
        verify(mTasksRepository).deleteTask(task.getId());
        verify(mTaskDetailView).showTaskDeleted();
    }

    public void completeTask() {
        // Given an initialized presenter with an active task
        Task task = new Task(TITLE_TEST, DESCRIPTION_TEST);
        mTaskDetailPresenter.openTask(task.getId());

        // When the presenter is asked to complete the task
        mTaskDetailPresenter.completeTask(task.getId());

        // Then a request is sent to the task repository and the UI is updated
        verify(mTasksRepository).completeTask(task.getId());
        verify(mTaskDetailView).showTaskMarkedComplete();
    }

    @Test
    public void activateTask() {
        // Given an initialized presenter with a completed task
        Task task = new Task(TITLE_TEST, DESCRIPTION_TEST, true);
        mTaskDetailPresenter.openTask(task.getId());

        // When the presenter is asked to activate the task
        mTaskDetailPresenter.activateTask(task.getId());

        // Then a request is sent to the task repository and the UI is updated
        verify(mTasksRepository).activateTask(task.getId());
        verify(mTaskDetailView).showTaskMarkedActive();
    }
}
