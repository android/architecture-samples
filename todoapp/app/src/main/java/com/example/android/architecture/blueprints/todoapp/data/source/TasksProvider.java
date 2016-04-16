package com.example.android.architecture.blueprints.todoapp.data.source;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.support.annotation.Nullable;

import com.example.android.architecture.blueprints.todoapp.Injection;
import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.local.TasksLocalDataSource;
import com.example.android.architecture.blueprints.todoapp.data.source.local.TasksPersistenceContract;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TasksProvider extends ContentProvider {

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private TasksDataSource mTasksRemoteDataSource;
    private TasksLocalDataSource mTasksLocalDataSource;

    private static final int TASK = 100;
    private static final int TASK_ITEM = 101;

    /**
     * This variable has package local visibility so it can be accessed from tests.
     */
    Map<String, Task> mCachedTasks;

    @Override
    public boolean onCreate() {
        mTasksRemoteDataSource = Injection.provideRemoteDataSource();
        mTasksLocalDataSource = Injection.provideLocalDataSource(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor tasks;
        if (mCachedTasks != null) {
            tasks = getCachedTasks();
        } else {
            tasks = mTasksLocalDataSource.getTasks(selection, selectionArgs);
            if (null == tasks || tasks.getCount() == 0) {
                List<Task> taskList = mTasksRemoteDataSource.getTasks();
                saveTasksInLocalDataSource(taskList);
            }
        }
        tasks.setNotificationUri(getContext().getContentResolver(), uri);
        return tasks;
    }

    private MatrixCursor getCachedTasks() {
        MatrixCursor matrixCursor = new MatrixCursor(TasksPersistenceContract.TaskEntry.TASKS_COLUMNS);

        if (mCachedTasks == null) {
            return matrixCursor;
        } else {
            for (int i = 0; i < mCachedTasks.size(); i++) {
                matrixCursor.addRow(new Object[]{
                        mCachedTasks.get(i).getInternalId(),
                        mCachedTasks.get(i).getId(),
                        mCachedTasks.get(i).getTitle(),
                        mCachedTasks.get(i).getDescription(),
                        mCachedTasks.get(i).isCompleted()
                });

            }
        }
        return matrixCursor;

    }

    private void saveTasksInLocalDataSource(List<Task> tasks) {
        if (tasks != null) {
            for (Task task : tasks) {
                mTasksLocalDataSource.saveTask(task);
            }
        }
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case TASK:
                return TasksPersistenceContract.CONTENT_TASK_TYPE;
            case TASK_ITEM:
                return TasksPersistenceContract.CONTENT_TASK_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Task newTask = Task.from(values);
        mTasksRemoteDataSource.saveTask(newTask);
        Uri returnUri = mTasksLocalDataSource.saveTask(newTask);
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int rowsDeleted;

        if (selectionArgs.equals("1")) {
            mTasksRemoteDataSource.clearCompletedTasks();
            rowsDeleted = mTasksLocalDataSource.clearCompletedTasks(selection, selectionArgs);
            // Do in memory cache update to keep the app UI up to date
            if (mCachedTasks == null) {
                mCachedTasks = new LinkedHashMap<>();
            }
            Iterator<Map.Entry<String, Task>> it = mCachedTasks.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Task> entry = it.next();
                if (entry.getValue().isCompleted()) {
                    it.remove();
                }
            }
        } else {
            mTasksRemoteDataSource.deleteAllTasks();
            rowsDeleted = mTasksLocalDataSource.deleteAllTasks();

            if (mCachedTasks == null) {
                mCachedTasks = new LinkedHashMap<>();
            }
            mCachedTasks.clear();
        }

        if (selection == null || rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        Task newTask = Task.from(values);
        if (newTask.isCompleted()) {
            mTasksRemoteDataSource.completeTask(newTask);
        } else {
            mTasksRemoteDataSource.activateTask(newTask);
        }

        int rowsUpdated = mTasksLocalDataSource.updateTask(values, selectionArgs);

        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = TasksPersistenceContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, TasksPersistenceContract.TaskEntry.TABLE_NAME, TASK);
        matcher.addURI(authority, TasksPersistenceContract.TaskEntry.TABLE_NAME + "/*", TASK_ITEM);

        return matcher;
    }

}
