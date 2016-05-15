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

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;

import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.LoaderProvider;
import com.example.android.architecture.blueprints.todoapp.data.source.MockCursorProvider;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksInteractor;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the implementation of {@link TasksPresenter}.
 */
public class TasksPresenterTest {

    /**
     * {@link ArgumentCaptor} is a powerful Mockito API to capture argument values and use them to
     * perform further actions or assertions on them.
     */
    @Captor
    private ArgumentCaptor<Cursor> mShowTasksArgumentCaptor;

    @Mock
    private LoaderProvider mLoaderProvider;

    @Mock
    private TasksContract.View mTasksView;

    @Mock
    private TasksRepository mTasksRepository;

    @Mock
    private LoaderManager mLoaderManager;

    @Mock
    private Bundle mBundle;

    private MockCursorProvider.TaskMockCursor mCompletedTasksCursor;
    private MockCursorProvider.TaskMockCursor mActiveTasksCursor;
    private MockCursorProvider.TaskMockCursor mAllTasksCursor;

    private TasksPresenter mTasksPresenter;

    @Before
    public void setupTasksPresenter() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);

        // Given a task filter
        when(mBundle.getSerializable(TasksInteractor.KEY_TASK_FILTER)).thenReturn(TasksFilterType.ALL_TASKS);
        TaskFilter taskFilter = new TaskFilter(mBundle);

        // Get a reference to the class under test
        mTasksPresenter = new TasksPresenter(mLoaderProvider, mLoaderManager, mTasksRepository, mTasksView, taskFilter);

        mCompletedTasksCursor = MockCursorProvider.createCompletedTasksCursor();
        mActiveTasksCursor = MockCursorProvider.createActiveTasksCursor();
        mAllTasksCursor = MockCursorProvider.createAllTasksCursor();
    }

    @Test
    public void loadAllTasksFromRepositoryAndLoadIntoView() {
        // When the loader finishes with tasks and filter is set to all
        when(mBundle.getSerializable(TasksInteractor.KEY_TASK_FILTER)).thenReturn(TasksFilterType.ALL_TASKS);
        TaskFilter taskFilter = new TaskFilter(mBundle);

        mTasksPresenter.setFiltering(taskFilter);
        mTasksPresenter.onDataLoaded(mAllTasksCursor);

        // Then progress indicator is hidden and all tasks are shown in UI
        verify(mTasksView).setLoadingIndicator(false);
        verify(mTasksView).showTasks(mShowTasksArgumentCaptor.capture());
        assertThat(mShowTasksArgumentCaptor.getValue().getCount(), is(5));
    }

    @Test
    public void loadActiveTasksFromRepositoryAndLoadIntoView() {
        when(mBundle.getSerializable(TasksInteractor.KEY_TASK_FILTER)).thenReturn(TasksFilterType.ACTIVE_TASKS);
        TaskFilter taskFilter = new TaskFilter(mBundle);

        // When the loader finishes with tasks and filter is set to active
        mTasksPresenter.setFiltering(taskFilter);
        mTasksPresenter.onDataLoaded(mActiveTasksCursor);

        // Then progress indicator is hidden and active tasks are shown in UI
        verify(mTasksView).setLoadingIndicator(false);
        verify(mTasksView).showTasks(mShowTasksArgumentCaptor.capture());
        assertThat(mShowTasksArgumentCaptor.getValue().getCount(), is(2));
    }

    @Test
    public void loadCompletedTasksFromRepositoryAndLoadIntoView() {
        when(mBundle.getSerializable(TasksInteractor.KEY_TASK_FILTER)).thenReturn(TasksFilterType.COMPLETED_TASKS);
        TaskFilter taskFilter = new TaskFilter(mBundle);

        // When the loader finishes with tasks and filter is set to completed
        mTasksPresenter.setFiltering(taskFilter);
        mTasksPresenter.onDataLoaded(mCompletedTasksCursor);

        // Then progress indicator is hidden and completed tasks are shown in UI
        verify(mTasksView).setLoadingIndicator(false);
        verify(mTasksView).showTasks(mShowTasksArgumentCaptor.capture());
        assertThat(mShowTasksArgumentCaptor.getValue().getCount(), is(3));
    }

    @Test
    public void clickOnFab_ShowsAddTaskUi() {
        // When adding a new task
        mTasksPresenter.addNewTask();

        // Then add task UI is shown
        verify(mTasksView).showAddTask();
    }

    @Test
    public void clickOnTask_ShowsDetailUi() {
        // Given a stubbed active task
        Task requestedTask = new Task("Details Requested", "For this task");

        // When open task details is requested
        mTasksPresenter.openTaskDetails(requestedTask);

        // Then task detail UI is shown
        verify(mTasksView).showTaskDetailsUi(requestedTask);
    }

    @Test
    public void completeTask_ShowsTaskMarkedComplete() {
        // Given a stubbed task
        Task task = new Task("Details Requested", "For this task");

        // When task is marked as complete
        mTasksPresenter.completeTask(task);

        // Then repository is called and task marked complete UI is shown
        verify(mTasksRepository).completeTask(task);
        verify(mTasksView).showTaskMarkedComplete();
    }

    @Test
    public void activateTask_ShowsTaskMarkedActive() {
        // Given a stubbed completed task
        Task task = new Task("Details Requested", "For this task", true);

        // When task is marked as activated
        mTasksPresenter.activateTask(task);

        // Then repository is called and task marked active UI is shown
        verify(mTasksRepository).activateTask(task);
        verify(mTasksView).showTaskMarkedActive();
    }

    @Test
    public void unavailableTasks_ShowsError() {
        when(mBundle.getSerializable(TasksInteractor.KEY_TASK_FILTER)).thenReturn(TasksFilterType.ALL_TASKS);
        TaskFilter taskFilter = new TaskFilter(mBundle);

        // When the loader finishes with error
        mTasksPresenter.setFiltering(taskFilter);
        mTasksPresenter.onDataNotAvailable();

        // Then an error message is shown
        verify(mTasksView).showLoadingTasksError();
    }
}
