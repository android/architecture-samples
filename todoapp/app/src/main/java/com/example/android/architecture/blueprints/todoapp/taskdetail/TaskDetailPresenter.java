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
import com.google.common.base.Strings;

import javax.inject.Inject;

/**
 * Listens to user actions from the UI ({@link TaskDetailFragment}), retrieves the data and updates
 * the UI as required.
 * <p>
 * By marking the constructor with {@code @Inject}, Dagger injects the dependencies required to
 * create an instance of the TaskDetailPresenter (if it fails, it emits a compiler error). It uses
 * {@link TaskDetailPresenterModule} to do so.
 * <p>
 * Dagger generated code doesn't require public access to the constructor or class, and
 * therefore, to ensure the developer doesn't instantiate the class manually and bypasses Dagger,
 * it's good practice minimise the visibility of the class/constructor as much as possible.
 */
final class TaskDetailPresenter implements TaskDetailContract.Presenter {

    private TasksRepository mTasksRepository;
    @Nullable
    private TaskDetailContract.View mTaskDetailView;

    /**
     * Dagger strictly enforces that arguments not marked with {@code @Nullable} are not injected
     * with {@code @Nullable} values.
     */
    @Nullable
    private String mTaskId;

    /**
     * Dagger strictly enforces that arguments not marked with {@code @Nullable} are not injected
     * with {@code @Nullable} values.
     */
    @Inject
    TaskDetailPresenter(@Nullable String taskId,
                        TasksRepository tasksRepository) {
        mTasksRepository = tasksRepository;
        mTaskId = taskId;
    }


    private void openTask() {
        if (Strings.isNullOrEmpty(mTaskId)) {
            if (mTaskDetailView != null) {
                mTaskDetailView.showMissingTask();
            }
            return;
        }

        if (mTaskDetailView != null) {
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
        if (Strings.isNullOrEmpty(mTaskId)) {
            if (mTaskDetailView != null) {
                mTaskDetailView.showMissingTask();
            }
            return;
        }
        if (mTaskDetailView != null) {
            mTaskDetailView.showEditTask(mTaskId);
        }
    }

    @Override
    public void deleteTask() {
        if (Strings.isNullOrEmpty(mTaskId)) {
            if (mTaskDetailView != null) {
                mTaskDetailView.showMissingTask();
            }
            return;
        }
        mTasksRepository.deleteTask(mTaskId);
        if (mTaskDetailView != null) {
            mTaskDetailView.showTaskDeleted();
        }
    }

    @Override
    public void completeTask() {
        if (Strings.isNullOrEmpty(mTaskId)) {
            if (mTaskDetailView != null) {
                mTaskDetailView.showMissingTask();
            }
            return;
        }
        mTasksRepository.completeTask(mTaskId);
        if (mTaskDetailView != null) {
            mTaskDetailView.showTaskMarkedComplete();
        }
    }

    @Override
    public void activateTask() {
        if (Strings.isNullOrEmpty(mTaskId)) {
            if (mTaskDetailView != null) {
                mTaskDetailView.showMissingTask();
            }
            return;
        }
        mTasksRepository.activateTask(mTaskId);
        if (mTaskDetailView != null) {
            mTaskDetailView.showTaskMarkedActive();
        }
    }

    @Override
    public void takeView(TaskDetailContract.View taskDetailView) {
        mTaskDetailView = taskDetailView;
        openTask();
    }

    @Override
    public void dropView() {
        mTaskDetailView = null;
    }

    private void showTask(@NonNull Task task) {
        String title = task.getTitle();
        String description = task.getDescription();

        if (Strings.isNullOrEmpty(title)) {
            if (mTaskDetailView != null) {
                mTaskDetailView.hideTitle();
            }
        } else {
            if (mTaskDetailView != null) {
                mTaskDetailView.showTitle(title);
            }
        }

        if (Strings.isNullOrEmpty(description)) {
            if (mTaskDetailView != null) {
                mTaskDetailView.hideDescription();
            }
        } else {
            if (mTaskDetailView != null) {
                mTaskDetailView.showDescription(description);
            }
        }
        if (mTaskDetailView != null) {
            mTaskDetailView.showCompletionStatus(task.isCompleted());
        }
    }
}
