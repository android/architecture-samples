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

        if (null == selection) {
            mTasksRemoteDataSource.deleteAllTasks();
            rowsDeleted = mTasksLocalDataSource.deleteAllTasks();
            clearCache();

        } else if (selectionArgs.equals("1")) {
            mTasksRemoteDataSource.clearCompletedTasks();
            rowsDeleted = mTasksLocalDataSource.clearCompletedTasks();

            Iterator<Map.Entry<String, Task>> it = mCachedTasks.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Task> entry = it.next();
                if (entry.getValue().isCompleted()) {
                    it.remove();
                }
            }
        } else {
            String taskId = selectionArgs[0];
            mTasksRemoteDataSource.deleteTask(taskId);
            rowsDeleted = mTasksLocalDataSource.deleteTask(taskId);

            Iterator<Map.Entry<String, Task>> it = mCachedTasks.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Task> entry = it.next();
                if (entry.getValue().getId().equals(taskId)) {
                    it.remove();
                }
            }
        }

        if (selection == null || rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        Task newTask = Task.from(values);
        int rowsUpdated;
        if (newTask.isCompleted()) {
            mTasksRemoteDataSource.completeTask(newTask);
            rowsUpdated = mTasksLocalDataSource.completeTask(newTask);
        } else {
            mTasksRemoteDataSource.activateTask(newTask);
            rowsUpdated = mTasksLocalDataSource.activateTask(newTask);
        }

//        mCachedTasks.put(newTask.getId(), newTask);

        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor tasks;
        switch (sUriMatcher.match(uri)) {
            case TASK:
                tasks = mTasksLocalDataSource.getTasks(selection, selectionArgs);
                break;
            case TASK_ITEM:
                tasks = mTasksLocalDataSource.getTask(uri.getLastPathSegment());
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        tasks.setNotificationUri(getContext().getContentResolver(), uri);
        return tasks;

//            if (hasCachedTasks()) {
//                tasks = getCachedTasks(selection, selectionArgs);
//            } else {
//                tasks = mTasksLocalDataSource.getTasks(selection, selectionArgs);
//                if (null == tasks || tasks.getCount() == 0) {
//                    List<Task> taskList = mTasksRemoteDataSource.getTasks();
//                    saveTasksInLocalDataSource(taskList);
//                } else {
//                    saveToLocalCache(tasks);
//                }
//            }
    }

    private boolean hasCachedTasks() {
        if (null == mCachedTasks) {
            return false;
        } else {
            return mCachedTasks.size() > 0;
        }
    }

    private Cursor getCachedTask(String[] selectionArgs) {
        MatrixCursor matrixCursor = new MatrixCursor(new String[]{

                TasksPersistenceContract.TaskEntry.COLUMN_NAME_ENTRY_ID,
                TasksPersistenceContract.TaskEntry.COLUMN_NAME_TITLE,
                TasksPersistenceContract.TaskEntry.COLUMN_NAME_DESCRIPTION,
                TasksPersistenceContract.TaskEntry.COLUMN_NAME_COMPLETED});

        Task cachedTask = mCachedTasks.get(selectionArgs[0]);
        matrixCursor.addRow(new Object[]{

                cachedTask.getId(),
                cachedTask.getTitle(),
                cachedTask.getDescription(),
                cachedTask.isCompleted() ? 1 : 0});

        return matrixCursor;
    }

    private MatrixCursor getCachedTasks(String selection, String[] selectionArgs) {
        MatrixCursor matrixCursor = new MatrixCursor(new String[]{

                TasksPersistenceContract.TaskEntry.COLUMN_NAME_ENTRY_ID,
                TasksPersistenceContract.TaskEntry.COLUMN_NAME_TITLE,
                TasksPersistenceContract.TaskEntry.COLUMN_NAME_DESCRIPTION,
                TasksPersistenceContract.TaskEntry.COLUMN_NAME_COMPLETED});

        if (mCachedTasks == null) {
            return matrixCursor;
        } else {
            Iterator it = mCachedTasks.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                Task cachedTask = (Task) pair.getValue();

                if (selection != null) {
                    boolean taskStateFilter = selectionArgs[0].equals("1");
                    if (cachedTask.isCompleted() == taskStateFilter) {
                        matrixCursor.addRow(new Object[]{

                                cachedTask.getId(),
                                cachedTask.getTitle(),
                                cachedTask.getDescription(),
                                cachedTask.isCompleted() ? 1 : 0
                        });
                    }
                } else {
                    matrixCursor.addRow(new Object[]{

                            cachedTask.getId(),
                            cachedTask.getTitle(),
                            cachedTask.getDescription(),
                            cachedTask.isCompleted() ? 1 : 0
                    });
                }

                it.remove(); // avoids a ConcurrentModificationException
            }
            return matrixCursor;
        }
    }

    private void saveToLocalCache(Cursor tasks) {
        if (mCachedTasks == null) {
            mCachedTasks = new LinkedHashMap<>();
        }
        while (tasks.moveToNext()) {
            Task newTask = Task.from(tasks);
            mCachedTasks.put(newTask.getId(), newTask);
        }
    }

    private void saveTasksInLocalDataSource(List<Task> tasks) {
        if (mCachedTasks == null) {
            mCachedTasks = new LinkedHashMap<>();
        }
        if (tasks != null) {
            for (Task task : tasks) {
                mTasksLocalDataSource.saveTask(task);
                mCachedTasks.put(task.getId(), task);
            }
        }
    }

    private void clearCache() {
        if (mCachedTasks == null) {
            mCachedTasks = new LinkedHashMap<>();
        } else {
            mCachedTasks.clear();
        }
    }

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = TasksPersistenceContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, TasksPersistenceContract.TaskEntry.TABLE_NAME, TASK);
        matcher.addURI(authority, TasksPersistenceContract.TaskEntry.TABLE_NAME + "/*", TASK_ITEM);

        return matcher;
    }

}
