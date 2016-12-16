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

import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource.LoadTasksCallback;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;
import com.google.common.collect.Lists;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for the implementation of {@link TasksViewModel}
 */
public class TasksViewModelTest {

    private static List<Task> TASKS;

    @Mock
    private TasksRepository mTasksRepository;

    @Mock
    private Context mContext;

    @Mock
    private TasksNavigator mTasksNavigator;

    /**3
     * {@link ArgumentCaptor} is a powerful Mockito API to capture argument values and use them to
     * perform further actions or assertions on them.
     */
    @Captor
    private ArgumentCaptor<LoadTasksCallback> mLoadTasksCallbackCaptor;

    private TasksViewModel mTasksViewModel;


    @Before
    public void setupTasksViewModel() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);

        // Get a reference to the class under test
        mTasksViewModel = new TasksViewModel(
                mTasksRepository, mContext, mTasksNavigator);

        // We initialise the tasks to 3, with one active and two completed
        TASKS = Lists.newArrayList(new Task("Title1", "Description1"),
                new Task("Title2", "Description2", true), new Task("Title3", "Description3", true));
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
        assertTrue(mTasksViewModel.dataLoading.get());
        mLoadTasksCallbackCaptor.getValue().onTasksLoaded(TASKS);

        // Then progress indicator is hidden
        assertFalse(mTasksViewModel.dataLoading.get());

        // And data loaded
        assertFalse(mTasksViewModel.items.isEmpty());
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
        assertFalse(mTasksViewModel.dataLoading.get());

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
        assertFalse(mTasksViewModel.dataLoading.get());

        // And data loaded
        assertFalse(mTasksViewModel.items.isEmpty());
        assertTrue(mTasksViewModel.items.size() == 2);
    }

    @Test
    public void clickOnFab_ShowsAddTaskUi() {
        // When adding a new task
        mTasksViewModel.addNewTask();

        // Then the navigator is called
        verify(mTasksNavigator).addNewTask();
    }

    @Test
    public void clearCompletedTasks_ClearsTasks() {
        // When completed tasks are cleared
        mTasksViewModel.clearCompletedTasks();

        // Then repository is called and the view is notified
        verify(mTasksRepository).clearCompletedTasks();
        verify(mTasksRepository).getTasks(any(LoadTasksCallback.class));
    }
}
