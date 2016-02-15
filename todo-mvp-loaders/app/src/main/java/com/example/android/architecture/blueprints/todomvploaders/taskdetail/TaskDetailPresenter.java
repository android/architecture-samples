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

package com.example.android.architecture.blueprints.todomvploaders.taskdetail;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import com.example.android.architecture.blueprints.todomvploaders.data.Task;
import com.example.android.architecture.blueprints.todomvploaders.data.source.TaskLoader;
import com.example.android.architecture.blueprints.todomvploaders.data.source.TasksRepository;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Listens to user actions from the UI ({@link TaskDetailFragment}), retrieves the data and updates
 * the UI as required.
 */
public class TaskDetailPresenter implements TaskDetailContract.UserActionsListener,
        LoaderManager.LoaderCallbacks<Task> {

    private static final int TASK_QUERY = 3;

    private TasksRepository mTasksRepository;

    private TaskDetailContract.View mTaskDetailView;

    private TaskLoader mTaskLoader;

    @Nullable
    private String mTaskId;

    public TaskDetailPresenter(@Nullable String taskId,
                               @NonNull TasksRepository tasksRepository,
                               @NonNull TaskDetailContract.View taskDetailView,
                               @NonNull TaskLoader taskLoader) {
        mTaskId = taskId;
        mTasksRepository = checkNotNull(tasksRepository, "tasksRepository cannot be null!");
        mTaskDetailView = checkNotNull(taskDetailView, "taskDetailView cannot be null!");
        mTaskLoader = checkNotNull(taskLoader, "taskLoader cannot be nulL!");
    }

    /**
     * This starts the {@link LoaderManager}, querying the task. It returns the TaskDetailPresenter
     * so it can be chained with the constructor. This isn't called from the constructor to enable
     * writing unit tests for the non loader methods in the TaskDetailPresenter (creating an
     * instance from a unit test would fail if this method were called from it).
     */
    public TaskDetailPresenter startLoader(TaskDetailFragment fragment) {
        fragment.getLoaderManager().initLoader(TASK_QUERY, null, this);
        return this;
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
        mTaskDetailView.setProgressIndicator(false);
    }

    @Override
    public Loader<Task> onCreateLoader(int id, Bundle args) {
        if (mTaskId == null) {
            return null;
        }
        mTaskDetailView.setProgressIndicator(true);
        return mTaskLoader;
    }

    @Override
    public void onLoadFinished(Loader<Task> loader, Task data) {
        if (data != null) {
            showTask(data);
        } else {
            mTaskDetailView.showMissingTask();
        }
    }

    @Override
    public void onLoaderReset(Loader<Task> loader) {

    }
}
