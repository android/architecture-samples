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


import android.content.Context;

import com.example.android.architecture.blueprints.todoapp.data.Priority;
import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for the implementation of {@link AddEditTaskViewModel}.
 */
public class AddEditTaskViewModelTest {

    public static final String SNACKBAR_TEXT = "Snackbar text";
    @Mock
    private TasksRepository mTasksRepository;

    /**
     * {@link ArgumentCaptor} is a powerful Mockito API to capture argument values and use them to
     * perform further actions or assertions on them.
     */
    @Captor
    private ArgumentCaptor<TasksDataSource.GetTaskCallback> mGetTaskCallbackCaptor;

    private AddEditTaskViewModel mAddEditTaskViewModel;

    @Before
    public void setupAddEditTaskViewModel() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);

        // Get a reference to the class under test
        mAddEditTaskViewModel = new AddEditTaskViewModel(
                mock(Context.class), mTasksRepository);
        mAddEditTaskViewModel.onActivityCreated(mock(AddEditTaskActivity.class));
    }

    @Test
    public void saveNewTaskToRepository_showsSuccessMessageUi() {
        // When the ViewModel is asked to save a task
        mAddEditTaskViewModel.description.set("Some Task Description");
        mAddEditTaskViewModel.title.set("New Task Title");
        mAddEditTaskViewModel.saveTask();

        // Then a task is saved in the repository and the view updated
        verify(mTasksRepository).saveTask(any(Task.class)); // saved to the model
    }

    @Test
    public void populateTask_callsRepoAndUpdatesView() {
        Task testTask = new Task("TITLE", "DESCRIPTION", Priority.NONE, "1");

        // Get a reference to the class under test
        mAddEditTaskViewModel = new AddEditTaskViewModel(
                mock(Context.class), mTasksRepository);
        mAddEditTaskViewModel.onActivityCreated(mock(AddEditTaskActivity.class));


        // When the ViewModel is asked to populate an existing task
        mAddEditTaskViewModel.start(testTask.getId());

        // Then the task repository is queried and the view updated
        verify(mTasksRepository).getTask(eq(testTask.getId()), mGetTaskCallbackCaptor.capture());

        // Simulate callback
        mGetTaskCallbackCaptor.getValue().onTaskLoaded(testTask);

        // Verify the fields were updated
        assertThat(mAddEditTaskViewModel.title.get(), is(testTask.getTitle()));
        assertThat(mAddEditTaskViewModel.description.get(), is(testTask.getDescription()));
    }

    @Test
    public void updateSnackbar_nullValue() {
        // Get a reference to the class under test
        mAddEditTaskViewModel = new AddEditTaskViewModel(
                mock(Context.class), mTasksRepository);
        mAddEditTaskViewModel.onActivityCreated(mock(AddEditTaskActivity.class));

        // Before setting the Snackbar text, get its current value
        String snackbarText = mAddEditTaskViewModel.getSnackbarText();

        // Check that the value is null
        assertThat("Snackbar text does not match", snackbarText, is(nullValue()));
    }

    @Test
    public void updateSnackbar_nonNullValue() {
        // Get a reference to the class under test
        mAddEditTaskViewModel = new AddEditTaskViewModel(
                mock(Context.class), mTasksRepository);
        mAddEditTaskViewModel.onActivityCreated(mock(AddEditTaskActivity.class));

        // Set a new value for the Snackbar text via the public Observable
        mAddEditTaskViewModel.snackbarText.set(SNACKBAR_TEXT);

        // Get its current value with the Snackbar text getter
        String snackbarText = mAddEditTaskViewModel.getSnackbarText();

        // Check that the value matches the observable's.
        assertThat("Snackbar text does not match", snackbarText, is(SNACKBAR_TEXT));
    }
}
