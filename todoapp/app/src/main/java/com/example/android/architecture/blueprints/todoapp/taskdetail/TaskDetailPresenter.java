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

import com.example.android.architecture.blueprints.todoapp.BasePresenter;
import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;
import com.example.android.architecture.blueprints.todoapp.util.schedulers.BaseSchedulerProvider;

import rx.Observer;
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Listens to user actions from the UI ({@link TaskDetailFragment}), retrieves the data and updates
 * the UI as required.
 */
public class TaskDetailPresenter implements BasePresenter {

    @NonNull
    private final TasksRepository mTasksRepository;

    @NonNull
    private final TaskDetailContract.View mTaskDetailView;

    @NonNull
    private final BaseSchedulerProvider mSchedulerProvider;

    @Nullable
    private String mTaskId;

    @NonNull
    private CompositeSubscription mSubscriptions;

    public TaskDetailPresenter(@Nullable String taskId,
                               @NonNull TasksRepository tasksRepository,
                               @NonNull TaskDetailContract.View taskDetailView,
                               @NonNull BaseSchedulerProvider schedulerProvider) {
        this.mTaskId = taskId;
        mTasksRepository = checkNotNull(tasksRepository, "tasksRepository cannot be null!");
        mTaskDetailView = checkNotNull(taskDetailView, "taskDetailView cannot be null!");
        mSchedulerProvider = checkNotNull(schedulerProvider, "schedulerProvider cannot be null");

        mSubscriptions = new CompositeSubscription();
        mTaskDetailView.setPresenter(this);
    }

    @Override
    public void subscribe() {
        openTask();
    }

    @Override
    public void unsubscribe() {
        mSubscriptions.clear();
    }

    private void openTask() {
        if (null == mTaskId || mTaskId.isEmpty()) {
            mTaskDetailView.showMissingTask();
            return;
        }

        mTaskDetailView.setLoadingIndicator(true);
        Subscription repoSubscription = mTasksRepository
                .getTask(mTaskId)
                .subscribeOn(mSchedulerProvider.computation())
                .observeOn(mSchedulerProvider.ui())
                .subscribe(new Observer<Task>() {
                    @Override
                    public void onCompleted() {
                        mTaskDetailView.setLoadingIndicator(false);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Task task) {
                        showTask(task);
                    }
                });
        mSubscriptions.add(repoSubscription);

        Subscription editSubscription = mTaskDetailView
                .editTask()
                .subscribe(aVoid -> editTask());
        mSubscriptions.add(editSubscription);

        Subscription deleteSubscription = mTaskDetailView
                .deleteTask()
                .subscribe(aVoid -> deleteTask());
        mSubscriptions.add(deleteSubscription);

        Subscription completeSubscription = mTaskDetailView
                .completeTask()
                .subscribe(aVoid -> completeTask());
        mSubscriptions.add(completeSubscription);

        Subscription activateSubscription = mTaskDetailView
                .activateTask()
                .subscribe(aVoid -> activateTask());
        mSubscriptions.add(activateSubscription);
    }

    private void editTask() {
        if (null == mTaskId || mTaskId.isEmpty()) {
            mTaskDetailView.showMissingTask();
            return;
        }
        mTaskDetailView.showEditTask(mTaskId);
    }

    private void deleteTask() {
        mTasksRepository.deleteTask(mTaskId);
        mTaskDetailView.showTaskDeleted();
    }

    private void completeTask() {
        if (null == mTaskId || mTaskId.isEmpty()) {
            mTaskDetailView.showMissingTask();
            return;
        }
        mTasksRepository.completeTask(mTaskId);
        mTaskDetailView.showTaskMarkedComplete();
    }

    private void activateTask() {
        if (null == mTaskId || mTaskId.isEmpty()) {
            mTaskDetailView.showMissingTask();
            return;
        }
        mTasksRepository.activateTask(mTaskId);
        mTaskDetailView.showTaskMarkedActive();
    }

    private void showTask(@NonNull Task task) {
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
