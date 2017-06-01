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

package com.example.android.architecture.blueprints.todoapp.tasks.domain.usecase;

import android.support.annotation.NonNull;

import com.example.android.architecture.blueprints.todoapp.Subscription;
import com.example.android.architecture.blueprints.todoapp.UseCase;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Marks a task as completed.
 */
public class CompleteTask implements UseCase<CompleteTask.RequestValues, CompleteTask.ResponseValue> {

    private final TasksRepository mTasksRepository;

    public CompleteTask(@NonNull TasksRepository tasksRepository) {
        mTasksRepository = checkNotNull(tasksRepository, "tasksRepository cannot be null!");
    }

    @Override
    public void executeUseCase(@NonNull RequestValues requestValues, @NonNull Callback<ResponseValue> callback, @NonNull Subscription subscription) {
        if (subscription.isUnsubscribed()) {
            return;
        }

        String completedTask = requestValues.getCompletedTask();
        mTasksRepository.completeTask(completedTask);

        if (subscription.isUnsubscribed()) {
            return;
        }
        callback.onNext(new ResponseValue());
        callback.onCompleted();
    }

    public static final class RequestValues implements UseCase.RequestValues {

        private final String mCompletedTask;

        public RequestValues(@NonNull String completedTask) {
            mCompletedTask = checkNotNull(completedTask, "completedTask cannot be null!");
        }

        public String getCompletedTask() {
            return mCompletedTask;
        }
    }

    public static final class ResponseValue implements UseCase.ResponseValue {
    }
}
