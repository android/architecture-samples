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

import com.example.android.architecture.blueprints.todoapp.Subscription;
import com.example.android.architecture.blueprints.todoapp.UseCase;
import com.example.android.architecture.blueprints.todoapp.UseCaseHandler;
import com.example.android.architecture.blueprints.todoapp.addedittask.domain.usecase.DeleteTask;
import com.example.android.architecture.blueprints.todoapp.addedittask.domain.usecase.GetTask;
import com.example.android.architecture.blueprints.todoapp.tasks.domain.model.Task;
import com.example.android.architecture.blueprints.todoapp.tasks.domain.usecase.ActivateTask;
import com.example.android.architecture.blueprints.todoapp.tasks.domain.usecase.CompleteTask;

/**
 * Listens to user actions from the UI ({@link TaskDetailFragment}), retrieves the data and updates
 * the UI as required.
 */
public class TaskDetailPresenter implements TaskDetailContract.Presenter {

    private final TaskDetailContract.View mTaskDetailView;
    private final UseCaseHandler mUseCaseHandler;
    private final GetTask mGetTask;
    private final CompleteTask mCompleteTask;
    private final ActivateTask mActivateTask;
    private final DeleteTask mDeleteTask;
    private Subscription mGetTaskSubscription;
    private Subscription mCompleteTaskSubscription;
    private Subscription mActivateTaskSubscription;
    private Subscription mDeleteTaskSubscription;

    @Nullable
    private String mTaskId;

    public TaskDetailPresenter(@NonNull UseCaseHandler useCaseHandler,
            @Nullable String taskId,
            @NonNull TaskDetailContract.View taskDetailView,
            @NonNull GetTask getTask,
            @NonNull CompleteTask completeTask,
            @NonNull ActivateTask activateTask,
            @NonNull DeleteTask deleteTask) {
        mTaskId = taskId;
        mUseCaseHandler = checkNotNull(useCaseHandler, "useCaseHandler cannot be null!");
        mTaskDetailView = checkNotNull(taskDetailView, "taskDetailView cannot be null!");
        mGetTask = checkNotNull(getTask, "getTask cannot be null!");
        mCompleteTask = checkNotNull(completeTask, "completeTask cannot be null!");
        mActivateTask = checkNotNull(activateTask, "activateTask cannot be null!");
        mDeleteTask = checkNotNull(deleteTask, "deleteTask cannot be null!");
        mTaskDetailView.setPresenter(this);
    }

    @Override
    public void start() {
        openTask();
    }

    @Override
    public void stop() {
        if (mGetTaskSubscription != null) {
            mGetTaskSubscription.unsubscribe();
        }
        if (mCompleteTaskSubscription != null) {
            mCompleteTaskSubscription.unsubscribe();
        }
        if (mActivateTaskSubscription != null) {
            mActivateTaskSubscription.unsubscribe();
        }
        if (mDeleteTaskSubscription != null) {
            mDeleteTaskSubscription.unsubscribe();
        }
    }

    private void openTask() {
        if (mTaskId == null || mTaskId.isEmpty()) {
            mTaskDetailView.showMissingTask();
            return;
        }

        mTaskDetailView.setLoadingIndicator(true);

        mGetTaskSubscription = mUseCaseHandler.execute(mGetTask, new GetTask.RequestValues(mTaskId),
                new UseCase.Callback<GetTask.ResponseValue>() {
                    @Override
                    public void onStart() {

                    }

                    @Override
                    public void onNext(GetTask.ResponseValue responseValues) {
                        Task task = responseValues.getTask();

                        // The view may not be able to handle UI updates anymore
                        if (!mTaskDetailView.isActive()) {
                            return;
                        }
                        mTaskDetailView.setLoadingIndicator(false);
                        showTask(task);
                    }

                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable exception) {
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
        if (mTaskId == null || mTaskId.isEmpty()) {
            mTaskDetailView.showMissingTask();
            return;
        }
        mTaskDetailView.showEditTask(mTaskId);
    }

    @Override
    public void deleteTask() {
        mDeleteTaskSubscription = mUseCaseHandler.execute(mDeleteTask, new DeleteTask.RequestValues(mTaskId),
                new UseCase.Callback<DeleteTask.ResponseValue>() {
                    @Override
                    public void onStart() {

                    }

                    @Override
                    public void onNext(DeleteTask.ResponseValue responseValues) {

                    }

                    @Override
                    public void onCompleted() {
                        mTaskDetailView.showTaskDeleted();
                    }

                    @Override
                    public void onError(Throwable exception) {
                        // Show error, log, etc.
                    }
                });
    }

    @Override
    public void completeTask() {
        if (mTaskId == null || mTaskId.isEmpty()) {
            mTaskDetailView.showMissingTask();
            return;
        }

        mCompleteTaskSubscription = mUseCaseHandler.execute(mCompleteTask, new CompleteTask.RequestValues(mTaskId),
                new UseCase.Callback<CompleteTask.ResponseValue>() {
                    @Override
                    public void onStart() {

                    }

                    @Override
                    public void onNext(CompleteTask.ResponseValue responseValues) {

                    }

                    @Override
                    public void onCompleted() {
                        mTaskDetailView.showTaskMarkedComplete();
                    }

                    @Override
                    public void onError(Throwable exception) {
                        // Show error, log, etc.
                    }
                });
    }

    @Override
    public void activateTask() {
        if (mTaskId == null || mTaskId.isEmpty()) {
            mTaskDetailView.showMissingTask();
            return;
        }
        mActivateTaskSubscription = mUseCaseHandler.execute(mActivateTask, new ActivateTask.RequestValues(mTaskId),
                new UseCase.Callback<ActivateTask.ResponseValue>() {
                    @Override
                    public void onStart() {

                    }

                    @Override
                    public void onNext(ActivateTask.ResponseValue responseValues) {

                    }

                    @Override
                    public void onCompleted() {
                        mTaskDetailView.showTaskMarkedActive();
                    }

                    @Override
                    public void onError(Throwable exception) {
                        // Show error, log, etc.
                    }
                });
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
