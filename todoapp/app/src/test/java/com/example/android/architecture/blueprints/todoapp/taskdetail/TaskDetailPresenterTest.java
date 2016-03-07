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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;

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

    @Mock
    private TasksRepository mTasksRepository;

    @Mock
    private TasksDataSource.GetTaskCallback mRepositoryCallback;

    @Mock
    private TasksDataSource.GetTaskCallback mPresenterCallback;

    @Mock
    private TaskDetailContract.View mView;

    /**
     * {@link ArgumentCaptor} is a powerful Mockito API to capture argument values and use them to
     * perform further actions or assertions on them.
     */
    @Captor
    private ArgumentCaptor<TasksDataSource.GetTaskCallback> mGetTaskCallbackCaptor;

    private TaskDetailPresenter mTaskDetailPresenter;

    private Task mTask;


    @Before
    public void setupTasksPresenter() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);

        mTask = new Task(TITLE_TEST, DESCRIPTION_TEST);

        // Get a reference to the class under test
        mTaskDetailPresenter = new TaskDetailPresenter(mTask.getId(), mTasksRepository, mView);

        // The presenter won't update the view unless it's active.
        when(mView.isActive()).thenReturn(true);
    }

    @Test
    public void getActiveTaskFromRepositoryAndLoadIntoView() {
        setupPresenterRepositoryCallback();

        // When task is finally loaded
        mGetTaskCallbackCaptor.getValue().onTaskLoaded(mTask); // Trigger callback

        // Then verify that the view was notified
        verify(mView).showTask(mTask);
    }

    @Test
    public void deleteTask() {
        // When the deletion of a task is requested
        mTaskDetailPresenter.deleteTask();

        // Then the repository is notified
        verify(mTasksRepository).deleteTask(mTask.getId());
    }

    @Test
    public void completeTask() {
        // When the presenter is asked to complete the task
        mTaskDetailPresenter.completeChanged(mTask, true);

        // Then a request is sent to the task repository and the UI is updated
        verify(mTasksRepository).completeTask(mTask);
        assertThat(mTask.isCompleted(), is(true));
    }

    @Test
    public void activateTask() {
        // When the presenter is asked to complete the task
        mTaskDetailPresenter.completeChanged(mTask, false);

        // Then a request is sent to the task repository and the UI is updated
        verify(mTasksRepository).activateTask(mTask);
        assertThat(mTask.isCompleted(), is(false));
    }

    @Test
    public void TaskDetailPresenter_repositoryError() {
        setupPresenterRepositoryCallback();

        // When the repository returns an error
        mGetTaskCallbackCaptor.getValue().onDataNotAvailable(); // Trigger callback error

        // Then verify that the view was notified
        verify(mView).showError();
    }

    @Test
    public void TaskDetailPresenter_repositoryNull() {
        setupPresenterRepositoryCallback();

        // When the repository returns a null task
        mGetTaskCallbackCaptor.getValue().onTaskLoaded(null); // Trigger callback error

        // Then verify that the view was notified
        verify(mView).showError();
    }

    private void setupPresenterRepositoryCallback() {
        // Given an initialized presenter with an active task
        mPresenterCallback = mock(TasksDataSource.GetTaskCallback.class);

        mTaskDetailPresenter.getTask();

        // Use a captor to get a reference for the callback.
        verify(mTasksRepository).getTask(eq(mTask.getId()), mGetTaskCallbackCaptor.capture());
    }
}
