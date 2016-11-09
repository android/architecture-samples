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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskContract;
import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskPresenter;
import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;
import com.example.android.architecture.blueprints.todoapp.taskdetail.TaskDetailContract;
import com.example.android.architecture.blueprints.todoapp.taskdetail.TaskDetailPresenter;
import com.example.android.architecture.blueprints.todoapp.tasks.TasksContract;
import com.example.android.architecture.blueprints.todoapp.tasks.TasksFilterType;
import com.example.android.architecture.blueprints.todoapp.tasks.TasksPresenter;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Presenter for the tablet screen that can act as a Tasks Presenter and a Task Detail Presenter.
 */
public class TasksTabletPresenter implements TasksContract.Presenter, TaskDetailContract.Presenter,
        AddEditTaskContract.Presenter {

    @NonNull private final TasksRepository mTasksRepository;

    @NonNull private TasksPresenter mTasksPresenter;

    private final TasksMvpController mTasksMvpController;

    @Nullable private TaskDetailPresenter mTaskDetailPresenter;

    @Nullable private AddEditTaskPresenter mAddEditPresenter;

    public TasksTabletPresenter(@NonNull TasksRepository tasksRepository,
                                @NonNull TasksPresenter tasksPresenter,
                                TasksMvpController tasksMvpController) {
        mTasksRepository = checkNotNull(tasksRepository);
        mTasksPresenter = checkNotNull(tasksPresenter);
        mTasksMvpController = tasksMvpController;
    }

    public void setTaskDetailPresenter(TaskDetailPresenter taskDetailPresenter) {
        mTaskDetailPresenter = taskDetailPresenter;
    }

    public void setAddEditPresenter(AddEditTaskPresenter addEditPresenter) {
        mAddEditPresenter = addEditPresenter;
    }

    /* TasksContract.Presenter methods can be called with or without a detail pane */

    @Override
    public void onTaskAdded() {
        mTasksPresenter.onTaskAdded();
        if (mTaskDetailPresenter != null) {
            mTaskDetailPresenter.loadTask();
        }
        mAddEditPresenter = null;
    }

    @Override
    public void loadTasks(boolean forceUpdate) {
        mTasksPresenter.loadTasks(forceUpdate);
    }

    @Override
    public void addNewTask() {
        mTasksMvpController.addNewTask();
    }

    @Override
    public void openTaskDetails(@NonNull Task requestedTask) {
        mTaskDetailPresenter.setDetailTaskId(requestedTask.getId());
        mTaskDetailPresenter.loadTask();
    }

    @Override
    public void completeTask(@NonNull Task completedTask) {
        mTasksPresenter.completeTask(completedTask);
        // Refresh detail view
        if (mTaskDetailPresenter != null && mTaskDetailPresenter.getDetailTaskId() != null) {
            if (mTaskDetailPresenter.getDetailTaskId().equals(completedTask.getId())) {
                mTaskDetailPresenter.loadTask();
            }
        }
    }

    @Override
    public void activateTask(@NonNull Task activeTask) {
        mTasksPresenter.activateTask(activeTask);
        // Refresh detail view
        if (mTaskDetailPresenter != null && mTaskDetailPresenter.getDetailTaskId() != null) {
            if (mTaskDetailPresenter.getDetailTaskId().equals(activeTask.getId())) {
                mTaskDetailPresenter.loadTask();
            }
        }
    }

    @Override
    public void clearCompletedTasks() {
        mTasksPresenter.clearCompletedTasks();

        // If task on detail has just been cleared, update it.
        if (mTaskDetailPresenter != null) {
            String taskId = mTaskDetailPresenter.getDetailTaskId();
            if (taskId != null) {
                mTasksRepository.getTask(taskId,
                        new TasksDataSource.GetTaskCallback() {
                            @Override
                            public void onTaskLoaded(Task task) {
                                if (task == null) {
                                    mTaskDetailPresenter.setDetailTaskId(null);
                                }
                            }

                            @Override
                            public void onDataNotAvailable() {
                                mTaskDetailPresenter.setDetailTaskId(null);
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
    public void setSelectedTaskId(@Nullable String taskId) {
        mTasksPresenter.setSelectedTaskId(taskId);
    }

    @Override
    public void setFiltering(TasksFilterType requestType) {
        mTasksPresenter.setFiltering(requestType);
    }

    /* TaskDetailContract.Presenter methods */

    @Override
    public void editTask() {
        assert mTaskDetailPresenter != null; // Only called from detail view
        mTasksMvpController.editTask(getDetailTaskId());
    }

    @Override
    public void deleteTask() {
        assert mTaskDetailPresenter != null; // Only called from detail view
        if (getDetailTaskId() != null) {
            mTasksRepository.deleteTask(getDetailTaskId());
        }
        mTaskDetailPresenter.setDetailTaskId(null); // Show an empty detail view
        mTaskDetailPresenter.loadTask();
        mTasksPresenter.loadTasks(false); // Reload the list
    }

    @Override
    public void completeTask() {
        assert mTaskDetailPresenter != null; // Only called from detail view
        mTaskDetailPresenter.completeTask();
        mTasksPresenter.loadTasks(false);
    }

    @Override
    public void activateTask() {
        assert mTaskDetailPresenter != null; // Only called from detail view
        mTaskDetailPresenter.activateTask();
        mTasksPresenter.loadTasks(false);
    }

    @Override
    public void loadTask() {
        assert mTaskDetailPresenter != null; // Only called from detail view
        mTaskDetailPresenter.loadTask();
    }

    @Override
    public void setDetailTaskId(String taskId) {
        if(mTaskDetailPresenter != null) {
            mTaskDetailPresenter.setDetailTaskId(taskId);
        }
        mTasksPresenter.setSelectedTaskId(taskId);
    }

    @Override
    public String getDetailTaskId() {
        if (mTaskDetailPresenter != null) {
            return mTaskDetailPresenter.getDetailTaskId();
        }
        return null;
    }

    /* AddEditTaskContract.Presenter methods */

    @Override
    public void saveTask(String title, String description) {
        if (mAddEditPresenter != null) {
            mAddEditPresenter.saveTask(title, description);
        }
        mTasksPresenter.onTaskAdded();
        mTasksPresenter.loadTasks(false);
        if (mTaskDetailPresenter != null) {
            mTaskDetailPresenter.loadTask();
        }
    }

    @Override
    public void populateTask() {
        if (mAddEditPresenter != null) {
            mAddEditPresenter.populateTask();
        }
    }

    @Nullable
    @Override
    public String getAddEditTaskId() {
        if (mAddEditPresenter != null) {
            return mAddEditPresenter.getAddEditTaskId();
        }
        return null;
    }

    @Override
    public void onAddEditStops() {
        mAddEditPresenter = null;
    }
}
