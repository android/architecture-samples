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
 * Deletes tasks marked as completed.
 */
public class ClearCompleteTasks
        implements UseCase<ClearCompleteTasks.RequestValues, ClearCompleteTasks.ResponseValue> {

    private final TasksRepository mTasksRepository;

    public ClearCompleteTasks(@NonNull TasksRepository tasksRepository) {
        mTasksRepository = checkNotNull(tasksRepository, "tasksRepository cannot be null!");
    }

    @Override
    public void executeUseCase(@NonNull RequestValues requestValues, @NonNull Callback<ResponseValue> callback, @NonNull Subscription subscription) {
        if (subscription.isUnsubscribed()) {
            return;
        }

        callback.onStart();
        mTasksRepository.clearCompletedTasks();

        if (subscription.isUnsubscribed()) {
            return;
        }
        callback.onNext(new ResponseValue());
        callback.onCompleted();
    }

    public static class RequestValues implements UseCase.RequestValues { }

    public static class ResponseValue implements UseCase.ResponseValue { }
}
