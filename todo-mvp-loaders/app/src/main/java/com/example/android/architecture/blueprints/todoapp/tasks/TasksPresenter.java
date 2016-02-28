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
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import com.example.android.architecture.blueprints.todoapp.R;
import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskActivity;
import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksLoader;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Listens to user actions from the UI ({@link TasksFragment}), retrieves the data and updates the
 * UI as required. It is implemented as a non UI {@link Fragment} to make use of the
 * {@link LoaderManager} mechanism for managing loading and updating data asynchronously.
 */
public class TasksPresenter implements TasksContract.Presenter,
        LoaderManager.LoaderCallbacks<List<Task>> {

    private final static int TASKS_QUERY = 1;

    private final TasksRepository mTasksRepository;

    private final TasksContract.View mTasksView;

    private final TasksLoader mLoader;

    private final LoaderManager mLoaderManager;

    private List<Task> mCurrentTasks;

    private TasksFilterType mCurrentFiltering = TasksFilterType.ALL_TASKS;

    private boolean mFirstLoad;

    public TasksPresenter(@NonNull TasksLoader loader, @NonNull LoaderManager loaderManager,
                          @NonNull TasksRepository tasksRepository, @NonNull TasksContract.View tasksView) {
        mLoader = checkNotNull(loader, "loader cannot be null!");
        mLoaderManager = checkNotNull(loaderManager, "loader manager cannot be null");
        mTasksRepository = checkNotNull(tasksRepository, "tasksRepository cannot be null");
        mTasksView = checkNotNull(tasksView, "tasksView cannot be null!");
    }

    @Override
    public void result(int requestCode, int resultCode) {
        // If a task was successfully added, show snackbar
        if (AddEditTaskActivity.REQUEST_ADD_TASK == requestCode && Activity.RESULT_OK == resultCode) {
            mTasksView.showSuccessfullySavedMessage();
        }
    }

    @Override
    public void resume() {
        mLoaderManager.initLoader(TASKS_QUERY, null, this);
    }

    @Override
    public void pause() {

    }

    @Override
    public Loader<List<Task>> onCreateLoader(int id, Bundle args) {
        mTasksView.setLoadingIndicator(true);
        return mLoader;
    }

    @Override
    public void onLoadFinished(Loader<List<Task>> loader, List<Task> data) {
        mTasksView.setLoadingIndicator(false);

        mCurrentTasks = data;
        if (mCurrentTasks == null) {
            mTasksView.showLoadingTasksError();
        } else {
            showFilteredTasks();
        }
    }

    private void showFilteredTasks() {
        List<Task> tasksToDisplay = new ArrayList<>();
        if (mCurrentTasks != null) {
            for (Task task : mCurrentTasks) {
                switch (mCurrentFiltering) {
                    case ALL_TASKS:
                        tasksToDisplay.add(task);
                        break;
                    case ACTIVE_TASKS:
                        if (task.isActive()) {
                            tasksToDisplay.add(task);
                        }
                        break;
                    case COMPLETED_TASKS:
                        if (task.isCompleted()) {
                            tasksToDisplay.add(task);
                        }
                        break;
                    default:
                        tasksToDisplay.add(task);
                        break;
                }
            }
        }

        processTasks(tasksToDisplay);
    }

    @Override
    public void onLoaderReset(Loader<List<Task>> loader) {
        // no-op
    }

    /**
     * @param forceUpdate Pass in true to refresh the data in the {@link TasksDataSource}
     */
    public void loadTasks(boolean forceUpdate) {
        if (forceUpdate || mFirstLoad) {
            mFirstLoad = false;
            mTasksRepository.refreshTasks();
        } else {
            showFilteredTasks();
        }
    }

    private void processTasks(List<Task> tasks) {
        if (tasks.isEmpty()) {
            // Show a message indicating there are no tasks for that filter type.
            processEmptyTasks();
        } else {
            // Show the list of tasks
            mTasksView.showTasks(tasks);
            // Set the filter label's text.
            showFilterLabel();
        }
    }

    private void showFilterLabel() {
        switch (mCurrentFiltering) {
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
        switch (mCurrentFiltering) {
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
        mTasksView.showTaskDetailsUi(requestedTask.getId());
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

    /**
     * Sets the current task filtering type.
     *
     * @param requestType Can be {@link TasksFilterType#ALL_TASKS},
     *                    {@link TasksFilterType#COMPLETED_TASKS}, or {@link TasksFilterType#ACTIVE_TASKS}
     */
    @Override
    public void setFiltering(TasksFilterType requestType) {
        mCurrentFiltering = requestType;
    }

    @Override
    public void optionsItemSelected(int itemId) {
        switch (itemId) {
            case R.id.menu_clear:
                clearCompletedTasks();
                break;
            case R.id.menu_filter:
                mTasksView.showFilteringPopUpMenu();
                break;
            case R.id.menu_refresh:
                loadTasks(true);
                break;
        }
    }

    @Override
    public void popupItemSelected(int itemId) {
        switch (itemId) {
            case R.id.active:
                setFiltering(TasksFilterType.ACTIVE_TASKS);
                break;
            case R.id.completed:
                setFiltering(TasksFilterType.COMPLETED_TASKS);
                break;
            default:
                setFiltering(TasksFilterType.ALL_TASKS);
                break;
        }
        loadTasks(false);
    }

    @Override
    public TasksFilterType getFiltering() {
        return mCurrentFiltering;
    }

}
