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

package com.example.android.architecture.blueprints.todoapp.data.source;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.AsyncTaskLoader;

import com.example.android.architecture.blueprints.todoapp.data.Task;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Custom {@link android.content.Loader} for a list of {@link Task}, using the
 * {@link TasksRepository} as its source. This Loader is a {@link AsyncTaskLoader} so it queries
 * the data asynchronously.
 */
public class TasksLoader extends AsyncTaskLoader<List<Task>>
        implements TasksRepository.TasksRepositoryObserver{

    private TasksRepository mRepository;

    public TasksLoader(Context context, @NonNull TasksRepository repository) {
        super(context);
        checkNotNull(repository);
        mRepository = repository;
    }

    @Override
    public List<Task> loadInBackground() {
        return mRepository.getTasks();
    }

    @Override
    public void deliverResult(List<Task> data) {
        if (isReset()) {
            return;
        }

        if (isStarted()) {
            super.deliverResult(data);
        }

    }

    @Override
    protected void onStartLoading() {
        // Deliver any previously loaded data immediately if available.
        if (mRepository.cachedTasksAvailable()) {
            deliverResult(mRepository.getCachedTasks());
        }

        if (takeContentChanged() || !mRepository.cachedTasksAvailable()) {
            // When a change has  been delivered or the repository cache isn't available, we force
            // a load.
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    protected void onReset() {
        onStopLoading();
    }

    @Override
    public void onTasksChanged() {
        if (isStarted()) {
            forceLoad();
        }
    }
}
