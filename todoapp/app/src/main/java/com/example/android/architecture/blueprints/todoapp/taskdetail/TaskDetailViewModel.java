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

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.example.android.architecture.blueprints.todoapp.SingleLiveEvent;
import com.example.android.architecture.blueprints.todoapp.R;
import com.example.android.architecture.blueprints.todoapp.SnackbarMessage;
import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;
import com.example.android.architecture.blueprints.todoapp.tasks.TasksFragment;


/**
 * Listens to user actions from the list item in ({@link TasksFragment}) and redirects them to the
 * Fragment's actions listener.
 */
public class TaskDetailViewModel extends AndroidViewModel implements TasksDataSource.GetTaskCallback {

    public final ObservableField<Task> task = new ObservableField<>();

    public final ObservableBoolean completed = new ObservableBoolean();

    private final SingleLiveEvent<Void> mEditTaskCommand = new SingleLiveEvent<>();

    private final SingleLiveEvent<Void> mDeleteTaskCommand = new SingleLiveEvent<>();

    private final TasksRepository mTasksRepository;

    private final SnackbarMessage mSnackbarText = new SnackbarMessage();

    private boolean mIsDataLoading;

    public TaskDetailViewModel(Application context, TasksRepository tasksRepository) {
        super(context);
        mTasksRepository = tasksRepository;
    }

    public void deleteTask() {
        if (task.get() != null) {
            mTasksRepository.deleteTask(task.get().getId());
            mDeleteTaskCommand.call();
        }
    }

    public void editTask() {
        mEditTaskCommand.call();
    }

    public SnackbarMessage getSnackbarMessage() {
        return mSnackbarText;
    }

    public SingleLiveEvent<Void> getEditTaskCommand() {
        return mEditTaskCommand;
    }

    public SingleLiveEvent<Void> getDeleteTaskCommand() {
        return mDeleteTaskCommand;
    }

    public void setCompleted(boolean completed) {
        if (mIsDataLoading) {
            return;
        }
        Task task = this.task.get();
        if (completed) {
            mTasksRepository.completeTask(task);
            showSnackbarMessage(R.string.task_marked_complete);
        } else {
            mTasksRepository.activateTask(task);
            showSnackbarMessage(R.string.task_marked_active);
        }
    }

    public void start(String taskId) {
        if (taskId != null) {
            mIsDataLoading = true;
            mTasksRepository.getTask(taskId, this);
        }
    }

    public void setTask(Task task) {
        this.task.set(task);
        if (task != null) {
            completed.set(task.isCompleted());
        }
    }

    public boolean isDataAvailable() {
        return task.get() != null;
    }

    public boolean isDataLoading() {
        return mIsDataLoading;
    }

    @Override
    public void onTaskLoaded(Task task) {
        setTask(task);
        mIsDataLoading = false;
    }

    @Override
    public void onDataNotAvailable() {
        task.set(null);
        mIsDataLoading = false;
    }

    public void onRefresh() {
        if (task.get() != null) {
            start(task.get().getId());
        }
    }

    @Nullable
    protected String getTaskId() {
        return task.get().getId();
    }

    private void showSnackbarMessage(@StringRes Integer message) {
        mSnackbarText.setValue(message);
    }
}
