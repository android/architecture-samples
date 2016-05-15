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
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TaskValues;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource;
import com.example.android.architecture.blueprints.todoapp.data.source.local.TasksPersistenceContract.TaskEntry;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Concrete implementation of a data source as a db.
 */
public class TasksLocalDataSource implements TasksDataSource {

    private static TasksLocalDataSource INSTANCE;

    private ContentResolver mContentResolver;

    // Prevent direct instantiation.
    private TasksLocalDataSource(@NonNull ContentResolver contentResolver) {
        checkNotNull(contentResolver);
        mContentResolver = contentResolver;
    }

    public static TasksLocalDataSource getInstance(@NonNull ContentResolver contentResolver) {
        if (INSTANCE == null) {
            INSTANCE = new TasksLocalDataSource(contentResolver);
        }
        return INSTANCE;
    }

    @Override
    public void getTasks(@NonNull GetTasksCallback callback) {

    }

    @Override
    public void getTask(@NonNull String taskId, @NonNull GetTaskCallback callback) {

    }

    public void saveTask(@NonNull Task task) {
        checkNotNull(task);

        ContentValues values = TaskValues.from(task);
        mContentResolver.insert(TasksPersistenceContract.TaskEntry.buildTasksUri(), values);
    }

    public void completeTask(@NonNull Task task) {
        checkNotNull(task);

        ContentValues values = new ContentValues();
        values.put(TasksPersistenceContract.TaskEntry.COLUMN_NAME_COMPLETED, 1);

        String selection = TasksPersistenceContract.TaskEntry.COLUMN_NAME_ENTRY_ID + " LIKE ?";
        String[] selectionArgs = {task.getId()};

        mContentResolver.update(TasksPersistenceContract.TaskEntry.buildTasksUri(), values, selection, selectionArgs);
    }

    @Override
    public void completeTask(@NonNull String taskId) {
        checkNotNull(taskId);

        ContentValues values = new ContentValues();
        values.put(TasksPersistenceContract.TaskEntry.COLUMN_NAME_COMPLETED, 1);

        String selection = TasksPersistenceContract.TaskEntry.COLUMN_NAME_ENTRY_ID + " LIKE ?";
        String[] selectionArgs = {taskId};

        mContentResolver.update(TasksPersistenceContract.TaskEntry.buildTasksUri(), values, selection, selectionArgs);
    }

    public void activateTask(@NonNull Task task) {
        checkNotNull(task);

        ContentValues values = new ContentValues();
        values.put(TasksPersistenceContract.TaskEntry.COLUMN_NAME_COMPLETED, false);

        String selection = TasksPersistenceContract.TaskEntry.COLUMN_NAME_ENTRY_ID + " LIKE ?";
        String[] selectionArgs = {task.getId()};

        mContentResolver.update(TasksPersistenceContract.TaskEntry.buildTasksUri(), values, selection, selectionArgs);
    }

    @Override
    public void activateTask(@NonNull String taskId) {
        checkNotNull(taskId);

        ContentValues values = new ContentValues();
        values.put(TasksPersistenceContract.TaskEntry.COLUMN_NAME_COMPLETED, false);

        String selection = TasksPersistenceContract.TaskEntry.COLUMN_NAME_ENTRY_ID + " LIKE ?";
        String[] selectionArgs = {taskId};

        mContentResolver.update(TasksPersistenceContract.TaskEntry.buildTasksUri(), values, selection, selectionArgs);
    }

    public void clearCompletedTasks() {
        String selection = TasksPersistenceContract.TaskEntry.COLUMN_NAME_COMPLETED + " LIKE ?";
        String[] selectionArgs = {"1"};

        mContentResolver.delete(TasksPersistenceContract.TaskEntry.buildTasksUri(), selection, selectionArgs);
    }

    public void deleteAllTasks() {
        mContentResolver.delete(TasksPersistenceContract.TaskEntry.buildTasksUri(), null, null);
    }

    public void deleteTask(@NonNull String taskId) {
        String selection = TasksPersistenceContract.TaskEntry.COLUMN_NAME_ENTRY_ID + " LIKE ?";
        String[] selectionArgs = {taskId};

        mContentResolver.delete(TasksPersistenceContract.TaskEntry.buildTasksUri(), selection, selectionArgs);
    }
}
