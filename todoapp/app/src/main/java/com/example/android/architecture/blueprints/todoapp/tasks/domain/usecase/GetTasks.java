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

import com.example.android.architecture.blueprints.todoapp.UseCase;
import com.example.android.architecture.blueprints.todoapp.base.domain.error.DataNotAvailableError;
import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;
import com.example.android.architecture.blueprints.todoapp.tasks.TasksFilterType;
import com.example.android.architecture.blueprints.todoapp.tasks.domain.filter.FilterFactory;
import com.example.android.architecture.blueprints.todoapp.tasks.domain.filter.TaskFilter;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Fetches the list of tasks.
 */
public class GetTasks extends UseCase<GetTasks.RequestValues, GetTasks.ResponseValue> {

    private final TasksRepository mTasksRepository;

    private final FilterFactory mFilterFactory;

    public GetTasks(@NonNull TasksRepository tasksRepository, @NonNull FilterFactory filterFactory) {
        mTasksRepository = checkNotNull(tasksRepository, "tasksRepository cannot be null!");
        mFilterFactory = checkNotNull(filterFactory, "filterFactory cannot be null!");
    }

    @Override
    protected void executeUseCase(final RequestValues values) {
        if (values.isForceUpdate()) {
            mTasksRepository.refreshTasks();
        }

        mTasksRepository.getTasks(new TasksDataSource.LoadTasksCallback() {
            @Override
            public void onTasksLoaded(List<Task> tasks) {
                TasksFilterType currentFiltering = values.getCurrentFiltering();
                TaskFilter taskFilter = mFilterFactory.create(currentFiltering);

                List<Task> tasksFiltered = taskFilter.filter(tasks);
                ResponseValue responseValue = new ResponseValue(tasksFiltered);
                getUseCaseCallback().onSuccess(responseValue);
            }

            @Override
            public void onDataNotAvailable() {
                getUseCaseCallback().onError(new DataNotAvailableError());
            }
        });

    }

    public static final class RequestValues implements UseCase.RequestValues {

        private final TasksFilterType mCurrentFiltering;
        private final boolean mForceUpdate;

        public RequestValues(boolean forceUpdate, @NonNull TasksFilterType currentFiltering) {
            mForceUpdate = forceUpdate;
            mCurrentFiltering = checkNotNull(currentFiltering, "currentFiltering cannot be null!");
        }

        public boolean isForceUpdate() {
            return mForceUpdate;
        }

        public TasksFilterType getCurrentFiltering() {
            return mCurrentFiltering;
        }
    }

    public static final class ResponseValue implements UseCase.ResponseValue {

        private final List<Task> mTasks;

        public ResponseValue(@NonNull List<Task> tasks) {
            mTasks = checkNotNull(tasks, "tasks cannot be null!");
        }

        public List<Task> getTasks() {
            return mTasks;
        }
    }
}
