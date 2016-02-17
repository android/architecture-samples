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

package com.example.android.architecture.blueprints.todomvploaders.tasks;

import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import com.example.android.architecture.blueprints.todomvploaders.data.Task;
import com.example.android.architecture.blueprints.todomvploaders.data.source.TasksLoader;
import com.example.android.architecture.blueprints.todomvploaders.data.source.TasksRepository;
import com.google.common.collect.Lists;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for the implementation of {@link TasksPresenter}.
 */
public class TasksPresenterTest {

    private static List<Task> TASKS;

    @Mock
    private TasksRepository mTasksRepository;

    @Mock
    private TasksContract.View mTasksView;

    /**
     * {@link ArgumentCaptor} is a powerful Mockito API to capture argument values and use them to
     * perform further actions or assertions on them.
     */
    @Captor
    private ArgumentCaptor<List> mShowTasksArgumentCaptor;

    @Mock
    private TasksLoader mTasksLoader;

    @Mock
    private LoaderManager mLoaderManager;

    private TasksPresenter mTasksPresenter;

    @Before
    public void setupTasksPresenter() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);

        // Get a reference to the class under test
        mTasksPresenter = new TasksPresenter(mTasksLoader, mTasksRepository, mTasksView, mLoaderManager, TaskFilterType.ALL_TASKS);

        // We initialise the tasks to 3, with one active and two completed
        TASKS = Lists.newArrayList(new Task("Title1", "Description1"),
                new Task("Title2", "Description2", true), new Task("Title3", "Description3", true));
    }

    @Test
    public void loadAllTasksFromRepositoryAndLoadIntoView() {
        // When the loader finishes with tasks and filter is set to all
        mTasksPresenter.setFilterType(TaskFilterType.ALL_TASKS);
        mTasksPresenter.onLoadFinished(mock(Loader.class), TASKS);

        // Then progress indicator is hidden and all tasks are shown in UI
        verify(mTasksView).setProgressIndicator(false);
        verify(mTasksView).showTasks(mShowTasksArgumentCaptor.capture());
        assertThat(mShowTasksArgumentCaptor.getValue().size(), is(3));
    }

    @Test
    public void loadActiveTasksFromRepositoryAndLoadIntoView() {
        // When the loader finishes with tasks and filter is set to active
        mTasksPresenter.setFilterType(TaskFilterType.ACTIVE_TASKS);
        mTasksPresenter.onLoadFinished(mock(Loader.class), TASKS);

        // Then progress indicator is hidden and active tasks are shown in UI
        verify(mTasksView).setProgressIndicator(false);
        verify(mTasksView).showTasks(mShowTasksArgumentCaptor.capture());
        assertThat(mShowTasksArgumentCaptor.getValue().size(), is(1));
    }

    @Test
    public void loadCompletedTasksFromRepositoryAndLoadIntoView() {
        // When the loader finishes with tasks and filter is set to completed
        mTasksPresenter.setFilterType(TaskFilterType.COMPLETED_TASKS);
        mTasksPresenter.onLoadFinished(mock(Loader.class), TASKS);

        // Then progress indicator is hidden and completed tasks are shown in UI
        verify(mTasksView).setProgressIndicator(false);
        verify(mTasksView).showTasks(mShowTasksArgumentCaptor.capture());
        assertThat(mShowTasksArgumentCaptor.getValue().size(), is(2));
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
        verify(mTasksView).showTaskDetailsUi(any(String.class));
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
        // When the loader finishes with error
        mTasksPresenter.setFilterType(TaskFilterType.COMPLETED_TASKS);
        mTasksPresenter.onLoadFinished(mock(Loader.class), null);

        // Then an error message is shown
        verify(mTasksView).showLoadingTasksError();
    }
}
