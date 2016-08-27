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
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;

import rx.Observable;
import rx.functions.Action0;
import rx.functions.Func1;
import rx.subjects.BehaviorSubject;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Listens to user actions from the UI ({@link TaskDetailFragment}), retrieves the data and edits,
 * deletes, updates, activates and completes the task.
 */
public class TaskDetailViewModel {

    @NonNull
    private final TasksRepository mTasksRepository;

    @Nullable
    private String mTaskId;

    @NonNull
    private final BehaviorSubject<Boolean> mLoadingSubject;

    public TaskDetailViewModel(@Nullable String taskId,
                               @NonNull TasksRepository tasksRepository) {
        this.mTaskId = taskId;
        mTasksRepository = checkNotNull(tasksRepository, "tasksRepository cannot be null!");
        mLoadingSubject = BehaviorSubject.create(false);
    }

    /**
     * @return a stream notifying on whether the loading is in progress or not
     */
    @NonNull
    public Observable<Boolean> getLoadingIndicator() {
        return mLoadingSubject.asObservable();
    }

    /**
     * @return a stream containing the task retrieved from the repository. An error will be emitted
     * if the task id is invalid. The loading is updated before retrieving the task and when the
     * task has been retrieved.
     */
    @NonNull
    public Observable<Task> getTask() {
        if (null == mTaskId || mTaskId.isEmpty()) {
            return Observable.error(new Exception("Task id null or empty"));
        }

        return mTasksRepository.getTask(mTaskId)
                .doOnSubscribe(new Action0() {
                    @Override
                    public void call() {
                        mLoadingSubject.onNext(true);
                    }
                })
                .doOnCompleted(new Action0() {
                    @Override
                    public void call() {
                        mLoadingSubject.onNext(false);
                    }
                });
    }

    /**
     * @return a stream containing the current task id, or error if the task id is invalid.
     */
    @NonNull
    public Observable<String> editTask() {
        if (null == mTaskId || mTaskId.isEmpty()) {
            return Observable.error(new Exception("Task id null or empty"));
        }
        return Observable.just(mTaskId);
    }

    /**
     * Deletes the task from repository. Emits when the task has been deleted. An error is emitted
     * if the task id is invalid.
     *
     * @return a stream notifying about the deletion of the task
     */
    @NonNull
    public Observable<Void> deleteTask() {
        if (null == mTaskId || mTaskId.isEmpty()) {
            return Observable.error(new Exception("Task id null or empty"));
        }
        return Observable.just(mTaskId)
                .map(new Func1<String, Void>() {
                    @Override
                    public Void call(String s) {
                        mTasksRepository.deleteTask(mTaskId);
                        return null;
                    }
                });
    }

    /**
     * Marks a task as completed in the repository. Emits when the task has been marked as
     * completed. An error is emitted if the task id is invalid.
     *
     * @return a stream notifying about the marking of the task as completed
     */
    @NonNull
    public Observable<Void> completeTask() {
        if (null == mTaskId || mTaskId.isEmpty()) {
            return Observable.error(new Exception("Task id null or empty"));
        }
        return Observable.just(mTaskId)
                .map(new Func1<String, Void>() {
                    @Override
                    public Void call(String s) {
                        mTasksRepository.completeTask(mTaskId);
                        return null;
                    }
                });
    }

    /**
     * Marks a task as active in the repository. Emits when the task has been marked as
     * active. An error is emitted if the task id is invalid.
     *
     * @return a stream notifying about the marking of the task as active
     */
    @NonNull
    public Observable<Void> activateTask() {
        if (null == mTaskId || mTaskId.isEmpty()) {
            return Observable.error(new Exception("Task id null or empty"));
        }
        return Observable.just(mTaskId)
                .map(new Func1<String, Void>() {
                    @Override
                    public Void call(String s) {
                        mTasksRepository.activateTask(mTaskId);
                        return null;
                    }
                });
    }
}
