package com.example.android.architecture.blueprints.todoapp.data.source;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.local.TasksPersistenceContract;

import java.util.List;

public class TasksInteractor implements TasksDataSource {

    private final ContentResolver mContentResolver;

    public TasksInteractor(ContentResolver mContentResolver) {
        this.mContentResolver = mContentResolver;
    }

    @Nullable
    @Override
    public List<Task> getTasks() {
        // the Loader and Content Provider deal with returning data
        return null;
    }

    @Nullable
    @Override
    public Task getTask(@NonNull String taskId) {
        // the Loader and Content Provider deal with returning data
        return null;
    }

    @Override
    public void saveTask(@NonNull Task task) {

    }

    @Override
    public void completeTask(@NonNull Task task) {

    }

    @Override
    public void completeTask(@NonNull String taskId) {
        try {
            ContentValues values = new ContentValues();
            values.put(TasksPersistenceContract.TaskEntry.COLUMN_NAME_COMPLETED, true);

            String selection = TasksPersistenceContract.TaskEntry.COLUMN_NAME_ENTRY_ID + " LIKE ?";
            String[] selectionArgs = {taskId};

            mContentResolver.update(TasksPersistenceContract.TaskEntry.buildTasksUri(), values, selection, selectionArgs);
        } catch (IllegalStateException e) {
            // Send to analytics, log etc
        }
    }

    @Override
    public void activateTask(@NonNull Task task) {
        try {
            ContentValues values = new ContentValues();
            values.put(TasksPersistenceContract.TaskEntry.COLUMN_NAME_COMPLETED, false);

            String selection = TasksPersistenceContract.TaskEntry.COLUMN_NAME_ENTRY_ID + " LIKE ?";
            String[] selectionArgs = {task.getId()};

            mContentResolver.update(TasksPersistenceContract.TaskEntry.buildTasksUri(), values, selection, selectionArgs);
        } catch (IllegalStateException e) {
            // Send to analytics, log etc
        }
    }

    @Override
    public void activateTask(@NonNull String taskId) {

    }

    @Override
    public void clearCompletedTasks() {

    }

    @Override
    public void refreshTasks() {

    }

    @Override
    public void deleteAllTasks() {

    }

    @Override
    public void deleteTask(@NonNull String taskId) {
        try {
            String selection = TasksPersistenceContract.TaskEntry.COLUMN_NAME_ENTRY_ID + " LIKE ?";
            String[] selectionArgs = {taskId};
            mContentResolver.delete(TasksPersistenceContract.TaskEntry.buildTasksUri(), selection, selectionArgs);
        } catch (IllegalStateException e) {
            // Send to analytics, log etc
        }
    }
}
