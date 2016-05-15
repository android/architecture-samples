package com.example.android.architecture.blueprints.todoapp.data.source;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;

import com.example.android.architecture.blueprints.todoapp.BuildConfig;
import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.local.TasksPersistenceContract;

public class TasksInteractor {

    public final static int TASKS_LOADER = 1;
    public final static int TASK_LOADER = 2;
    public final static int EDIT_TASK_LOADER = 3;

    public final static String KEY_TASK_FILTER = BuildConfig.APPLICATION_ID + "TASK_FILTER";
    public final static String KEY_TASK_ID = BuildConfig.APPLICATION_ID + "TASK_ID";
    private static TasksInteractor INSTANCE;
    private final LoaderManager mLoaderManager;
    private final ContentResolver mContentResolver;

    private TasksInteractor(LoaderManager mLoaderManager, ContentResolver contentResolver) {
        this.mLoaderManager = mLoaderManager;
        this.mContentResolver = contentResolver;
    }

    public static TasksInteractor getInstance(LoaderManager mLoaderManager, ContentResolver mContentResolver) {
        if (INSTANCE == null) {
            INSTANCE = new TasksInteractor(mLoaderManager, mContentResolver);
        }
        return INSTANCE;
    }

    public void getTasks(final Bundle extras, LoaderManager.LoaderCallbacks<Cursor> callback) {
        if (mLoaderManager.getLoader(TasksInteractor.TASKS_LOADER) == null) {
            mLoaderManager.initLoader(TasksInteractor.TASKS_LOADER, extras, callback);
        } else {
            mLoaderManager.restartLoader(TasksInteractor.TASKS_LOADER, extras, callback);
        }
    }

    public void getTask(String taskId, LoaderManager.LoaderCallbacks<Cursor> callback) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(TasksInteractor.KEY_TASK_ID, taskId);

        if (mLoaderManager.getLoader(TASK_LOADER) == null) {
            mLoaderManager.initLoader(TASK_LOADER, bundle, callback);
        } else {
            mLoaderManager.restartLoader(TASK_LOADER, bundle, callback);
        }
    }

    public void completeTask(Task task) {
        try {
            ContentValues values = TaskValues.from(task);
            values.put(TasksPersistenceContract.TaskEntry.COLUMN_NAME_COMPLETED, 1);

            String selection = TasksPersistenceContract.TaskEntry.COLUMN_NAME_ENTRY_ID + " LIKE ?";
            String[] selectionArgs = {task.getId()};

            mContentResolver.update(TasksPersistenceContract.TaskEntry.buildTasksUri(), values, selection, selectionArgs);
        } catch (IllegalStateException e) {
            // Send to analytics, log etc
        }
    }

    public void activateTask(Task activeTask) {
        try {
            ContentValues values = TaskValues.from(activeTask);
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

    public void saveTask(Task newTask) {
        ContentValues values = TaskValues.from(newTask);
        mContentResolver.insert(TasksPersistenceContract.TaskEntry.buildTasksUri(), values);
    }

    public void deleteTask(Task deletedTask) {
        String selection = TasksPersistenceContract.TaskEntry.COLUMN_NAME_ENTRY_ID + " LIKE ?";
        String[] selectionArgs = {deletedTask.getId()};

        mContentResolver.delete(TasksPersistenceContract.TaskEntry.buildTasksUri(), selection, selectionArgs);
    }


}
