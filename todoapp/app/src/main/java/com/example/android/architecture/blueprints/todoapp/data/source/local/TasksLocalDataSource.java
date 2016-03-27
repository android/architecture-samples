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

package com.example.android.architecture.blueprints.todoapp.data.source.local;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.support.annotation.NonNull;

import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource;
import com.example.android.architecture.blueprints.todoapp.data.source.local.TasksPersistenceContract.TaskEntry;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Concrete implementation of a data source as a db.
 * <p/>
 * Note: this is a singleton and we are opening the database once and not closing it. The framework
 * cleans up the resources when the application closes so we don't need to close the db.
 */
public class TasksLocalDataSource implements TasksDataSource {

    private static TasksLocalDataSource INSTANCE;
    private ContentResolver mContentResolver;

    // Prevent direct instantiation.
    private TasksLocalDataSource(@NonNull Context context) {
        checkNotNull(context);

        mContentResolver = context.getContentResolver();
    }

    public static TasksLocalDataSource getInstance(@NonNull Context context) {
        if (INSTANCE == null) {
            INSTANCE = new TasksLocalDataSource(context);
        }
        return INSTANCE;
    }

    @Override
    public List<Task> getTasks() {
        // we don't need this since the CursorLoader talks to the
        // ContentResolver directly
        return null;
    }

    /**
     * Note: {@link GetTaskCallback#onDataNotAvailable()} is fired if the {@link Task} isn't
     * found.
     */
    @Override
    public Task getTask(@NonNull String taskId) {
        // we don't need this since the CursorLoader talks to the
        // ContentResolver directly
        return null;
    }

    @Override
    public void saveTask(@NonNull Task task) {
        try {
            checkNotNull(task);

            ContentValues values = new ContentValues();
            values.put(TaskEntry.COLUMN_NAME_ENTRY_ID, task.getId());
            values.put(TaskEntry.COLUMN_NAME_TITLE, task.getTitle());
            values.put(TaskEntry.COLUMN_NAME_DESCRIPTION, task.getDescription());
            values.put(TaskEntry.COLUMN_NAME_COMPLETED, task.isCompleted());

            mContentResolver.insert(TasksPersistenceContract.TaskEntry.buildTasksUri(), values);

        } catch (IllegalStateException e) {
            // Send to analytics, log etc
        }
    }

    @Override
    public void completeTask(@NonNull Task task) {
        try {
            ContentValues values = new ContentValues();
            values.put(TaskEntry.COLUMN_NAME_COMPLETED, true);

            String selection = TaskEntry.COLUMN_NAME_ENTRY_ID + " LIKE ?";
            String[] selectionArgs = {task.getId()};

            mContentResolver.update(TasksPersistenceContract.TaskEntry.buildTasksUri(), values, selection, selectionArgs);
        } catch (IllegalStateException e) {
            // Send to analytics, log etc
        }

    }

    @Override
    public void completeTask(@NonNull String taskId) {
        // Not required for the local data source because the {@link TasksRepository} handles
        // converting from a {@code taskId} to a {@link task} using its cached data.
    }

    @Override
    public void activateTask(@NonNull Task task) {
        try {
            ContentValues values = new ContentValues();
            values.put(TaskEntry.COLUMN_NAME_COMPLETED, false);

            String selection = TaskEntry.COLUMN_NAME_ENTRY_ID + " LIKE ?";
            String[] selectionArgs = {task.getId()};

            mContentResolver.update(TasksPersistenceContract.TaskEntry.buildTasksUri(), values, selection, selectionArgs);
        } catch (IllegalStateException e) {
            // Send to analytics, log etc
        }
    }

    @Override
    public void activateTask(@NonNull String taskId) {
        // Not required for the local data source because the {@link TasksRepository} handles
        // converting from a {@code taskId} to a {@link task} using its cached data.
    }

    @Override
    public void clearCompletedTasks() {
        try {
            String selection = TaskEntry.COLUMN_NAME_COMPLETED + " LIKE ?";
            String[] selectionArgs = {"1"};
            mContentResolver.delete(TasksPersistenceContract.TaskEntry.buildTasksUri(), selection, selectionArgs);
        } catch (IllegalStateException e) {
            // Send to analytics, log etc
        }
    }

    @Override
    public void refreshTasks() {
        // Not required because the {@link TasksRepository} handles the logic of refreshing the
        // tasks from all the available data sources.
    }

    @Override
    public void deleteAllTasks() {
        try {
            mContentResolver.delete(TasksPersistenceContract.TaskEntry.buildTasksUri(), null, null);
        } catch (IllegalStateException e) {
            // Send to analytics, log etc
        }
    }

    @Override
    public void deleteTask(@NonNull String taskId) {
        try {
            String selection = TaskEntry.COLUMN_NAME_ENTRY_ID + " LIKE ?";
            String[] selectionArgs = {taskId};

            mContentResolver.delete(TasksPersistenceContract.TaskEntry.buildTasksUri(), selection, selectionArgs);
        } catch (IllegalStateException e) {
            // Send to analytics, log etc
        }
    }
}
