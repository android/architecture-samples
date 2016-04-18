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
import com.example.android.architecture.blueprints.todoapp.data.source.TaskLoader;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksOperations;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Listens to user actions from the UI ({@link AddEditTaskFragment}), retrieves the data and updates
 * the UI as required.
 */
public class AddEditTaskPresenter implements AddEditTaskContract.Presenter,
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final int TASK_QUERY = 2;

    @NonNull
    private TasksOperations tasksOperations;

    @NonNull
    private AddEditTaskContract.View mAddTaskView;

    @Nullable
    private String mTaskId;

    private TaskLoader mTaskLoader;

    private final LoaderManager mLoaderManager;

    public AddEditTaskPresenter(@Nullable String taskId, @NonNull TasksOperations tasksOperations,
                                @NonNull AddEditTaskContract.View addTaskView, @NonNull TaskLoader taskLoader,
                                @NonNull LoaderManager loaderManager) {
        mTaskId = taskId;
        tasksOperations = checkNotNull(tasksOperations);
        mAddTaskView = checkNotNull(addTaskView);
        mTaskLoader = checkNotNull(taskLoader);
        mLoaderManager = checkNotNull(loaderManager, "loaderManager cannot be null!");

        mAddTaskView.setPresenter(this);
    }

    @Override
    public void start() {
        mLoaderManager.initLoader(TASK_QUERY, null, this);
    }

    @Override
    public void createTask(String title, String description) {
        Task newTask = new Task(title, description);
        if (newTask.isEmpty()) {
            mAddTaskView.showEmptyTaskError();
        } else {
            tasksOperations.saveTask(newTask);
            mAddTaskView.showTasksList();
        }
    }

    @Override
    public void updateTask(String taskId, String title, String description) {
        tasksOperations.saveTask(new Task(title, description, taskId));
        mAddTaskView.showTasksList(); // After an edit, go back to the list.
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (mTaskId == null) {
            return null;
        }
        return mTaskLoader;
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
}
