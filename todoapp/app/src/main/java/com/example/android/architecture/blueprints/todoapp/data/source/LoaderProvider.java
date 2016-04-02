package com.example.android.architecture.blueprints.todoapp.data.source;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import com.example.android.architecture.blueprints.todoapp.data.source.local.TasksPersistenceContract;
import com.example.android.architecture.blueprints.todoapp.tasks.TasksFilterType;

import static com.google.common.base.Preconditions.checkNotNull;

public class LoaderProvider {

    @NonNull
    private final Context mContext;

    public LoaderProvider(@NonNull Context context) {
        mContext = checkNotNull(context, "context cannot be null");
    }

    public Loader<Cursor> createFilteredTasksLoader(TasksFilterType filterType) {
        String selection = null;
        String[] selectionArgs = null;

        switch (filterType) {
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
        return new CursorLoader(mContext, TasksPersistenceContract.TaskEntry.buildTasksUri(),
                                TasksPersistenceContract.TaskEntry.TASKS_COLUMNS,
                                TasksPersistenceContract.TaskEntry.COLUMN_NAME_ENTRY_ID + " = ? ",
                                new String[]{String.valueOf(taskId)}, null
        );
    }
}
