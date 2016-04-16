package com.example.android.architecture.blueprints.todoapp.data.source;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import com.example.android.architecture.blueprints.todoapp.BuildConfig;
import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.local.TasksPersistenceContract;
import com.example.android.architecture.blueprints.todoapp.tasks.TasksFilterType;

public class TasksOperations implements LoaderManager.LoaderCallbacks<Cursor> {

    private final static int TASKS_QUERY = 1;
    public final static String KEY_TASK_FILTER = BuildConfig.APPLICATION_ID + "TASK_FILTER";

    private final LoaderProvider mLoaderProvider;
    private final LoaderManager mLoaderManager;
    private final ContentResolver mContentResolver;

    private GetTasksCallback callback;

    public TasksOperations(LoaderProvider mLoaderProvider, LoaderManager mLoaderManager, ContentResolver contentResolver) {
        this.mLoaderProvider = mLoaderProvider;
        this.mLoaderManager = mLoaderManager;
        this.mContentResolver = contentResolver;
    }

    public void getTasks(Bundle extras, GetTasksCallback callback) {
        this.callback = callback;
        if (mLoaderManager.getLoader(TASKS_QUERY) == null) {
            mLoaderManager.initLoader(TASKS_QUERY, extras, this);
        } else {
            mLoaderManager.restartLoader(TASKS_QUERY, extras, this);
        }
    }

    public void completeTask(Task completedTask) {
        try {
            ContentValues values = new ContentValues();
            values.put(TasksPersistenceContract.TaskEntry.COLUMN_NAME_COMPLETED, true);

            String selection = TasksPersistenceContract.TaskEntry.COLUMN_NAME_ENTRY_ID + " LIKE ?";
            String[] selectionArgs = {completedTask.getId()};

            mContentResolver.update(TasksPersistenceContract.TaskEntry.buildTasksUri(), values, selection, selectionArgs);
        } catch (IllegalStateException e) {
            // Send to analytics, log etc
        }
    }

    public void activateTask(Task activeTask) {
        try {
            ContentValues values = new ContentValues();
            values.put(TasksPersistenceContract.TaskEntry.COLUMN_NAME_COMPLETED, false);

            String selection = TasksPersistenceContract.TaskEntry.COLUMN_NAME_ENTRY_ID + " LIKE ?";
            String[] selectionArgs = {activeTask.getId()};

            mContentResolver.update(TasksPersistenceContract.TaskEntry.buildTasksUri(), values, selection, selectionArgs);
        } catch (IllegalStateException e) {
            // Send to analytics, log etc
        }
    }

    public void clearCompletedTasks() {
        try {
            String selection = TasksPersistenceContract.TaskEntry.COLUMN_NAME_COMPLETED + " LIKE ?";
            String[] selectionArgs = {"1"};
            mContentResolver.delete(TasksPersistenceContract.TaskEntry.buildTasksUri(), selection, selectionArgs);
        } catch (IllegalStateException e) {
            // Send to analytics, log etc
        }
    }

    public void deleteAllTasks() {
        try {
            mContentResolver.delete(TasksPersistenceContract.TaskEntry.buildTasksUri(), null, null);
        } catch (IllegalStateException e) {
            // Send to analytics, log etc
        }
    }

    public Task getTask(String id) {
        return null;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        TasksFilterType tasksFilterType = (TasksFilterType) args.getSerializable(KEY_TASK_FILTER);
        return mLoaderProvider.createFilteredTasksLoader(tasksFilterType);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null) {
            callback.onTasksLoaded(data);
        } else {
            callback.onDataNotAvailable();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public interface GetTasksCallback {
        void onTasksLoaded(Cursor data);

        void onDataNotAvailable();
    }
}
