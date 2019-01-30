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

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import android.content.Context;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableList;
import android.graphics.drawable.Drawable;

import com.example.android.architecture.blueprints.todoapp.SingleLiveEvent;
import com.example.android.architecture.blueprints.todoapp.R;
import com.example.android.architecture.blueprints.todoapp.SnackbarMessage;
import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskActivity;
import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;
import com.example.android.architecture.blueprints.todoapp.taskdetail.TaskDetailActivity;

import java.util.ArrayList;
import java.util.List;


/**
 * Exposes the data to be used in the task list screen.
 * <p>
 * {@link BaseObservable} implements a listener registration mechanism which is notified when a
 * property changes. This is done by assigning a {@link Bindable} annotation to the property's
 * getter method.
 */
public class TasksViewModel extends AndroidViewModel {

    // These observable fields will update Views automatically
    public final ObservableList<Task> items = new ObservableArrayList<>();

    public final ObservableBoolean dataLoading = new ObservableBoolean(false);

    public final ObservableField<String> currentFilteringLabel = new ObservableField<>();

    public final ObservableField<String> noTasksLabel = new ObservableField<>();

    public final ObservableField<Drawable> noTaskIconRes = new ObservableField<>();

    public final ObservableBoolean empty = new ObservableBoolean(false);

    public final ObservableBoolean tasksAddViewVisible = new ObservableBoolean();

    private final SnackbarMessage mSnackbarText = new SnackbarMessage();

    private TasksFilterType mCurrentFiltering = TasksFilterType.ALL_TASKS;

    private final TasksRepository mTasksRepository;

    private final ObservableBoolean mIsDataLoadingError = new ObservableBoolean(false);

    private final SingleLiveEvent<String> mOpenTaskEvent = new SingleLiveEvent<>();

    private final Context mContext; // To avoid leaks, this must be an Application Context.

    private final SingleLiveEvent<Void> mNewTaskEvent = new SingleLiveEvent<>();

    public TasksViewModel(
            Application context,
            TasksRepository repository) {
        super(context);
        mContext = context.getApplicationContext(); // Force use of Application Context.
        mTasksRepository = repository;

        // Set initial state
        setFiltering(TasksFilterType.ALL_TASKS);
    }

    public void start() {
        loadTasks(false);
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
        mSnackbarText.setValue(R.string.completed_tasks_cleared);
        loadTasks(false, false);
    }

    public void completeTask(Task task, boolean completed) {
        // Notify repository
        if (completed) {
            mTasksRepository.completeTask(task);
            showSnackbarMessage(R.string.task_marked_complete);
        } else {
            mTasksRepository.activateTask(task);
            showSnackbarMessage(R.string.task_marked_active);
        }
    }

    SnackbarMessage getSnackbarMessage() {
        return mSnackbarText;
    }

    SingleLiveEvent<String> getOpenTaskEvent() {
        return mOpenTaskEvent;
    }

    SingleLiveEvent<Void> getNewTaskEvent() {
        return mNewTaskEvent;
    }

    private void showSnackbarMessage(Integer message) {
        mSnackbarText.setValue(message);
    }

    /**
     * Called by the Data Binding library and the FAB's click listener.
     */
    public void addNewTask() {
        mNewTaskEvent.call();
    }

    void handleActivityResult(int requestCode, int resultCode) {
        if (AddEditTaskActivity.REQUEST_CODE == requestCode) {
            switch (resultCode) {
                case TaskDetailActivity.EDIT_RESULT_OK:
                    mSnackbarText.setValue(R.string.successfully_saved_task_message);
                    break;
                case AddEditTaskActivity.ADD_EDIT_RESULT_OK:
                    mSnackbarText.setValue(R.string.successfully_added_task_message);
                    break;
                case TaskDetailActivity.DELETE_RESULT_OK:
                    mSnackbarText.setValue(R.string.successfully_deleted_task_message);
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

        mTasksRepository.getTasks(new TasksDataSource.LoadTasksCallback() {
            @Override
            public void onTasksLoaded(List<Task> tasks) {
                List<Task> tasksToShow = new ArrayList<>();

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
                empty.set(items.isEmpty());
            }

            @Override
            public void onDataNotAvailable() {
                mIsDataLoadingError.set(true);
            }
        });
    }
}
