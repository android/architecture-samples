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

package com.example.android.architecture.blueprints.todoapp.addedittask.domain.usecase;

import com.example.android.architecture.blueprints.todoapp.UseCase;
import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;

/**
 * Updates or creates a new {@link Task} in the {@link TasksRepository}.
 */
public class SaveTask extends UseCase<SaveTask.RequestValues, SaveTask.ResponseValue> {

    private final TasksRepository mTasksRepository;

    public SaveTask(TasksRepository tasksRepository) {
        mTasksRepository = tasksRepository;
    }

    @Override
    protected void executeUseCase(final RequestValues values) {
        Task task = values.getTask();
        mTasksRepository.saveTask(task);

        getUseCaseCallback().onSuccess(new ResponseValue(task));
    }

    public static class RequestValues extends UseCase.RequestValues {
        private final Task mTask;

        public RequestValues(Task task) {
            mTask = task;
        }

        public Task getTask() {
            return mTask;
        }
    }

    public class ResponseValue extends UseCase.ResponseValue {
        private Task mTask;

        public ResponseValue(Task task) {
            mTask = task;
        }

        public Task getTask() {
            return mTask;
        }
    }
}
