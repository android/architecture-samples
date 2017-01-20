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

package com.example.android.architecture.blueprints.todoapp.tasks;

import android.content.Context;
import android.content.res.Resources;

import com.example.android.architecture.blueprints.todoapp.R;
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
 * Unit tests for the implementation of {@link TasksViewModel}
 */
public class TaskItemViewModelTest {

    private static final String NO_DATA_STRING = "NO_DATA_STRING";

    private static final String NO_DATA_DESC_STRING = "NO_DATA_DESC_STRING";

    @Mock
    private TasksRepository mTasksRepository;

    @Mock
    private Context mContext;

    @Mock
    private TaskItemNavigator mTaskItemNavigator;

    @Captor
    private ArgumentCaptor<TasksDataSource.GetTaskCallback> mLoadTasksCallbackCaptor;

    private TaskItemViewModel mTaskItemViewModel;

    private Task mTask;

    @Before
    public void setupTasksViewModel() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);

        setupContext();

        // Get a reference to the class under test
        mTaskItemViewModel = new TaskItemViewModel(mContext, mTasksRepository, mTaskItemNavigator);

    }

    private void setupContext() {
        when(mContext.getApplicationContext()).thenReturn(mContext);
        when(mContext.getString(R.string.no_data)).thenReturn(NO_DATA_STRING);
        when(mContext.getString(R.string.no_data_description)).thenReturn(NO_DATA_DESC_STRING);

        when(mContext.getResources()).thenReturn(mock(Resources.class));
    }

    @Test
    public void clickOnTask_ShowsDetailUi() {
        loadTaskIntoViewModel();

        mLoadTasksCallbackCaptor.getValue().onTaskLoaded(mTask); // Trigger callback

        // Then task detail UI is shown
        assertEquals(mTaskItemViewModel.getTitle(), mTask.getTitle());
        assertEquals(mTaskItemViewModel.getDescription(), mTask.getDescription());
    }

    @Test
    public void nullTask_showsNoData() {
        loadTaskIntoViewModel();

        mLoadTasksCallbackCaptor.getValue().onTaskLoaded(null); // Trigger callback

        // Then task detail UI is shown
        assertEquals(mTaskItemViewModel.getTitle(), NO_DATA_STRING);
        assertEquals(mTaskItemViewModel.getDescription(), NO_DATA_DESC_STRING);
    }

    @Test
    public void completeTask_ShowsTaskMarkedComplete() {
        loadTaskIntoViewModel();

        mLoadTasksCallbackCaptor.getValue().onTaskLoaded(mTask); // Trigger callback

        // When task is marked as complete
        mTaskItemViewModel.setCompleted(true);

        // Then repository is called
        verify(mTasksRepository).completeTask(mTask);
    }

    @Test
    public void activateTask_ShowsTaskMarkedActive() {
        loadTaskIntoViewModel();

        mLoadTasksCallbackCaptor.getValue().onTaskLoaded(mTask); // Trigger callback

        // When task is marked as complete
        mTaskItemViewModel.setCompleted(false);

        // Then repository is called
        verify(mTasksRepository).activateTask(mTask);
    }

    @Test
    public void unavailableTasks_ShowsError() {
        loadTaskIntoViewModel();

        mLoadTasksCallbackCaptor.getValue().onDataNotAvailable(); // Trigger callback

        // Then repository is called
        assertFalse(mTaskItemViewModel.isDataAvailable());
    }

    private void loadTaskIntoViewModel() {
        // Given a stubbed active task
        mTask = new Task("Details Requested", "For this task");

        // When open task details is requested
        mTaskItemViewModel.start(mTask.getId());

        // Use a captor to get a reference for the callback.
        verify(mTasksRepository).getTask(eq(mTask.getId()), mLoadTasksCallbackCaptor.capture());
    }
}
