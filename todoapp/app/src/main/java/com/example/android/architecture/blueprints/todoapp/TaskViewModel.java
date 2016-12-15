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

package com.example.android.architecture.blueprints.todoapp;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.ObservableField;
import android.util.Log;

import com.android.annotations.Nullable;
import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;


/**
 * TODO
 */
public abstract class TaskViewModel extends BaseObservable
        implements TasksDataSource.GetTaskCallback, SnackBarChangedCallback.SnackBarViewModel {

    public final ObservableField<String> snackBarText = new ObservableField<>();

    private static final String TAG = "TaskViewModel";

    private final TasksRepository mTasksRepository;

    private final Context mContext;

    @Nullable
    private Task mTask;

    private boolean mIsDataLoading; // TODO observable field?

    public TaskViewModel(Context context, TasksRepository tasksRepository) {
        mContext = context;
        mTasksRepository = tasksRepository;
    }

    public void start(String taskId) {
        if (taskId != null) {
            mIsDataLoading = true;
            mTasksRepository.getTask(taskId, this);
        }
    }

    @Bindable
    public String getTitle() {
        if (mTask == null) {
            return "No data";
        }
        return mTask.getTitle();
    }

    @Bindable
    public String getDescription() {
        if (mTask == null) {
            return "";
        }
        return mTask.getDescription();
    }

    @Bindable
    public boolean getCompleted() {
        return mTask.isCompleted();
    }

    public void setCompleted(boolean completed) {
        completeChanged(completed);
    }

    @Bindable
    public boolean isDataAvailable() {
        return mTask != null;
    }

    @Bindable
    public boolean isDataLoading() {
        return mIsDataLoading;
    }

    @Bindable
    public String getTitleForList() {
        if (mTask == null) {
            return "No data";
        }
        return mTask.getTitleForList();
    }

    @Override
    public void onTaskLoaded(Task task) {
        mTask = task;
        mIsDataLoading = false;
        notifyChange();
    }

    @Override
    public void onDataNotAvailable() {
        mTask = null;
        mIsDataLoading = false;
    }


    private void completeChanged(boolean isChecked) {
        Log.d(TAG, "User completed/activated a task");
        mTask.setCompleted(isChecked);
        if (isChecked) {
            mTasksRepository.completeTask(mTask);
            snackBarText.set(mContext.getResources().getString(R.string.task_marked_complete));
        } else {
            mTasksRepository.activateTask(mTask);
            snackBarText.set(mContext.getResources().getString(R.string.task_marked_active));
        }
        notifyPropertyChanged(BR.completed);
    }

    public void deleteTask() {
        mTasksRepository.deleteTask(mTask.getId());
    }

    public void onRefresh() {
        if (mTask != null) {
            start(mTask.getId());
        }
    }

    @Nullable
    protected String getTaskId() {
        return mTask.getId();
    }

    @Override
    public String getSnackBarText() {
        return snackBarText.get();
    }
}
