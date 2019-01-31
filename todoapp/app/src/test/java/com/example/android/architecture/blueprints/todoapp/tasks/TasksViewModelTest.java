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

import static com.example.android.architecture.blueprints.todoapp.R.string.successfully_deleted_task_message;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.app.Application;
import android.content.res.Resources;

import com.example.android.architecture.blueprints.todoapp.R;
import com.example.android.architecture.blueprints.todoapp.TestUtils;
import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskActivity;
import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource.LoadTasksCallback;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;
import com.example.android.architecture.blueprints.todoapp.taskdetail.TaskDetailActivity;
import com.google.common.collect.Lists;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;

/**
 * Unit tests for the implementation of {@link TasksViewModel}
 */
public class TasksViewModelTest {

    // Executes each task synchronously using Architecture Components.
    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

    private static List<Task> TASKS;

    @Mock
    private TasksRepository mTasksRepository;

    @Mock
    private Application mContext;

    @Captor
    private ArgumentCaptor<LoadTasksCallback> mLoadTasksCallbackCaptor;

    private TasksViewModel mTasksViewModel;

    @Before
    public void setupTasksViewModel() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);

        setupContext();

        // Get a reference to the class under test
        mTasksViewModel = new TasksViewModel(mContext, mTasksRepository);

        // We initialise the tasks to 3, with one active and two completed
        TASKS = Lists.newArrayList(new Task("Title1", "Description1"),
                new Task("Title2", "Description2", true), new Task("Title3", "Description3", true));

        mTasksViewModel.getSnackbarMessage().removeObservers(TestUtils.TEST_OBSERVER);

    }

    private void setupContext() {
        when(mContext.getApplicationContext()).thenReturn(mContext);
        when(mContext.getString(R.string.successfully_saved_task_message))
                .thenReturn("EDIT_RESULT_OK");
        when(mContext.getString(R.string.successfully_added_task_message))
                .thenReturn("ADD_EDIT_RESULT_OK");
        when(mContext.getString(successfully_deleted_task_message))
                .thenReturn("DELETE_RESULT_OK");

        when(mContext.getResources()).thenReturn(mock(Resources.class));
    }

    @Test
    public void loadAllTasksFromRepository_dataLoaded() {
        // Given an initialized TasksViewModel with initialized tasks
        // When loading of Tasks is requested
        mTasksViewModel.setFiltering(TasksFilterType.ALL_TASKS);
        mTasksViewModel.loadTasks(true);

        // Callback is captured and invoked with stubbed tasks
        verify(mTasksRepository).getTasks(mLoadTasksCallbackCaptor.capture());


        // Then progress indicator is shown
        assertTrue(mTasksViewModel.mDataLoading.getValue());
        mLoadTasksCallbackCaptor.getValue().onTasksLoaded(TASKS);

        // Then progress indicator is hidden
        assertFalse(mTasksViewModel.mDataLoading.getValue());

        // And data loaded
        assertFalse(mTasksViewModel.items.getValue().isEmpty());
        assertTrue(mTasksViewModel.items.size() == 3);
    }

    @Test
    public void loadActiveTasksFromRepositoryAndLoadIntoView() {
        // Given an initialized TasksViewModel with initialized tasks
        // When loading of Tasks is requested
        mTasksViewModel.setFiltering(TasksFilterType.ACTIVE_TASKS);
        mTasksViewModel.loadTasks(true);

        // Callback is captured and invoked with stubbed tasks
        verify(mTasksRepository).getTasks(mLoadTasksCallbackCaptor.capture());
        mLoadTasksCallbackCaptor.getValue().onTasksLoaded(TASKS);

        // Then progress indicator is hidden
        assertFalse(mTasksViewModel.mDataLoading.getValue());

        // And data loaded
        assertFalse(mTasksViewModel.items.isEmpty());
        assertTrue(mTasksViewModel.items.size() == 1);
    }

    @Test
    public void loadCompletedTasksFromRepositoryAndLoadIntoView() {
        // Given an initialized TasksViewModel with initialized tasks
        // When loading of Tasks is requested
        mTasksViewModel.setFiltering(TasksFilterType.COMPLETED_TASKS);
        mTasksViewModel.loadTasks(true);

        // Callback is captured and invoked with stubbed tasks
        verify(mTasksRepository).getTasks(mLoadTasksCallbackCaptor.capture());
        mLoadTasksCallbackCaptor.getValue().onTasksLoaded(TASKS);

        // Then progress indicator is hidden
        assertFalse(mTasksViewModel.mDataLoading.getValue());

        // And data loaded
        assertFalse(mTasksViewModel.items.isEmpty());
        assertTrue(mTasksViewModel.items.size() == 2);
    }

    @Test
    public void clickOnFab_ShowsAddTaskUi() {

        Observer<Void> observer = mock(Observer.class);

        mTasksViewModel.getNewTaskEvent().observe(TestUtils.TEST_OBSERVER, observer);

        // When adding a new task
        mTasksViewModel.addNewTask();

        // Then the event is triggered
        verify(observer).onChanged(null);
    }

    @Test
    public void clearCompletedTasks_ClearsTasks() {
        // When completed tasks are cleared
        mTasksViewModel.clearCompletedTasks();

        // Then repository is called and the view is notified
        verify(mTasksRepository).clearCompletedTasks();
        verify(mTasksRepository).getTasks(any(LoadTasksCallback.class));
    }

    @Test
    public void handleActivityResult_editOK() {
        // When TaskDetailActivity sends a EDIT_RESULT_OK
        Observer<Integer> observer = mock(Observer.class);

        mTasksViewModel.getSnackbarMessage().observe(TestUtils.TEST_OBSERVER, observer);

        mTasksViewModel.handleActivityResult(
                AddEditTaskActivity.REQUEST_CODE, TaskDetailActivity.EDIT_RESULT_OK);

        // Then the snackbar shows the correct message
        verify(observer).onChanged(R.string.successfully_saved_task_message);
    }

    @Test
    public void handleActivityResult_addEditOK() {
        // When TaskDetailActivity sends a EDIT_RESULT_OK
        Observer<Integer> observer = mock(Observer.class);

        mTasksViewModel.getSnackbarMessage().observe(TestUtils.TEST_OBSERVER, observer);

        // When AddEditTaskActivity sends a ADD_EDIT_RESULT_OK
        mTasksViewModel.handleActivityResult(
                AddEditTaskActivity.REQUEST_CODE, AddEditTaskActivity.ADD_EDIT_RESULT_OK);

        // Then the snackbar shows the correct message
        verify(observer).onChanged(R.string.successfully_added_task_message);
    }

    @Test
    public void handleActivityResult_deleteOk() {
        // When TaskDetailActivity sends a EDIT_RESULT_OK
        Observer<Integer> observer = mock(Observer.class);

        mTasksViewModel.getSnackbarMessage().observe(TestUtils.TEST_OBSERVER, observer);

        // When AddEditTaskActivity sends a ADD_EDIT_RESULT_OK
        mTasksViewModel.handleActivityResult(
                AddEditTaskActivity.REQUEST_CODE, TaskDetailActivity.DELETE_RESULT_OK);

        // Then the snackbar shows the correct message
        verify(observer).onChanged(R.string.successfully_deleted_task_message);
    }

    @Test
    public void getTasksAddViewVisible() {
        // When the filter type is ALL_TASKS
        mTasksViewModel.setFiltering(TasksFilterType.ALL_TASKS);

        // Then the "Add task" action is visible
        assertThat(mTasksViewModel.tasksAddViewVisible.getValue(), is(true));
    }
}
