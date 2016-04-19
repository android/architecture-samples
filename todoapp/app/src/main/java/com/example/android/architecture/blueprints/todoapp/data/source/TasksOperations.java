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

    private static TasksOperations INSTANCE;

    private final static int TASKS_LOADER = 1;
    private final static int TASK_LOADER = 2;

    public final static String KEY_TASK_FILTER = BuildConfig.APPLICATION_ID + "TASK_FILTER";
    public final static String KEY_TASK_ID = BuildConfig.APPLICATION_ID + "TASK_ID";

    private final LoaderProvider mLoaderProvider;
    private final LoaderManager mLoaderManager;
    private final ContentResolver mContentResolver;

    private GetTasksCallback callback;

    public static TasksOperations getInstance(LoaderProvider mLoaderProvider, LoaderManager mLoaderManager, ContentResolver mContentResolver) {
        if (INSTANCE == null) {
            INSTANCE = new TasksOperations(mLoaderProvider, mLoaderManager, mContentResolver);
        }
        return INSTANCE;
    }

    private TasksOperations(LoaderProvider mLoaderProvider, LoaderManager mLoaderManager, ContentResolver contentResolver) {
        this.mLoaderProvider = mLoaderProvider;
        this.mLoaderManager = mLoaderManager;
        this.mContentResolver = contentResolver;
    }

    public void getTasks(Bundle extras, GetTasksCallback callback) {
        this.callback = callback;
        if (mLoaderManager.getLoader(TASKS_LOADER) == null) {
            mLoaderManager.initLoader(TASKS_LOADER, extras, this);
        } else {
            mLoaderManager.restartLoader(TASKS_LOADER, extras, this);
        }
    }

    public void getTask(String taskId, GetTasksCallback callback) {
        this.callback = callback;

        Bundle bundle = new Bundle();
        bundle.putSerializable(TasksOperations.KEY_TASK_ID, taskId);

        if (mLoaderManager.getLoader(TASK_LOADER) == null) {
            mLoaderManager.initLoader(TASK_LOADER, null, this);
        } else {
            mLoaderManager.restartLoader(TASK_LOADER, null, this);
        }
    }

    public void completeTask(Task task) {
        completeTask(task.getId());
    }

    public void completeTask(String completedTaskId) {
        try {
            ContentValues values = new ContentValues();
            values.put(TasksPersistenceContract.TaskEntry.COLUMN_NAME_ENTRY_ID, newTask.getId());
            values.put(TasksPersistenceContract.TaskEntry.COLUMN_NAME_TITLE, newTask.getTitle());
            values.put(TasksPersistenceContract.TaskEntry.COLUMN_NAME_DESCRIPTION, newTask.getDescription());
            values.put(TasksPersistenceContract.TaskEntry.COLUMN_NAME_COMPLETED, true);

            String selection = TasksPersistenceContract.TaskEntry.COLUMN_NAME_ENTRY_ID + " LIKE ?";
            String[] selectionArgs = {completedTaskId};

            mContentResolver.update(TasksPersistenceContract.TaskEntry.buildTasksUri(), values, selection, selectionArgs);
        } catch (IllegalStateException e) {
            // Send to analytics, log etc
        }
    }

    public void activateTask(Task activeTask) {
        activateTask(activeTask.getId());
    }

    public void activateTask(String taskId) {
        try {
            ContentValues values = new ContentValues();
            values.put(TasksPersistenceContract.TaskEntry.COLUMN_NAME_COMPLETED, false);

            String selection = TasksPersistenceContract.TaskEntry.COLUMN_NAME_ENTRY_ID + " LIKE ?";
            String[] selectionArgs = {taskId};

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

    public void saveTask(Task newTask) {
        ContentValues values = new ContentValues();
        values.put(TasksPersistenceContract.TaskEntry.COLUMN_NAME_ENTRY_ID, newTask.getId());
        values.put(TasksPersistenceContract.TaskEntry.COLUMN_NAME_TITLE, newTask.getTitle());
        values.put(TasksPersistenceContract.TaskEntry.COLUMN_NAME_DESCRIPTION, newTask.getDescription());
        values.put(TasksPersistenceContract.TaskEntry.COLUMN_NAME_COMPLETED, newTask.isCompleted() ? 1 : 0);
        mContentResolver.insert(TasksPersistenceContract.TaskEntry.buildTasksUri(), values);
    }

    public void deleteTask(String taskId) {
        String selection = TasksPersistenceContract.TaskEntry.COLUMN_NAME_ENTRY_ID + " LIKE ?";
        String[] selectionArgs = {taskId};

        mContentResolver.delete(TasksPersistenceContract.TaskEntry.buildTasksUri(), selection, selectionArgs);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case TASKS_LOADER:
                TasksFilterType tasksFilterType = (TasksFilterType) args.getSerializable(KEY_TASK_FILTER);
                return mLoaderProvider.createFilteredTasksLoader(tasksFilterType);
            break;
            case TASK_LOADER:
                String taskId = args.getString(KEY_TASK_ID);
                return mLoaderProvider.createTaskLoader(taskId);
            break;
        }

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null) {
            callback.onDataLoaded(data);
        } else {
            callback.onDataNotAvailable();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public interface GetTasksCallback {
        void onDataLoaded(Cursor data);
        void onDataNotAvailable();
    }
}
