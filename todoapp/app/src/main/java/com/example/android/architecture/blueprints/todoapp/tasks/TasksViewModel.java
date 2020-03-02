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

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableList;

import com.example.android.architecture.blueprints.todoapp.BR;
import com.example.android.architecture.blueprints.todoapp.R;
import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskActivity;
import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;
import com.example.android.architecture.blueprints.todoapp.taskdetail.TaskDetailActivity;
import com.example.android.architecture.blueprints.todoapp.util.EspressoIdlingResource;

import java.util.ArrayList;
import java.util.List;

/**
 * Exposes the data to be used in the task list screen.
 * <p>
 * {@link BaseObservable} implements a listener registration mechanism which is notified when a
 * property changes. This is done by assigning a {@link Bindable} annotation to the property's
 * getter method.
 */
public class TasksViewModel extends BaseObservable {

    // These observable fields will update Views automatically
    public final ObservableList<Task> items = new ObservableArrayList<>();

    public final ObservableBoolean dataLoading = new ObservableBoolean(false);

    public final ObservableField<String> currentFilteringLabel = new ObservableField<>();

    public final ObservableField<String> noTasksLabel = new ObservableField<>();

    public final ObservableField<Drawable> noTaskIconRes = new ObservableField<>();

    public final ObservableBoolean tasksAddViewVisible = new ObservableBoolean();

    final ObservableField<String> snackbarText = new ObservableField<>();

    private TasksFilterType mCurrentFiltering = TasksFilterType.ALL_TASKS;

    private final TasksRepository mTasksRepository;

    private final ObservableBoolean mIsDataLoadingError = new ObservableBoolean(false);

    private Context mContext; // To avoid leaks, this must be an Application Context.

    private TasksNavigator mNavigator;

    public TasksViewModel(
            TasksRepository repository,
            Context context) {
        mContext = context.getApplicationContext(); // Force use of Application Context.
        mTasksRepository = repository;

        // Set initial state
        setFiltering(TasksFilterType.ALL_TASKS);
    }

    void setNavigator(TasksNavigator navigator) {
        mNavigator = navigator;
    }

    void onActivityDestroyed() {
        // Clear references to avoid potential memory leaks.
        mNavigator = null;
    }

    public void start() {
        loadTasks(false);
    }

    @Bindable
    public boolean isEmpty() {
        return items.isEmpty();
    }

    public void loadTasks(boolean forceUpdate) {
        loadTasks(forceUpdate, true);
    }

    /**
     * Sets the current task filtering type.
     *
     * @param requestType Can be {@link TasksFilterType#ALL_TASKS},
     *                    {@link TasksFilterType#COMPLETED_TASKS}, or
     *                    {@link TasksFilterType#ACTIVE_TASKS}
     */
    public void setFiltering(TasksFilterType requestType) {
        mCurrentFiltering = requestType;

        // Depending on the filter type, set the filtering label, icon drawables, etc.
        switch (requestType) {
            case ALL_TASKS:
                currentFilteringLabel.set(mContext.getString(R.string.label_all));
                noTasksLabel.set(mContext.getResources().getString(R.string.no_tasks_all));
                noTaskIconRes.set(mContext.getResources().getDrawable(
                        R.drawable.ic_assignment_turned_in_24dp));
                tasksAddViewVisible.set(true);
                break;
            case ACTIVE_TASKS:
                currentFilteringLabel.set(mContext.getString(R.string.label_active));
                noTasksLabel.set(mContext.getResources().getString(R.string.no_tasks_active));
                noTaskIconRes.set(mContext.getResources().getDrawable(
                        R.drawable.ic_check_circle_24dp));
                tasksAddViewVisible.set(false);
                break;
            case COMPLETED_TASKS:
                currentFilteringLabel.set(mContext.getString(R.string.label_completed));
                noTasksLabel.set(mContext.getResources().getString(R.string.no_tasks_completed));
                noTaskIconRes.set(mContext.getResources().getDrawable(
                        R.drawable.ic_verified_user_24dp));
                tasksAddViewVisible.set(false);
                break;
        }
    }

    public void clearCompletedTasks() {
        mTasksRepository.clearCompletedTasks();
        snackbarText.set(mContext.getString(R.string.completed_tasks_cleared));
        loadTasks(false, false);
    }

    public String getSnackbarText() {
        return snackbarText.get();
    }

    /**
     * Called by the Data Binding library and the FAB's click listener.
     */
    public void addNewTask() {
        if (mNavigator != null) {
            mNavigator.addNewTask();
        }
    }

    void handleActivityResult(int requestCode, int resultCode) {
        if (AddEditTaskActivity.REQUEST_CODE == requestCode) {
            switch (resultCode) {
                case TaskDetailActivity.EDIT_RESULT_OK:
                    snackbarText.set(
                            mContext.getString(R.string.successfully_saved_task_message));
                    break;
                case AddEditTaskActivity.ADD_EDIT_RESULT_OK:
                    snackbarText.set(
                            mContext.getString(R.string.successfully_added_task_message));
                    break;
                case TaskDetailActivity.DELETE_RESULT_OK:
                    snackbarText.set(
                            mContext.getString(R.string.successfully_deleted_task_message));
                    break;
            }
        }
    }

    /**
     * @param forceUpdate   Pass in true to refresh the data in the {@link TasksDataSource}
     * @param showLoadingUI Pass in true to display a loading icon in the UI
     */
    private void loadTasks(boolean forceUpdate, final boolean showLoadingUI) {
        if (showLoadingUI) {
            dataLoading.set(true);
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
                for (Task task : tasks) {
                    switch (mCurrentFiltering) {
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
                if (showLoadingUI) {
                    dataLoading.set(false);
                }
                mIsDataLoadingError.set(false);

                items.clear();
                items.addAll(tasksToShow);
                notifyPropertyChanged(BR.empty); // It's a @Bindable so update manually
            }

            @Override
            public void onDataNotAvailable() {
                mIsDataLoadingError.set(true);
            }
        });
    }

}
