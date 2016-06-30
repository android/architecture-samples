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

package com.example.android.architecture.blueprints.todoapp.data.source;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import com.example.android.architecture.blueprints.todoapp.data.source.local.TasksPersistenceContract;
import com.example.android.architecture.blueprints.todoapp.tasks.TaskFilter;

import static com.google.common.base.Preconditions.checkNotNull;

public class LoaderProvider {

    @NonNull
    private final Context mContext;

    public LoaderProvider(@NonNull Context context) {
        mContext = checkNotNull(context, "context cannot be null");
    }

    public Loader<Cursor> createFilteredTasksLoader(TaskFilter taskFilter) {
        String selection = null;
        String[] selectionArgs = null;

        switch (taskFilter.getTasksFilterType()) {
            case ALL_TASKS:
                selection = null;
                selectionArgs = null;
                break;
            case ACTIVE_TASKS:
                selection = TasksPersistenceContract.TaskEntry.COLUMN_NAME_COMPLETED + " = ? ";
                selectionArgs = new String[]{String.valueOf(0)};
                break;
            case COMPLETED_TASKS:
                selection = TasksPersistenceContract.TaskEntry.COLUMN_NAME_COMPLETED + " = ? ";
                selectionArgs = new String[]{String.valueOf(1)};
                break;
        }

        return new CursorLoader(
                mContext,
                TasksPersistenceContract.TaskEntry.buildTasksUri(),
                TasksPersistenceContract.TaskEntry.TASKS_COLUMNS, selection, selectionArgs, null
        );
    }

    public Loader<Cursor> createTaskLoader(String taskId) {
        return new CursorLoader(mContext, TasksPersistenceContract.TaskEntry.buildTasksUriWith(taskId),
                                null,
                                null,
                                new String[]{String.valueOf(taskId)}, null
        );
    }
}
