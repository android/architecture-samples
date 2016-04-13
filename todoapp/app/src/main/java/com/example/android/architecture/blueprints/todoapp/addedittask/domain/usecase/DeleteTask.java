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
 * Deletes a {@link Task} from the {@link TasksRepository}.
 */
public class DeleteTask extends UseCase<DeleteTask.RequestValues, DeleteTask.ResponseValue> {

    private final TasksRepository mTasksRepository;

    public DeleteTask(TasksRepository tasksRepository) {
        mTasksRepository = tasksRepository;
    }

    @Override
    protected void executeUseCase(final RequestValues values) {
        mTasksRepository.deleteTask(values.getTaskId());
        getUseCaseCallback().onSuccess(new ResponseValue());
    }

    public static class RequestValues extends UseCase.RequestValues {
        private final String mTaskId;

        public RequestValues(String taskId) {
            mTaskId = taskId;
        }

        public String getTaskId() {
            return mTaskId;
        }
    }

    public class ResponseValue extends UseCase.ResponseValue { }
}
