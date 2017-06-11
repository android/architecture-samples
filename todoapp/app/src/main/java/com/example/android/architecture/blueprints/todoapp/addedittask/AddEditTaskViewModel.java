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

package com.example.android.architecture.blueprints.todoapp.addedittask;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.content.Context;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.support.annotation.Nullable;

import com.example.android.architecture.blueprints.todoapp.R;
import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;

import static android.content.ContentValues.TAG;

/**
 * ViewModel for the Add/Edit screen.
 * <p>
 * This ViewModel only exposes {@link ObservableField}s, so it doesn't need to extend
 * {@link android.databinding.BaseObservable} and updates are notified automatically. See
 * {@link com.example.android.architecture.blueprints.todoapp.statistics.StatisticsViewModel} for
 * how to deal with more complex scenarios.
 */
public class AddEditTaskViewModel extends ViewModel implements TasksDataSource.GetTaskCallback {

    public ObservableField<String> title = new ObservableField<>();

    public ObservableField<String> description = new ObservableField<>();

    public ObservableBoolean dataLoading = new ObservableBoolean(false);

    public final MutableLiveData<String> snackbarText = new MutableLiveData<>();

    TasksRepository mTasksRepository;

    Context mContext;  // To avoid leaks, this must be an Application Context.

    @Nullable
    private String mTaskId;

    private boolean mIsNewTask;

    private boolean mIsDataLoaded = false;

    private AddEditTaskNavigator mAddEditTaskNavigator;

    public AddEditTaskViewModel() {
    }

    public void init(Context context, TasksRepository tasksRepository) {
        this.mContext = context.getApplicationContext(); // Force use of Application Context.
        this.mTasksRepository = tasksRepository;
    }

    AddEditTaskViewModel(Context context, TasksRepository tasksRepository) {
        this.init(context, tasksRepository);
    }

    void onActivityCreated(AddEditTaskNavigator navigator) {
        mAddEditTaskNavigator = navigator;
    }

    void onActivityDestroyed() {
        // Clear references to avoid potential memory leaks.
        mAddEditTaskNavigator = null;
    }

    public void start(String taskId) {
        if (dataLoading.get()) {
            // Already loading, ignore.
            return;
        }
        mTaskId = taskId;
        if (taskId == null) {
            // No need to populate, it's a new task
            mIsNewTask = true;
            return;
        }
        if (mIsDataLoaded) {
            // No need to populate, already have data.
            return;
        }
        mIsNewTask = false;
        dataLoading.set(true);
        mTasksRepository.getTask(taskId, this);
    }

    @Override
    public void onTaskLoaded(Task task) {
        title.set(task.getTitle());
        description.set(task.getDescription());
        dataLoading.set(false);
        mIsDataLoaded = true;

        // Note that there's no need to notify that the values changed because we're using
        // ObservableFields.
    }

    @Override
    public void onDataNotAvailable() {
        dataLoading.set(false);
    }

    // Called when clicking on fab.
    public void saveTask(String title, String description) {
        if (isNewTask()) {
            createTask(title, description);
        } else {
            updateTask(title, description);
        }
    }

    @Nullable
    public String getSnackbarText() {
        return snackbarText.getValue();
    }

    private boolean isNewTask() {
        return mIsNewTask;
    }

    private void createTask(String title, String description) {
        Task newTask = new Task(title, description);
        if (newTask.isEmpty()) {
            snackbarText.setValue(mContext.getString(R.string.empty_task_message));
        } else {
            mTasksRepository.saveTask(newTask);
            navigateOnTaskSaved();
        }
    }

    private void updateTask(String title, String description) {
        if (isNewTask()) {
            throw new RuntimeException("updateTask() was called but task is new.");
        }
        mTasksRepository.saveTask(new Task(title, description, mTaskId));
        navigateOnTaskSaved(); // After an edit, go back to the list.
    }

    private void navigateOnTaskSaved() {
        if (mAddEditTaskNavigator!= null) {
            mAddEditTaskNavigator.onTaskSaved();
        }
    }
}
