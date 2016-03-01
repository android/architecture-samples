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

package com.example.android.architecture.blueprints.todoapp.addedittask;

import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TaskLoader;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the implementation of {@link AddEditTaskPresenter}.
 */
public class AddEditTaskPresenterTest {

    @Mock
    private TasksRepository mTasksRepository;

    @Mock
    private AddEditTaskContract.View mAddEditTaskView;

    @Mock
    private TaskLoader mTaskLoader;

    @Mock
    private LoaderManager mLoaderManager;

    /**
     * {@link ArgumentCaptor} is a powerful Mockito API to capture argument values and use them to
     * perform further actions or assertions on them.
     */
    @Captor
    private ArgumentCaptor<TasksDataSource.GetTaskCallback> mGetTaskCallbackCaptor;

    private AddEditTaskPresenter mAddEditTaskPresenter;

    @Before
    public void setupAddEditTaskPresenter() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);

        // Get a reference to the class under test
        mAddEditTaskPresenter = new AddEditTaskPresenter(null, mTasksRepository, mAddEditTaskView,
                mTaskLoader, mLoaderManager);
    }

    @Test
    public void saveNewTaskToRepository_showsSuccessMessageUi() {
        // When the presenter is asked to save a task
        mAddEditTaskPresenter.createTask("New Task Title", "Some Task Description");

        // Then a task is saved in the repository and the view updated
        verify(mTasksRepository).saveTask(any(Task.class)); // saved to the model
        verify(mAddEditTaskView).showTasksList(); // shown in the UI
    }

    @Test
    public void saveTask_emptyTaskShowsErrorUi() {
        // When the presenter is asked to save an empty task
        mAddEditTaskPresenter.createTask("", "");

        // Then an empty not error is shown in the UI
        verify(mAddEditTaskView).showEmptyTaskError();
    }

    @Test
    public void saveExistingTaskToRepository_showsSuccessMessageUi() {
        // When the presenter is asked to save an existing task
        mAddEditTaskPresenter.updateTask("1", "New Task Title", "Some Task Description");

        // Then a task is saved in the repository and the view updated
        verify(mTasksRepository).saveTask(any(Task.class)); // saved to the model
        verify(mAddEditTaskView).showTasksList(); // shown in the UI
    }

    @Test
    public void populateTask_callsRepoAndUpdatesView() {
        Task testTask = new Task("TITLE", "DESCRIPTION");

        when(mTasksRepository.getTask(testTask.getId())).thenReturn(testTask);


        // When the presenter is asked to populate an existing task
        mAddEditTaskPresenter.onLoadFinished(mock(Loader.class), testTask);

        // Then the task repository is queried and the view updated
        //verify(mTasksRepository).getTask(eq(testTask.getId()));

        verify(mAddEditTaskView).setTitle(testTask.getTitle());
        verify(mAddEditTaskView).setDescription(testTask.getDescription());
    }
}
