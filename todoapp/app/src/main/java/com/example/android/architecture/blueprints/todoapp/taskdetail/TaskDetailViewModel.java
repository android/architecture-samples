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

import com.example.android.architecture.blueprints.todoapp.Event;
import com.example.android.architecture.blueprints.todoapp.R;
import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;
import com.example.android.architecture.blueprints.todoapp.tasks.TasksFragment;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;


/**
 * Listens to user actions from the list item in ({@link TasksFragment}) and redirects them to the
 * Fragment's actions listener.
 */
public class TaskDetailViewModel extends AndroidViewModel implements TasksDataSource.GetTaskCallback {

    public final MutableLiveData<Task> task = new MutableLiveData<>();

    public final MutableLiveData<Boolean> completed = new MutableLiveData<>();

    public final MutableLiveData<Boolean> isDataAvailable = new MutableLiveData<>();

    public final MutableLiveData<Boolean> isDataLoading = new MutableLiveData<>();

    private final MutableLiveData<Event<Object>> mEditTaskCommand = new MutableLiveData<>();

    private final MutableLiveData<Event<Object>> mDeleteTaskCommand = new MutableLiveData<>();

    private final TasksRepository mTasksRepository;

    private final MutableLiveData<Event<Integer>> mSnackbarText = new MutableLiveData<>();


    public TaskDetailViewModel(Application context, TasksRepository tasksRepository) {
        super(context);
        mTasksRepository = tasksRepository;
    }

    public void deleteTask() {
        if (task.getValue() != null) {
            mTasksRepository.deleteTask(task.getValue().getId());
            mDeleteTaskCommand.setValue(new Event<>(new Object()));
        }
    }

    public void editTask() {
        mEditTaskCommand.setValue(new Event<>(new Object()));
    }

    public LiveData<Event<Integer>> getSnackbarMessage() {
        return mSnackbarText;
    }

    public MutableLiveData<Event<Object>> getEditTaskCommand() {
        return mEditTaskCommand;
    }

    public MutableLiveData<Event<Object>> getDeleteTaskCommand() {
        return mDeleteTaskCommand;
    }

    public void setCompleted(boolean completed) {
        if (isDataLoading.getValue()) {
            return;
        }
        Task task = this.task.getValue();
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
            isDataLoading.setValue(true);
            mTasksRepository.getTask(taskId, this);
        }
    }

    public void setTask(Task task) {
        this.task.setValue(task);
        if (task != null) {
            completed.setValue(task.isCompleted());
            isDataAvailable.setValue(task != null);
        }
    }

    @Override
    public void onTaskLoaded(Task task) {
        setTask(task);
        isDataLoading.setValue(false);
    }

    @Override
    public void onDataNotAvailable() {
        task.setValue(null);
        isDataLoading.setValue(false);
    }

    public void onRefresh() {
        if (task.getValue() != null) {
            start(task.getValue().getId());
        }
    }

    @Nullable
    protected String getTaskId() {
        return task.getValue().getId();
    }

    private void showSnackbarMessage(@StringRes Integer message) {
        mSnackbarText.setValue(new Event<>(message));
    }
}
