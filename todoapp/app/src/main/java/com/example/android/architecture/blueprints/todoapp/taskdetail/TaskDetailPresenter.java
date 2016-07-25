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

import static com.google.common.base.Preconditions.checkNotNull;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;
import com.example.android.architecture.blueprints.todoapp.tasks.TasksNavigator;
import com.example.android.architecture.blueprints.todoapp.tasks.TasksPresenter;

/**
 * Listens to user actions from the UI ({@link TaskDetailFragment}), retrieves the data and updates
 * the UI as required.
 */
public class TaskDetailPresenter implements TaskDetailContract.Presenter {

    @NonNull private final TasksRepository mTasksRepository;

    private final TaskDetailContract.View mTaskDetailView;

    @NonNull private TasksPresenter mTasksPresenter;

    @NonNull  private TasksNavigator mTasksNavigator;

    @Nullable
    private String mTaskId;

    public TaskDetailPresenter(@Nullable String taskId,
            @NonNull TasksRepository tasksRepository,
            @NonNull TaskDetailContract.View taskDetailView,
            @NonNull TasksNavigator tasksNavigator) {
        this.mTaskId = taskId;
        mTasksRepository = checkNotNull(tasksRepository, "tasksRepository cannot be null!");
        mTaskDetailView = checkNotNull(taskDetailView, "taskDetailView cannot be null!");
        mTasksNavigator = checkNotNull(tasksNavigator);
    }

    @Override
    public void start() {
        if (mTaskDetailView.isActive()) {
            openTask();
        }
    }

    private void openTask() {
        if (null == mTaskId || mTaskId.isEmpty()) {
            mTaskDetailView.showMissingTask();
            return;
        }
        if (mTaskDetailView.isActive()) {
            mTaskDetailView.setLoadingIndicator(true);
        }
        mTasksRepository.getTask(mTaskId, new TasksDataSource.GetTaskCallback() {
            @Override
            public void onTaskLoaded(Task task) {
                // The view may not be able to handle UI updates anymore
                if (!mTaskDetailView.isActive()) {
                    return;
                }
                mTaskDetailView.setLoadingIndicator(false);
                if (null == task) {
                    mTaskDetailView.showMissingTask();
                } else {
                    showTask(task);
                }
            }

            @Override
            public void onDataNotAvailable() {
                // The view may not be able to handle UI updates anymore
                if (!mTaskDetailView.isActive()) {
                    return;
                }
                mTaskDetailView.showMissingTask();
            }
        });
    }

    @Override
    public void editTask() {
        if (null == mTaskId || mTaskId.isEmpty()) {
            mTaskDetailView.showMissingTask();
            return;
        }
        mTaskDetailView.showEditTask(mTaskId);
    }

    @Override
    public void deleteTask() {
        mTasksRepository.deleteTask(mTaskId);
        if (isTablet()) {
            mTasksPresenter.start();
            mTasksNavigator.removeDetailPane();
        } else {
            mTaskDetailView.showTaskDeleted();
        }
    }

    @Override
    public void completeTask() {
        if (null == mTaskId || mTaskId.isEmpty()) {
            mTaskDetailView.showMissingTask();
            return;
        }
        mTasksRepository.completeTask(mTaskId);
        showTaskMarkedComplete();

        // In tablet mode, ping the other presenter in case it needs to update
        if (mTasksPresenter != null) {
            mTasksPresenter.start();
        }
    }

    public void showTaskMarkedComplete() {
        mTaskDetailView.showTaskMarkedComplete();
    }

    @Override
    public void activateTask() {
        if (null == mTaskId || mTaskId.isEmpty()) {
            mTaskDetailView.showMissingTask();
            return;
        }
        mTasksRepository.activateTask(mTaskId);
        mTaskDetailView.showTaskMarkedActive();

        // In tablet mode, ping the other presenter in case it needs to update
        if (mTasksPresenter != null) {
            mTasksPresenter.start();
        }
    }

    private void showTask(Task task) {
        String title = task.getTitle();
        String description = task.getDescription();

        if (title != null && title.isEmpty()) {
            mTaskDetailView.hideTitle();
        } else {
            mTaskDetailView.showTitle(title);
        }

        if (description != null && description.isEmpty()) {
            mTaskDetailView.hideDescription();
        } else {
            mTaskDetailView.showDescription(description);
        }
        mTaskDetailView.showCompletionStatus(task.isCompleted());
    }

    @Nullable
    public String getTaskId() {
        return mTaskId;
    }

    public void setTasksPresenter(TasksPresenter tasksPresenter) {
        mTasksPresenter = tasksPresenter;
    }

    private boolean isTablet() {
        return mTasksPresenter != null;
    }
}
