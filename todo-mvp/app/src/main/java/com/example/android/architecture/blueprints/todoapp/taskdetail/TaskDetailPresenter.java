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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Listens to user actions from the UI ({@link TaskDetailFragment}), retrieves the data and updates
 * the UI as required.
 */
public class TaskDetailPresenter implements TaskDetailContract.UserActionsListener {

    private final TasksRepository mTasksRepository;

    private final TaskDetailContract.View mTaskDetailView;

    public TaskDetailPresenter(@NonNull TasksRepository tasksRepository,
                               @NonNull TaskDetailContract.View taskDetailView) {
        mTasksRepository = checkNotNull(tasksRepository, "tasksRepository cannot be null!");
        mTaskDetailView = checkNotNull(taskDetailView, "taskDetailView cannot be null!");
        mTaskDetailView.setActionListener(this);
    }

    @Override
    public void openTask(@Nullable String taskId) {
        if (null == taskId || taskId.isEmpty()) {
            mTaskDetailView.showMissingTask();
            return;
        }

        mTaskDetailView.setProgressIndicator(true);
        mTasksRepository.getTask(taskId, new TasksDataSource.GetTaskCallback() {
            @Override
            public void onTaskLoaded(Task task) {
                // The view may not be able to handle UI updates anymore
                if (!mTaskDetailView.isActive()) {
                    return;
                }
                mTaskDetailView.setProgressIndicator(false);
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
    public void editTask(@Nullable String taskId) {
        if (null == taskId || taskId.isEmpty()) {
            mTaskDetailView.showMissingTask();
            return;
        }
        mTaskDetailView.showEditTask(taskId);
    }

    @Override
    public void deleteTask(@Nullable String taskId) {
        mTasksRepository.deleteTask(taskId);
        mTaskDetailView.showTaskDeleted();
    }

    public void completeTask(@Nullable String taskId) {
        if (null == taskId || taskId.isEmpty()) {
            mTaskDetailView.showMissingTask();
            return;
        }
        mTasksRepository.completeTask(taskId);
        mTaskDetailView.showTaskMarkedComplete();
    }

    @Override
    public void activateTask(@Nullable String taskId) {
        if (null == taskId || taskId.isEmpty()) {
            mTaskDetailView.showMissingTask();
            return;
        }
        mTasksRepository.activateTask(taskId);
        mTaskDetailView.showTaskMarkedActive();
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
}
