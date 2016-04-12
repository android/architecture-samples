package com.example.android.architecture.blueprints.todoapp.data.source;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import com.example.android.architecture.blueprints.todoapp.BuildConfig;
import com.example.android.architecture.blueprints.todoapp.tasks.TasksFilterType;

public class TasksOperations implements LoaderManager.LoaderCallbacks<Cursor> {

    private final static int TASKS_QUERY = 1;
    public final static String KEY_TASK_FILTER = BuildConfig.APPLICATION_ID + "TASK_FILTER";

    private final LoaderProvider mLoaderProvider;
    private final LoaderManager mLoaderManager;
    private GetTasksCallback callback;

    public TasksOperations(LoaderProvider mLoaderProvider, LoaderManager mLoaderManager) {
        this.mLoaderProvider = mLoaderProvider;
        this.mLoaderManager = mLoaderManager;
    }

    public void getTasks(Bundle extras, GetTasksCallback callback) {
        this.callback = callback;

        if (mLoaderManager.getLoader(TASKS_QUERY) == null) {
            mLoaderManager.initLoader(TASKS_QUERY, extras, this);
        } else {
            mLoaderManager.restartLoader(TASKS_QUERY, extras, this);
        }
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
