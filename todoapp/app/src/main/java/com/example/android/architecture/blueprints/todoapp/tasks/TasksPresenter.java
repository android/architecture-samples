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

package com.example.android.architecture.blueprints.todoapp.tasks;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskActivity;
import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.LoaderProvider;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Listens to user actions from the UI ({@link TasksFragment}), retrieves the data and updates the
 * UI as required. It is implemented as a non UI {@link Fragment} to make use of the
 * {@link LoaderManager} mechanism for managing loading and updating data asynchronously.
 */
public class TasksPresenter implements TasksContract.Presenter,
        TasksRepository.LoadDataCallback, TasksDataSource.GetTasksCallback,
        LoaderManager.LoaderCallbacks<Cursor> {

    public final static int TASKS_LOADER = 1;

    private final TasksContract.View mTasksView;

    @NonNull
    private final TasksRepository mTasksRepository;

    @NonNull
    private final LoaderManager mLoaderManager;

    @NonNull
    private final LoaderProvider mLoaderProvider;

    private TaskFilter mCurrentFiltering;

    public TasksPresenter(@NonNull LoaderProvider loaderProvider, @NonNull LoaderManager loaderManager, @NonNull TasksRepository tasksRepository, @NonNull TasksContract.View tasksView, @NonNull TaskFilter taskFilter) {
        mLoaderProvider = checkNotNull(loaderProvider, "loaderProvider provider cannot be null");
        mLoaderManager = checkNotNull(loaderManager, "loaderManager provider cannot be null");
        mTasksRepository = checkNotNull(tasksRepository, "tasksRepository provider cannot be null");
        mTasksView = checkNotNull(tasksView, "tasksView cannot be null!");
        mCurrentFiltering = checkNotNull(taskFilter, "taskFilter cannot be null!");
        mTasksView.setPresenter(this);
    }

    @Override
    public void result(int requestCode, int resultCode) {
        // If a task was successfully added, show snackbar
        if (AddEditTaskActivity.REQUEST_ADD_TASK == requestCode && Activity.RESULT_OK == resultCode) {
            mTasksView.showSuccessfullySavedMessage();
        }
    }

    @Override
    public void start() {
        loadTasks();
    }

    /**
     * We will always have fresh data from remote, the Loaders handle the local data
     */
    public void loadTasks() {
        mTasksView.setLoadingIndicator(true);
        mTasksRepository.getTasks(this);
    }

    @Override
    public void onDataLoaded(Cursor data) {
        mTasksView.setLoadingIndicator(false);
        // Show the list of tasks
        mTasksView.showTasks(data);
        // Set the filter label's text.
        showFilterLabel();
    }


    @Override
    public void onDataEmpty() {
        mTasksView.setLoadingIndicator(false);
        // Show a message indicating there are no tasks for that filter type.
        processEmptyTasks();
    }

    @Override
    public void onTasksLoaded(List<Task> tasks) {
        // we don't care about the result since the CursorLoader will load the data for us
        if (mLoaderManager.getLoader(TASKS_LOADER) == null) {
            mLoaderManager.initLoader(TASKS_LOADER, mCurrentFiltering.getFilterExtras(), this);
        } else {
            mLoaderManager.restartLoader(TASKS_LOADER, mCurrentFiltering.getFilterExtras(), this);
        }
    }

    @Override
    public void onDataNotAvailable() {
        mTasksView.setLoadingIndicator(false);
        mTasksView.showLoadingTasksError();
    }

    @Override
    public void onDataReset() {
        mTasksView.showTasks(null);
    }

    private void showFilterLabel() {
        switch (mCurrentFiltering.getTasksFilterType()) {
            case ACTIVE_TASKS:
                mTasksView.showActiveFilterLabel();
                break;
            case COMPLETED_TASKS:
                mTasksView.showCompletedFilterLabel();
                break;
            default:
                mTasksView.showAllFilterLabel();
                break;
        }
    }

    private void processEmptyTasks() {
        switch (mCurrentFiltering.getTasksFilterType()) {
            case ACTIVE_TASKS:
                mTasksView.showNoActiveTasks();
                break;
            case COMPLETED_TASKS:
                mTasksView.showNoCompletedTasks();
                break;
            default:
                mTasksView.showNoTasks();
                break;
        }
    }

    @Override
    public void addNewTask() {
        mTasksView.showAddTask();
    }

    @Override
    public void openTaskDetails(@NonNull Task requestedTask) {
        checkNotNull(requestedTask, "requestedTask cannot be null!");
        mTasksView.showTaskDetailsUi(requestedTask);
    }

    @Override
    public void completeTask(@NonNull Task completedTask) {
        checkNotNull(completedTask, "completedTask cannot be null!");
        mTasksRepository.completeTask(completedTask);
        mTasksView.showTaskMarkedComplete();
    }

    @Override
    public void activateTask(@NonNull Task activeTask) {
        checkNotNull(activeTask, "activeTask cannot be null!");
        mTasksRepository.activateTask(activeTask);
        mTasksView.showTaskMarkedActive();
    }

    @Override
    public void clearCompletedTasks() {
        mTasksRepository.clearCompletedTasks();
        mTasksView.showCompletedTasksCleared();
    }

    @Override
    public TasksFilterType getFiltering() {
        return mCurrentFiltering.getTasksFilterType();
    }

    /**
     * Sets the current task filtering type.
     *
     * @param taskFilter Can be {@link TasksFilterType#ALL_TASKS},
     *                   {@link TasksFilterType#COMPLETED_TASKS}, or {@link TasksFilterType#ACTIVE_TASKS}
     */
    @Override
    public void setFiltering(TaskFilter taskFilter) {
        mCurrentFiltering = taskFilter;
        mLoaderManager.initLoader(TASKS_LOADER, mCurrentFiltering.getFilterExtras(), this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return mLoaderProvider.createFilteredTasksLoader(mCurrentFiltering);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null) {
            if (data.moveToLast()) {
                onDataLoaded(data);
            } else {
                onDataEmpty();
            }
        } else {
            onDataNotAvailable();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        onDataReset();
    }
}
