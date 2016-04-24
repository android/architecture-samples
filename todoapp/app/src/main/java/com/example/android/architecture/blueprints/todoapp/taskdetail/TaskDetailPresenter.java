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

import android.database.Cursor;
import android.support.annotation.NonNull;

import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksInteractor;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Listens to user actions from the UI ({@link TaskDetailFragment}), retrieves the data and updates
 * the UI as required.
 */
public class TaskDetailPresenter implements TaskDetailContract.Presenter, TasksInteractor.GetTasksCallback {

    private TaskDetailContract.View mTaskDetailView;

    private Task mTask;
    private String mTaskId;

    @NonNull
    private final TasksInteractor mTasksInteractor;

    public TaskDetailPresenter(@NonNull String taskId,
                               @NonNull TasksInteractor tasksInteractor,
                               @NonNull TaskDetailContract.View taskDetailView) {
        mTaskId = checkNotNull(taskId, "taskId cannot be null!");
        mTasksInteractor = checkNotNull(tasksInteractor, "tasksOperations cannot be null!");
        mTaskDetailView = checkNotNull(taskDetailView, "taskDetailView cannot be null!");
        mTaskDetailView.setPresenter(this);
    }

    @Override
    public void start() {
        mTaskDetailView.setLoadingIndicator(true);
        mTasksInteractor.getTask(mTaskId, this);
    }

    @Override
    public void editTask() {
        if (null == mTask) {
            mTaskDetailView.showMissingTask();
            return;
        }
        mTaskDetailView.showEditTask(mTask.getId());
    }

    @Override
    public void deleteTask() {
        mTasksInteractor.deleteTask(mTask);
        mTaskDetailView.showTaskDeleted();
    }

    public void completeTask() {
        if (null == mTask) {
            mTaskDetailView.showMissingTask();
            return;
        }
        mTasksInteractor.completeTask(mTask);
        mTaskDetailView.showTaskMarkedComplete();
    }

    @Override
    public void activateTask() {
        if (null == mTask) {
            mTaskDetailView.showMissingTask();
            return;
        }
        mTasksInteractor.activateTask(mTask);
        mTaskDetailView.showTaskMarkedActive();
    }

    private void showTask(Cursor data) {
        mTask = Task.from(data);

        String title = mTask.getTitle();
        String description = mTask.getDescription();

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
        mTaskDetailView.showCompletionStatus(mTask.isCompleted());
        mTaskDetailView.setLoadingIndicator(false);
    }

    @Override
    public void onDataLoaded(Cursor data) {
        if (data.moveToLast()) {
            showTask(data);
        } else {
            mTaskDetailView.showMissingTask();
        }
    }

    @Override
    public void onDataNotAvailable() {
        mTaskDetailView.showMissingTask();
    }
}
