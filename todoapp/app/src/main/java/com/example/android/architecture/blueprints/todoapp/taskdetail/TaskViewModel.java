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

package com.example.android.architecture.blueprints.todoapp.taskdetail;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;

import com.android.annotations.Nullable;
import com.example.android.architecture.blueprints.todoapp.R;
import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;
import com.example.android.architecture.blueprints.todoapp.BoundSnackBar;


/**
 *
 */
public abstract class TaskViewModel extends BaseObservable
        implements TasksDataSource.GetTaskCallback {

    private final TasksRepository mTasksRepository;

    public final ObservableBoolean completed = new ObservableBoolean();

    public final ObservableField<BoundSnackBar> snackbar = new ObservableField<>();

    private final Context mContext;

    @Nullable
    private Task mTask;

    private boolean mIsDataLoading; // TODO observable field?

    public TaskViewModel(Context context, TasksRepository tasksRepository) {
        mContext = context;
        mTasksRepository = tasksRepository;
        snackbar.set(new BoundSnackBar());
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
        completed.set(task.isCompleted());
        mIsDataLoading = false;
        notifyChange();
    }

    @Override
    public void onDataNotAvailable() {
        mTask = null;
        mIsDataLoading = false;
    }

    /**
     * Called by the Data Binding library.
     */
    public void completeChanged(boolean isChecked) {
        mTask.setCompleted(isChecked);
        if (isChecked) {
            mTasksRepository.completeTask(mTask);
            completed.set(true);
            snackbar.get().showMessage(
                    mContext.getResources().getString(R.string.task_marked_complete));
        } else {
            mTasksRepository.activateTask(mTask);
            completed.set(false);
            snackbar.get().showMessage(
                    mContext.getResources().getString(R.string.task_marked_active));
        }
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
}
