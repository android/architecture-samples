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


import android.content.Context;
import android.content.res.Resources;

import com.example.android.architecture.blueprints.todoapp.TaskViewModel;
import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the implementation of {@link TaskDetailViewModel}
 */
public class TaskDetailViewModelTest {

    private static final String TITLE_TEST = "title";

    private static final String DESCRIPTION_TEST = "description";

    @Mock
    private TasksRepository mTasksRepository;

    @Mock
    private Context mContext;

    @Mock
    private TasksDataSource.GetTaskCallback mRepositoryCallback;

    @Mock
    private TasksDataSource.GetTaskCallback mViewModelCallback;

    @Captor
    private ArgumentCaptor<TasksDataSource.GetTaskCallback> mGetTaskCallbackCaptor;

    private TaskViewModel mTaskDetailViewModel;

    private Task mTask;

    @Before
    public void setupTasksViewModel() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);

        mTask = new Task(TITLE_TEST, DESCRIPTION_TEST);

        // Get a reference to the class under test
        mTaskDetailViewModel = new TaskDetailViewModel(
                mContext, mTasksRepository, mock(TaskDetailNavigator.class));
    }

    @Before
    public void setupContext() {
        when(mContext.getResources()).thenReturn(mock(Resources.class));
    }

    @Test
    public void getActiveTaskFromRepositoryAndLoadIntoView() {
        setupViewModelRepositoryCallback();

        // Then verify that the view was notified
        assertEquals(mTaskDetailViewModel.getTitle(), mTask.getTitle());
        assertEquals(mTaskDetailViewModel.getDescription(), mTask.getDescription());
    }

    @Test
    public void deleteTask() {
        setupViewModelRepositoryCallback();

        // When the deletion of a task is requested
        mTaskDetailViewModel.deleteTask();

        // Then the repository is notified
        verify(mTasksRepository).deleteTask(mTask.getId());
    }

    @Test
    public void completeTask() {
        setupViewModelRepositoryCallback();

        // When the ViewModel is asked to complete the task
        mTaskDetailViewModel.setCompleted(true);

        // Then a request is sent to the task repository and the UI is updated
        verify(mTasksRepository).completeTask(mTask);
    }

    @Test
    public void activateTask() {
        setupViewModelRepositoryCallback();

        // When the ViewModel is asked to complete the task
        mTaskDetailViewModel.setCompleted(false);

        // Then a request is sent to the task repository and the UI is updated
        verify(mTasksRepository).activateTask(mTask);
    }

    @Test
    public void TaskDetailViewModel_repositoryError() {
        // Given an initialized ViewModel with an active task
        mViewModelCallback = mock(TasksDataSource.GetTaskCallback.class);

        mTaskDetailViewModel.start(mTask.getId());

        // Use a captor to get a reference for the callback.
        verify(mTasksRepository).getTask(eq(mTask.getId()), mGetTaskCallbackCaptor.capture());

        // When the repository returns an error
        mGetTaskCallbackCaptor.getValue().onDataNotAvailable(); // Trigger callback error

        // Then verify that data is not available
        assertFalse(mTaskDetailViewModel.isDataAvailable());
    }

    @Test
    public void TaskDetailViewModel_repositoryNull() {
        setupViewModelRepositoryCallback();

        // When the repository returns a null task
        mGetTaskCallbackCaptor.getValue().onTaskLoaded(null); // Trigger callback error

        // Then verify that data is not available
        assertFalse(mTaskDetailViewModel.isDataAvailable());
    }

    private void setupViewModelRepositoryCallback() {
        // Given an initialized ViewModel with an active task
        mViewModelCallback = mock(TasksDataSource.GetTaskCallback.class);

        mTaskDetailViewModel.start(mTask.getId());

        // Use a captor to get a reference for the callback.
        verify(mTasksRepository).getTask(eq(mTask.getId()), mGetTaskCallbackCaptor.capture());

        mGetTaskCallbackCaptor.getValue().onTaskLoaded(mTask); // Trigger callback
    }
}
