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

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;

import com.android.annotations.Nullable;
import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;

/**
 *
 */
public class AddEditTaskViewModel extends BaseObservable implements TasksDataSource.GetTaskCallback {

    private final TasksRepository mTasksRepository;

    public final ObservableBoolean showEmptyTaskError = new ObservableBoolean();

    public final ObservableField<String> title = new ObservableField<>();

    public final ObservableField<String> description = new ObservableField<>();

    @Nullable
    private String mTaskId;

    private boolean mIsNewTask;

    private boolean mIsDataLoading;

    private AddEditTaskNavigator mNavigator;

    public AddEditTaskViewModel(TasksRepository tasksRepository, AddEditTaskNavigator taskDetailNavigator) {
        mTasksRepository = tasksRepository;
        mNavigator = taskDetailNavigator;
    }

    public void start(String taskId) {
        mTaskId = taskId;
        if (taskId == null) {
            // No need to populate, it's a new task
            mIsNewTask = true;
            return;
        }
        mIsDataLoading = true;
        mIsNewTask = false;
        mTasksRepository.getTask(taskId, this);
    }
//
//    @Bindable
//    public String getTitle() {
//        if (mTask == null) {
//            return "No data";
//        }
//        return mTask.getTitle();
//    }
//
//    @Bindable
//    public String getDescription() {
//        if (mTask == null) {
//            return "";
//        }
//        return mTask.getDescription();
//    }

//
//    @Bindable
//    public boolean isDataAvailable() {
//        return mTask != null;
//    }

    @Bindable
    public boolean isDataLoading() {
        return mIsDataLoading;
    }

    @Override
    public void onTaskLoaded(Task task) {
        title.set(task.getTitle());
        description.set(task.getDescription());
        mIsDataLoading = false;
        notifyChange();
    }

    @Override
    public void onDataNotAvailable() {
        mIsDataLoading = false;
    }

    // Called when clicking on fab.
    public void saveTask(String title, String description) {
        if (isNewTask()) {
            createTask(title, description);
        } else {
            updateTask(title, description);
        }
    }

    private boolean isNewTask() {
        return mIsNewTask;
    }

    private void createTask(String title, String description) {
        Task newTask = new Task(title, description);
        if (newTask.isEmpty()) {
            showEmptyTaskError.set(true);
        } else {
            mTasksRepository.saveTask(newTask);
            mNavigator.onTaskSaved();
        }
    }

    private void updateTask(String title, String description) {
        if (isNewTask()) {
            throw new RuntimeException("updateTask() was called but task is new.");
        }
        mTasksRepository.saveTask(new Task(title, description, mTaskId));
        mNavigator.onTaskSaved(); // After an edit, go back to the list.
    }
}
