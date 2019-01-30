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
import android.content.Context;
import android.graphics.drawable.Drawable;

import com.example.android.architecture.blueprints.todoapp.Event;
import com.example.android.architecture.blueprints.todoapp.R;
import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskActivity;
import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;
import com.example.android.architecture.blueprints.todoapp.taskdetail.TaskDetailActivity;

import java.util.ArrayList;
import java.util.List;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;


/**
 * Exposes the data to be used in the task list screen.
 * <p>
 * {@link BaseObservable} implements a listener registration mechanism which is notified when a
 * property changes. This is done by assigning a {@link Bindable} annotation to the property's
 * getter method.
 */
public class TasksViewModel extends AndroidViewModel {

    // These LiveDatas will update Views automatically
    public final MutableLiveData<List<Task>> items = new MutableLiveData<>();

    public final MutableLiveData<Boolean> dataLoading = new MutableLiveData<>();

    public final MutableLiveData<String> currentFilteringLabel = new MutableLiveData<>();

    public final MutableLiveData<String> noTasksLabel = new MutableLiveData<>();

    public final MutableLiveData<Drawable> noTaskIconRes = new MutableLiveData<>();

    public final MutableLiveData<Boolean> empty = new MutableLiveData<>();

    public final MutableLiveData<Boolean> tasksAddViewVisible = new MutableLiveData<>();

    private final MutableLiveData<Event<Integer>> mSnackbarText = new MutableLiveData<>();

    private TasksFilterType mCurrentFiltering = TasksFilterType.ALL_TASKS;

    private final TasksRepository mTasksRepository;

    private final MutableLiveData<Boolean> mIsDataLoadingError = new MutableLiveData<>();

    private final MutableLiveData<Event<String>> mOpenTaskEvent = new MutableLiveData<>();

    private final Context mContext; // To avoid leaks, this must be an Application Context.

    private final MutableLiveData<Event<Object>> mNewTaskEvent = new MutableLiveData<>();

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
                currentFilteringLabel.setValue(mContext.getString(R.string.label_all));
                noTasksLabel.setValue(mContext.getResources().getString(R.string.no_tasks_all));
                noTaskIconRes.setValue(mContext.getResources().getDrawable(
                        R.drawable.ic_assignment_turned_in_24dp));
                tasksAddViewVisible.setValue(true);
                break;
            case ACTIVE_TASKS:
                currentFilteringLabel.setValue(mContext.getString(R.string.label_active));
                noTasksLabel.setValue(mContext.getResources().getString(R.string.no_tasks_active));
                noTaskIconRes.setValue(mContext.getResources().getDrawable(
                        R.drawable.ic_check_circle_24dp));
                tasksAddViewVisible.setValue(false);
                break;
            case COMPLETED_TASKS:
                currentFilteringLabel.setValue(mContext.getString(R.string.label_completed));
                noTasksLabel.setValue(mContext.getResources().getString(R.string.no_tasks_completed));
                noTaskIconRes.setValue(mContext.getResources().getDrawable(
                        R.drawable.ic_verified_user_24dp));
                tasksAddViewVisible.setValue(false);
                break;
        }

    }

    public void clearCompletedTasks() {
        mTasksRepository.clearCompletedTasks();
        mSnackbarText.setValue(new Event<>(R.string.completed_tasks_cleared));
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

    LiveData<Event<Integer>> getSnackbarMessage() {
        return mSnackbarText;
    }

    LiveData<Event<String>> getOpenTaskEvent() {
        return mOpenTaskEvent;
    }

    LiveData<Event<Object>> getNewTaskEvent() {
        return mNewTaskEvent;
    }

    private void showSnackbarMessage(Integer message) {
        mSnackbarText.setValue(new Event<>(message));
    }

    /**
     * Called by the Data Binding library and the FAB's click listener.
     */
    public void addNewTask() {
        mNewTaskEvent.setValue(new Event<>(new Object()));
    }

    /**
     * Called by the {@link TasksAdapter}.
     */
    void openTask(String taskId) {
        mOpenTaskEvent.setValue(new Event<>(taskId));

    }

    void handleActivityResult(int requestCode, int resultCode) {
        if (AddEditTaskActivity.REQUEST_CODE == requestCode) {
            switch (resultCode) {
                case TaskDetailActivity.EDIT_RESULT_OK:
                    mSnackbarText.setValue(new Event<>(R.string.successfully_saved_task_message));
                    break;
                case AddEditTaskActivity.ADD_EDIT_RESULT_OK:
                    mSnackbarText.setValue(new Event<>(R.string.successfully_added_task_message));
                    break;
                case TaskDetailActivity.DELETE_RESULT_OK:
                    mSnackbarText.setValue(new Event<>(R.string.successfully_deleted_task_message));
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
            dataLoading.setValue(true);
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
                    dataLoading.setValue(false);
                }
                mIsDataLoadingError.setValue(false);

                List<Task> itemsValue = new ArrayList<>(tasksToShow);
                empty.setValue(itemsValue.isEmpty());
                items.setValue(itemsValue);
            }

            @Override
            public void onDataNotAvailable() {
                mIsDataLoadingError.setValue(true);
            }
        });
    }
}
