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

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.example.android.architecture.blueprints.todoapp.R;
import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;
import com.google.common.base.Strings;

import rx.Completable;
import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Listens to user actions from the UI ({@link TaskDetailFragment}), retrieves the data and edits,
 * deletes, updates, activates and completes the task.
 */
public class TaskDetailViewModel {

    @NonNull
    private final TasksRepository mTasksRepository;

    @Nullable
    private final String mTaskId;

    @NonNull
    private final TaskDetailNavigator mNavigator;

    @NonNull
    private final BehaviorSubject<Boolean> mLoadingSubject;

    @NonNull
    private final PublishSubject<Integer> mSnackbarText;

    public TaskDetailViewModel(@Nullable String taskId,
                               @NonNull TasksRepository tasksRepository,
                               @NonNull TaskDetailNavigator navigator) {
        mTaskId = taskId;
        mTasksRepository = checkNotNull(tasksRepository, "tasksRepository cannot be null!");
        mNavigator = checkNotNull(navigator, "navigator cannot be null");
        mLoadingSubject = BehaviorSubject.create(false);
        mSnackbarText = PublishSubject.create();
    }

    /**
     * @return a stream notifying on whether the loading is in progress or not
     */
    @NonNull
    public Observable<Boolean> getLoadingIndicator() {
        return mLoadingSubject.asObservable();
    }

    /**
     * @return a stream containing the task model. An error will be emitted
     * if the task id is invalid. The loading is updated before retrieving the task and when the
     * task has been retrieved.
     */
    @NonNull
    public Observable<TaskModel> getTask() {
        if (Strings.isNullOrEmpty(mTaskId)) {
            return Observable.error(new Exception("Task id null or empty"));
        }

        return mTasksRepository.getTask(mTaskId)
                .map(this::createModel)
                .doOnSubscribe(() -> mLoadingSubject.onNext(true))
                .doOnTerminate(() -> mLoadingSubject.onNext(false));
    }

    @NonNull
    private TaskModel createModel(Task task) {
        boolean isTitleVisible = !Strings.isNullOrEmpty(task.getTitle());
        boolean isDescriptionVisible = !Strings.isNullOrEmpty(task.getDescription());

        return new TaskModel(isTitleVisible, task.getTitle(), isDescriptionVisible,
                task.getDescription(), task.isCompleted());
    }

    /**
     * @return a stream that emits when a snackbar should be displayed. The stream contains the
     * snackbar text
     */
    @NonNull
    public Observable<Integer> getSnackbarText() {
        return mSnackbarText.asObservable();
    }

    /**
     * Handle the response received on onActivityResult.
     *
     * @param requestCode the request with which the Activity was opened.
     * @param resultCode  the result of the Activity.
     */
    public void handleActivityResult(int requestCode, int resultCode) {
        if (TaskDetailActivity.REQUEST_EDIT_TASK == requestCode
                && Activity.RESULT_OK == resultCode) {
            mNavigator.onTaskEdited();
        }
    }

    /**
     * @return a stream that completes when the task was edited.
     */
    @NonNull
    public Completable editTask() {
        return Completable.fromAction(() -> {
            if (Strings.isNullOrEmpty(mTaskId)) {
                throw new RuntimeException("Task id null or empty");
            }
            mNavigator.onStartEditTask(mTaskId);
        });
    }

    /**
     * Deletes the task from repository. Emits when the task has been deleted. An error is emitted
     * if the task id is invalid.
     *
     * @return a stream notifying about the deletion of the task
     */
    @NonNull
    public Completable deleteTask() {
        return Completable.fromAction(() -> {
            if (Strings.isNullOrEmpty(mTaskId)) {
                throw new RuntimeException("Task id null or empty");
            }
            mTasksRepository.deleteTask(mTaskId);
            mNavigator.onTaskDeleted();
        });
    }

    /**
     * Marks a task as active or completed in the repository. Emits when the task has been marked as
     * active or completed. An error is emitted if the task id is invalid.
     */
    @NonNull
    public Completable taskCheckChanged(final boolean checked) {
        return Completable.fromAction(() -> {
            if (Strings.isNullOrEmpty(mTaskId)) {
                throw new RuntimeException("Task id null or empty");
            }
            if (checked) {
                completeTask();
            } else {
                activateTask();
            }
        });
    }

    /**
     * Marks a task as completed in the repository.
     */
    @NonNull
    private void completeTask() {
        mTasksRepository.completeTask(mTaskId);
        mSnackbarText.onNext(R.string.task_marked_complete);
    }

    /**
     * Marks a task as active in the repository.
     */
    @NonNull
    private void activateTask() {
        mTasksRepository.activateTask(mTaskId);
        mSnackbarText.onNext(R.string.task_marked_active);
    }

}
