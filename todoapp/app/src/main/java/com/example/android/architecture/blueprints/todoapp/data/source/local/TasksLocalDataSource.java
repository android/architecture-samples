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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.local.TasksPersistenceContract.TaskEntry;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Concrete implementation of a data source as a db.
 * <p/>
 * Note: this is a singleton and we are opening the database once and not closing it. The framework
 * cleans up the resources when the application closes so we don't need to close the db.
 */
public class TasksLocalDataSource {

    private static TasksLocalDataSource INSTANCE;
    private TasksDbHelper mTasksDbHelper;

    // Prevent direct instantiation.
    private TasksLocalDataSource(@NonNull Context context) {
        checkNotNull(context);
        mTasksDbHelper = new TasksDbHelper(context);
    }

    public static TasksLocalDataSource getInstance(@NonNull Context context) {
        if (INSTANCE == null) {
            INSTANCE = new TasksLocalDataSource(context);
        }
        return INSTANCE;
    }

    public Cursor getTasks(String selection, String[] selectionArgs) {
        Cursor retCursor = mTasksDbHelper.getReadableDatabase().query(
                TasksPersistenceContract.TaskEntry.TABLE_NAME,
                TasksPersistenceContract.TaskEntry.TASKS_COLUMNS,
                selection,
                selectionArgs,
                null,
                null,
                null
        );
        return retCursor;
    }

    public Cursor getTask(@NonNull String taskId) {
        String[] mSelectionArgs = {""};
        mSelectionArgs[0] = taskId;

        Cursor retCursor = mTasksDbHelper.getReadableDatabase().query(
                TasksPersistenceContract.TaskEntry.TABLE_NAME,
                TasksPersistenceContract.TaskEntry.TASKS_COLUMNS,
                TaskEntry.COLUMN_NAME_ENTRY_ID + " = ?",
                mSelectionArgs,
                null,
                null,
                null);
        return retCursor;
    }

    public Uri saveTask(@NonNull Task task) {
        ContentValues values = new ContentValues();
        values.put(TasksPersistenceContract.TaskEntry.COLUMN_NAME_ENTRY_ID, task.getId());
        values.put(TasksPersistenceContract.TaskEntry.COLUMN_NAME_TITLE, task.getTitle());
        values.put(TasksPersistenceContract.TaskEntry.COLUMN_NAME_DESCRIPTION, task.getDescription());
        values.put(TasksPersistenceContract.TaskEntry.COLUMN_NAME_COMPLETED, task.isCompleted() ? 1 : 0);

        final SQLiteDatabase db = mTasksDbHelper.getWritableDatabase();
        Uri returnUri;

        Cursor exists = db.query(
                TasksPersistenceContract.TaskEntry.TABLE_NAME,
                new String[]{TasksPersistenceContract.TaskEntry.COLUMN_NAME_ENTRY_ID},
                TasksPersistenceContract.TaskEntry.COLUMN_NAME_ENTRY_ID + " = ?",
                new String[]{task.getId()},
                null,
                null,
                null
        );
        if (exists.moveToLast()) {
            long _id = db.update(
                    TasksPersistenceContract.TaskEntry.TABLE_NAME, values,
                    TasksPersistenceContract.TaskEntry.COLUMN_NAME_ENTRY_ID + " = ?",
                    new String[]{task.getId()}
            );
            if (_id > 0) {
                returnUri = TasksPersistenceContract.TaskEntry.buildTasksUriWith(_id);
            } else {
                throw new android.database.SQLException("Failed to insert row ");
            }
        } else {
            long _id = db.insert(TasksPersistenceContract.TaskEntry.TABLE_NAME, null, values);
            if (_id > 0) {
                returnUri = TasksPersistenceContract.TaskEntry.buildTasksUriWith(_id);
            } else {
                throw new android.database.SQLException("Failed to insert row");
            }
        }
        exists.close();
        return returnUri;
    }

    public int updateTask(@NonNull ContentValues task, String[] selectionArgs) {
        String selection = TaskEntry.COLUMN_NAME_ENTRY_ID + " LIKE ?";

        final SQLiteDatabase db = mTasksDbHelper.getWritableDatabase();
        int rowsUpdated = db.update(TasksPersistenceContract.TaskEntry.TABLE_NAME, task, selection,
                                    selectionArgs
        );

        return rowsUpdated;
    }

    public int clearCompletedTasks(String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mTasksDbHelper.getWritableDatabase();

        int rowsDeleted = db.delete(
                TasksPersistenceContract.TaskEntry.TABLE_NAME, selection, selectionArgs);

        return rowsDeleted;
    }

    public int deleteAllTasks() {
        final SQLiteDatabase db = mTasksDbHelper.getWritableDatabase();

        int rowsDeleted = db.delete(
                TasksPersistenceContract.TaskEntry.TABLE_NAME, null, null);

        return rowsDeleted;
    }

    public int deleteTask(String[] selectionArgs) {
        String selection = TaskEntry.COLUMN_NAME_ENTRY_ID + " LIKE ?";

        final SQLiteDatabase db = mTasksDbHelper.getWritableDatabase();
        int rowsUpdated = db.delete(TasksPersistenceContract.TaskEntry.TABLE_NAME, selection, selectionArgs);

        return rowsUpdated;
    }

}
