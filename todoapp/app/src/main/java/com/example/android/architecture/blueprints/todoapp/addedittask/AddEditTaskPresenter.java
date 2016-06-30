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

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.LoaderProvider;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;

import static com.google.common.base.Preconditions.checkNotNull;


/**
 * Listens to user actions from the UI ({@link AddEditTaskFragment}), retrieves the data and updates
 * the UI as required.
 */
public class AddEditTaskPresenter implements AddEditTaskContract.Presenter,
        LoaderManager.LoaderCallbacks<Cursor>, TasksDataSource.GetTaskCallback {

    public final static int EDIT_TASK_LOADER = 3;

    @NonNull
    private final LoaderProvider mLoaderProvider;

    @NonNull
    private final LoaderManager mLoaderManager;

    @NonNull
    private TasksRepository mTasksRepository;

    @NonNull
    private AddEditTaskContract.View mAddTaskView;

    @Nullable
    private String mTaskId;

    public AddEditTaskPresenter(@Nullable String taskId, @NonNull TasksRepository tasksRepository,
                                @NonNull AddEditTaskContract.View addTaskView, @NonNull LoaderProvider loaderProvider,
                                @NonNull LoaderManager loaderManager) {
        mTaskId = taskId;
        mTasksRepository = checkNotNull(tasksRepository);
        mAddTaskView = checkNotNull(addTaskView);
        mLoaderProvider = checkNotNull(loaderProvider);
        mLoaderManager = checkNotNull(loaderManager, "loaderManager cannot be null!");

        mAddTaskView.setPresenter(this);
    }

    @Override
    public void start() {
        if (!isNewTask()) {
            populateTask();
        }
    }

    @Override
    public void saveTask(String title, String description) {
        if (isNewTask()) {
            createTask(title, description);
        } else {
            updateTask(title, description);
        }
    }

    @Override
    public void populateTask() {
        if (isNewTask()) {
            throw new RuntimeException("populateTask() was called but task is new.");
        }
        mTasksRepository.getTask(mTaskId, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return mLoaderProvider.createTaskLoader(mTaskId);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToLast()) {
            Task task = Task.from(data);
            mAddTaskView.setDescription(task.getDescription());
            mAddTaskView.setTitle(task.getTitle());
        } else {
            // NO-OP, add mode.
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private boolean isNewTask() {
        return mTaskId == null;
    }

    private void createTask(String title, String description) {
        Task newTask = new Task(title, description);
        if (newTask.isEmpty()) {
            mAddTaskView.showEmptyTaskError();
        } else {
            mTasksRepository.saveTask(newTask);
            mAddTaskView.showTasksList();
        }
    }

    private void updateTask(String title, String description) {
        if (isNewTask()) {
            throw new RuntimeException("updateTask() was called but task is new.");
        }
        mTasksRepository.saveTask(new Task(title, description, mTaskId));
        mAddTaskView.showTasksList(); // After an edit, go back to the list.
    }

    @Override
    public void onTaskLoaded(Task task) {
        // we don't need this result since the CursorLoader will load it for us
        mLoaderManager.initLoader(EDIT_TASK_LOADER, null, this);
    }

    @Override
    public void onDataNotAvailable() {
        mAddTaskView.showEmptyTaskError();
    }
}
