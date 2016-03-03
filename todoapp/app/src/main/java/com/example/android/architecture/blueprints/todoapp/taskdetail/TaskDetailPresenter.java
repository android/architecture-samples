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

import android.support.annotation.Nullable;

import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;

import javax.inject.Inject;

/**
 * Listens to user actions from the UI ({@link TaskDetailFragment}), retrieves the data and updates
 * the UI as required.
 * <p />
 * By marking the constructor with {@code @Inject}, Dagger injects the dependencies required to
 * create an instance of the TaskDetailPresenter (if it fails, it emits a compiler error). It uses
 * {@link TaskDetailPresenterModule} to do so, and the constructed instance is available in
 * {@link TaskDetailFragmentComponent}.
 * <p />
 * Dagger generated code doesn't require public access to the constructor or class, and
 * therefore, to ensure the developer doesn't instantiate the class manually and bypasses Dagger,
 * it's good practice minimise the visibility of the class/constructor as much as possible.
 **/
final class TaskDetailPresenter implements TaskDetailContract.UserActionsListener {

    private final TasksRepository mTasksRepository;

    private final TaskDetailContract.View mTaskDetailView;

    /**
     * Dagger strictly enforces that arguments not marked with {@code @Nullable} are not injected
     * with {@code @Nullable} values.
     */
    @Inject
    TaskDetailPresenter(TasksRepository tasksRepository,
            TaskDetailContract.View taskDetailView) {
        mTasksRepository = tasksRepository;
        mTaskDetailView = taskDetailView;
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
                if (mTaskDetailView.isInactive()) {
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
                if (mTaskDetailView.isInactive()) {
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
