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

package com.example.android.architecture.blueprints.todomvp.tasks;

import android.support.annotation.NonNull;
import android.util.Log;

import com.example.android.architecture.blueprints.todomvp.data.Task;
import com.example.android.architecture.blueprints.todomvp.data.source.TasksDataSource;
import com.example.android.architecture.blueprints.todomvp.data.source.TasksRepository;
import com.example.android.architecture.blueprints.todomvp.util.EspressoIdlingResource;

import java.util.ArrayList;
import java.util.List;

import static com.example.android.architecture.blueprints.todomvp.tasks.TasksFragment.ACTIVE_TASKS;
import static com.example.android.architecture.blueprints.todomvp.tasks.TasksFragment.ALL_TASKS;
import static com.example.android.architecture.blueprints.todomvp.tasks.TasksFragment.COMPLETED_TASKS;
import static com.google.common.base.Preconditions.checkNotNull;


/**
 * Listens to user actions from the UI ({@link TasksFragment}), retrieves the data and updates the
 * UI as required.
 */
public class TasksPresenter implements TasksContract.UserActionsListener {

    private final TasksRepository mTasksRepository;

    private final TasksContract.View mTasksView;

    public TasksPresenter(
            @NonNull TasksRepository tasksRepository, @NonNull TasksContract.View tasksView) {
        mTasksRepository = checkNotNull(tasksRepository, "tasksRepository cannot be null");
        mTasksView = checkNotNull(tasksView, "tasksView cannot be null!");
    }

    @Override
    public void loadAllTasks(boolean forceUpdate) {
        loadTasks(forceUpdate, true, ALL_TASKS);
    }

    @Override
    public void loadActiveTasks(boolean forceUpdate) {
        loadTasks(forceUpdate, true, ACTIVE_TASKS);
    }

    @Override
    public void loadCompletedTasks(boolean forceUpdate) {
        loadTasks(forceUpdate, true, COMPLETED_TASKS);
    }

    /**
     * @param forceUpdate Pass in true to refresh the data in the {@link TasksDataSource}
     * @param showLoadingUI Pass in true to display a loading icon in the UI
     * @param requestType Corresponds to the position of the Navigation Spinner
     */
    private void loadTasks(boolean forceUpdate, final boolean showLoadingUI, final int requestType) {
        if (showLoadingUI) {
            mTasksView.setProgressIndicator(true);
        }
        if (forceUpdate) {
            mTasksRepository.refreshTasks();
        }

        // The network request might be handled in a different thread so make sure Espresso knows
        // that the app is busy until the response is handled.
        EspressoIdlingResource.increment(); // App is busy until further notice

        mTasksRepository.getTasks(new TasksDataSource.LoadTasksCallback() {
            @Override
            public void onTasksLoaded(List<Task> tasks) {
                List<Task> tasksToShow = new ArrayList<Task>();

                // This callback may be called twice, once for the cache and once for loading
                // the data from the server API, so we check before decrementing, otherwise
                // it throws "Counter has been corrupted!" exception.
                if (!EspressoIdlingResource.getIdlingResource().isIdleNow()) {
                    EspressoIdlingResource.decrement(); // Set app as idle.
                }

                // We filter the tasks based on the requestType
                for (Task task: tasks) {
                    switch (requestType) {
                        case ALL_TASKS:
                            tasksToShow.add(task);
                            break;
                        case ACTIVE_TASKS:
                            if (task.isActive()) {
                                tasksToShow.add(task);
                            }
                            break;
                        case COMPLETED_TASKS:
                            if (task.isCompleted()) {
                                tasksToShow.add(task);
                            }
                            break;
                        default:
                            tasksToShow.add(task);
                            break;
                    }
                }

                // The View may not be on screen anymore when this callback is returned
                if (mTasksView.isInactive()) {
                    return;
                }

                if (showLoadingUI) {
                    mTasksView.setProgressIndicator(false);
                }

                mTasksView.showTasks(tasksToShow);
            }

            @Override
            public void onDataNotAvailable() {
                // The View may not be on screen anymore when this callback is returned
                if (mTasksView.isInactive()) {
                    return;
                }

                mTasksView.showLoadingTasksError();
            }
        });
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
}
