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

import com.example.android.architecture.blueprints.todoapp.AbstractBasePresenter;
import com.example.android.architecture.blueprints.todoapp.UseCase;
import com.example.android.architecture.blueprints.todoapp.UseCaseHandler;
import com.example.android.architecture.blueprints.todoapp.addedittask.domain.usecase.DeleteTask;
import com.example.android.architecture.blueprints.todoapp.addedittask.domain.usecase.GetTask;
import com.example.android.architecture.blueprints.todoapp.tasks.domain.model.Task;
import com.example.android.architecture.blueprints.todoapp.tasks.domain.usecase.ActivateTask;
import com.example.android.architecture.blueprints.todoapp.tasks.domain.usecase.CompleteTask;
import com.google.common.base.Strings;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Listens to user actions from the UI ({@link TaskDetailFragment}), retrieves the data and updates
 * the UI as required.
 */
public class TaskDetailPresenter
        extends AbstractBasePresenter<TaskDetailContract.View>
        implements TaskDetailContract.Presenter {
    private final GetTask mGetTask;
    private final CompleteTask mCompleteTask;
    private final ActivateTask mActivateTask;
    private final DeleteTask mDeleteTask;

    @Nullable
    private String mTaskId;


    public TaskDetailPresenter(@NonNull UseCaseHandler useCaseHandler,
            @Nullable String taskId,
            @NonNull TaskDetailContract.View taskDetailView,
            @NonNull GetTask getTask,
            @NonNull CompleteTask completeTask,
            @NonNull ActivateTask activateTask,
            @NonNull DeleteTask deleteTask) {
        super(taskDetailView, useCaseHandler);

        mTaskId = taskId;
        mGetTask = checkNotNull(getTask, "getTask cannot be null!");
        mCompleteTask = checkNotNull(completeTask, "completeTask cannot be null!");
        mActivateTask = checkNotNull(activateTask, "activateTask cannot be null!");
        mDeleteTask = checkNotNull(deleteTask, "deleteTask cannot be null!");

        mView.setPresenter(this);
    }

    @Override
    public void start() {
        openTask();
    }

    private void openTask() {
        if (Strings.isNullOrEmpty(mTaskId)) {
            mView.showMissingTask();
            return;
        }

        mView.setLoadingIndicator(true);

        schedule(mGetTask, new GetTask.RequestValues(mTaskId),
                new UseCase.UseCaseCallback<GetTask.ResponseValue>() {
                    @Override
                    public void onSuccess(GetTask.ResponseValue response) {
                        Task task = response.getTask();

                        // The view may not be able to handle UI updates anymore
                        if (!mView.isActive()) {
                            return;
                        }
                        mView.setLoadingIndicator(false);
                        showTask(task);
                    }

                    @Override
                    public void onError() {
                        // The view may not be able to handle UI updates anymore
                        if (!mView.isActive()) {
                            return;
                        }
                        mView.showMissingTask();
                    }
                });
    }

    @Override
    public void editTask() {
        if (Strings.isNullOrEmpty(mTaskId)) {
            mView.showMissingTask();
            return;
        }
        mView.showEditTask(mTaskId);
    }

    @Override
    public void deleteTask() {
        schedule(mDeleteTask, new DeleteTask.RequestValues(mTaskId),
                new UseCase.UseCaseCallback<DeleteTask.ResponseValue>() {
                    @Override
                    public void onSuccess(DeleteTask.ResponseValue response) {
                        mView.showTaskDeleted();
                    }

                    @Override
                    public void onError() {
                        // Show error, log, etc.
                    }
                });
    }

    @Override
    public void completeTask() {
        if (Strings.isNullOrEmpty(mTaskId)) {
            mView.showMissingTask();
            return;
        }

        schedule(mCompleteTask, new CompleteTask.RequestValues(mTaskId),
                new UseCase.UseCaseCallback<CompleteTask.ResponseValue>() {
                    @Override
                    public void onSuccess(CompleteTask.ResponseValue response) {
                        mView.showTaskMarkedComplete();
                    }

                    @Override
                    public void onError() {
                        // Show error, log, etc.
                    }
                });
    }

    @Override
    public void activateTask() {
        if (Strings.isNullOrEmpty(mTaskId)) {
            mView.showMissingTask();
            return;
        }
        schedule(mActivateTask, new ActivateTask.RequestValues(mTaskId),
                new UseCase.UseCaseCallback<ActivateTask.ResponseValue>() {
                    @Override
                    public void onSuccess(ActivateTask.ResponseValue response) {
                        mView.showTaskMarkedActive();
                    }

                    @Override
                    public void onError() {
                        // Show error, log, etc.
                    }
                });
    }

    private void showTask(@NonNull Task task) {
        String title = task.getTitle();
        String description = task.getDescription();

        if (Strings.isNullOrEmpty(title)) {
            mView.hideTitle();
        } else {
            mView.showTitle(title);
        }

        if (Strings.isNullOrEmpty(description)) {
            mView.hideDescription();
        } else {
            mView.showDescription(description);
        }
        mView.showCompletionStatus(task.isCompleted());
    }
}
