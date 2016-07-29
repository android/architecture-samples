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

import static com.google.common.base.Preconditions.checkNotNull;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;
import com.example.android.architecture.blueprints.todoapp.taskdetail.TaskDetailContract;
import com.example.android.architecture.blueprints.todoapp.taskdetail.TaskDetailPresenter;
import com.example.android.architecture.blueprints.todoapp.tasks.TasksContract;
import com.example.android.architecture.blueprints.todoapp.tasks.TasksFilterType;
import com.example.android.architecture.blueprints.todoapp.tasks.TasksPresenter;

/**
 * Presenter for the tablet screen that can act as a Tasks Presenter and a Task Detail Presenter.
 */
public class TasksTabletPresenter implements TasksContract.Presenter, TaskDetailContract.Presenter {

    @NonNull
    private final TasksRepository mTasksRepository;
    @NonNull
    private final TasksTabletNavigator mTasksNavigator;
    @NonNull
    private TasksPresenter mTasksPresenter;
    @Nullable
    private TaskDetailPresenter mTaskDetailPresenter;

    public TasksTabletPresenter(@NonNull TasksRepository tasksRepository,
            @NonNull TasksTabletNavigator tasksNavigator,
            @NonNull TasksPresenter tasksPresenter) {
        mTasksRepository = checkNotNull(tasksRepository);
        mTasksNavigator = checkNotNull(tasksNavigator);
        mTasksPresenter = checkNotNull(tasksPresenter);
    }

    @NonNull public TasksPresenter getTasksPresenter() {
        return mTasksPresenter;
    }

    @Nullable public TaskDetailPresenter getTaskDetailPresenter() {
        return mTaskDetailPresenter;
    }

    public void setTaskDetailPresenter(TaskDetailPresenter taskDetailPresenter) {
        mTaskDetailPresenter = taskDetailPresenter;
    }

    /* TasksContract.Presenter methods can be called with or without a detail pane */

    @Override
    public void onTasksResult(int requestCode, int resultCode) {
        mTasksPresenter.onTasksResult(requestCode, resultCode);
    }

    @Override
    public void loadTasks(boolean forceUpdate) {
        mTasksPresenter.loadTasks(forceUpdate);
    }

    @Override
    public void addNewTask() {
        mTasksPresenter.addNewTask();
    }

    @Override
    public void openTaskDetails(@NonNull Task requestedTask) {
        mTasksNavigator.startTaskDetailForTablet(requestedTask.getId());
    }

    @Override
    public void completeTask(@NonNull Task completedTask) {
        mTasksPresenter.completeTask(completedTask);
        // Refresh detail view
        if (mTaskDetailPresenter != null && mTaskDetailPresenter.getTaskId() != null) {
            if (mTaskDetailPresenter.getTaskId().equals(completedTask.getId())) {
                mTaskDetailPresenter.startTaskDetailPresenter();
            }
        }
    }

    @Override
    public void activateTask(@NonNull Task activeTask) {
        mTasksPresenter.activateTask(activeTask);
        // Refresh detail view
        if (mTaskDetailPresenter != null && mTaskDetailPresenter.getTaskId() != null) {
            if (mTaskDetailPresenter.getTaskId().equals(activeTask.getId())) {
                mTaskDetailPresenter.startTaskDetailPresenter();
            }
        }
    }

    @Override
    public void clearCompletedTasks() {
        mTasksPresenter.clearCompletedTasks();

        // If task on detail has just been cleared, remove fragment.
        if (mTaskDetailPresenter != null) {
            String taskId = mTaskDetailPresenter.getTaskId();
            if (taskId != null) {
                mTasksRepository.getTask(taskId,
                        new TasksDataSource.GetTaskCallback() {
                            @Override
                            public void onTaskLoaded(Task task) {
                                // No-op
                                if (task == null) {
                                    mTasksNavigator.removeDetailPane();
                                }
                            }

                            @Override
                            public void onDataNotAvailable() {
                                mTasksNavigator.removeDetailPane();
                            }
                        });
            }
        }
    }

    @Override
    public TasksFilterType getFiltering() {
        return mTasksPresenter.getFiltering();
    }

    @Override
    public void setFiltering(TasksFilterType requestType) {
        mTasksPresenter.setFiltering(requestType);
    }

    @Override
    public void startTasksPresenter() {
        mTasksPresenter.startTasksPresenter();
    }

    /* TaskDetailContract.Presenter methods */

    @Override
    public void editTask() {
        mTaskDetailPresenter.editTask();
        mTasksPresenter.startTasksPresenter();
    }

    @Override
    public void deleteTask() {
        if (mTaskDetailPresenter.getTaskId() != null) {
            mTasksRepository.deleteTask(mTaskDetailPresenter.getTaskId());
        }
        mTasksNavigator.removeDetailPane();
        mTaskDetailPresenter = null; // Detail is gone, so make sure the detail presenter is cleared
        mTasksPresenter.startTasksPresenter();
    }

    @Override
    public void completeTask() {
        mTaskDetailPresenter.completeTask();
        mTasksPresenter.startTasksPresenter();
    }

    @Override
    public void activateTask() {
        mTaskDetailPresenter.activateTask();
        mTasksPresenter.startTasksPresenter();
    }

    @Override
    public void startTaskDetailPresenter() {
        mTaskDetailPresenter.startTaskDetailPresenter();
    }
}
