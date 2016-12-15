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
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableList;
import android.graphics.drawable.Drawable;

import com.example.android.architecture.blueprints.todoapp.BR;
import com.example.android.architecture.blueprints.todoapp.R;
import com.example.android.architecture.blueprints.todoapp.SnackBarChangedCallback;
import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;
import com.example.android.architecture.blueprints.todoapp.util.EspressoIdlingResource;

import java.util.ArrayList;
import java.util.List;

import static com.example.android.architecture.blueprints.todoapp.tasks.TasksFilterType.ALL_TASKS;

/**
 * Exposes the data to be used in the {@link TasksContract.View}.
 * <p>
 * {@link BaseObservable} implements a listener registration mechanism which is notified when a
 * property changes. This is done by assigning a {@link Bindable} annotation to the property's
 * getter method.
 */
public class TasksViewModel extends BaseObservable implements SnackBarChangedCallback.SnackBarViewModel {

    private final TasksRepository mTasksRepository;

    private final TasksNavigator mNavigator;

    private TasksFilterType mCurrentFiltering = TasksFilterType.ALL_TASKS;

    public final ObservableList<Task> items = new ObservableArrayList<>();

    public final ObservableBoolean dataLoading = new ObservableBoolean(false);

    public final ObservableBoolean isDataLoadingError = new ObservableBoolean(false);

    public final ObservableField<String> snackBarText = new ObservableField<>();

    private Context mContext;

    public TasksViewModel(
            TasksRepository repository,
            Context context,
            TasksNavigator navigator) {
        mContext = context;
        mTasksRepository = repository;
        mNavigator = navigator;
    }

    public void start() {
        loadTasks(false);
    }

    @Bindable
    public String getCurrentFilteringLabel() {
        switch (mCurrentFiltering) {
            case ALL_TASKS:
                return mContext.getResources().getString(R.string.label_all);
            case ACTIVE_TASKS:
                return mContext.getResources().getString(R.string.label_active);
            case COMPLETED_TASKS:
                return mContext.getResources().getString(R.string.label_completed);
        }
        return null;
    }

    @Bindable
    public String getNoTasksLabel() {
        switch (mCurrentFiltering) {
            case ALL_TASKS:
                return mContext.getResources().getString(R.string.no_tasks_all);
            case ACTIVE_TASKS:
                return mContext.getResources().getString(R.string.no_tasks_active);
            case COMPLETED_TASKS:
                return mContext.getResources().getString(R.string.no_tasks_completed);
        }
        return null;
    }

    @Bindable
    public Drawable getNoTaskIconRes() {
        switch (mCurrentFiltering) {
            case ALL_TASKS:
                return mContext.getResources().getDrawable(R.drawable.ic_assignment_turned_in_24dp);
            case ACTIVE_TASKS:
                return mContext.getResources().getDrawable(R.drawable.ic_check_circle_24dp);
            case COMPLETED_TASKS:
                return mContext.getResources().getDrawable(R.drawable.ic_verified_user_24dp);
        }
        return null;
    }

    @Bindable
    public boolean getTasksAddViewVisible() {
        return mCurrentFiltering == ALL_TASKS;
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
    }

    public TasksFilterType getFiltering() {
        return mCurrentFiltering;
    }

    public void clearCompletedTasks() {
        mTasksRepository.clearCompletedTasks();
        snackBarText.set(mContext.getString(R.string.completed_tasks_cleared));
        loadTasks(false, false);
    }

    @Override
    public String getSnackBarText() {
        return snackBarText.get();
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
                isDataLoadingError.set(false);

                items.clear();
                items.addAll(tasksToShow);
                notifyPropertyChanged(BR.empty);
                notifyPropertyChanged(BR.viewmodel);
            }

            @Override
            public void onDataNotAvailable() {
                isDataLoadingError.set(true);
            }
        });
    }

    /**
     * Called by Data Binding library.
     */
    public void addNewTask() {
        mNavigator.addNewTask();
    }

}
