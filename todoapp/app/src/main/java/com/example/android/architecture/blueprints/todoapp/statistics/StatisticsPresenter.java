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

package com.example.android.architecture.blueprints.todoapp.statistics;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.LoaderProvider;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;
import com.example.android.architecture.blueprints.todoapp.tasks.TaskFilter;
import com.example.android.architecture.blueprints.todoapp.tasks.TasksFilterType;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Listens to user actions from the UI ({@link StatisticsFragment}), retrieves the data and updates
 * the UI as required.
 */
public class StatisticsPresenter implements StatisticsContract.Presenter, TasksRepository.LoadDataCallback, LoaderManager.LoaderCallbacks<Cursor>, TasksDataSource.GetTasksCallback {

    public final static int TASKS_LOADER = 1;

    @NonNull
    private final TasksRepository mTasksRepository;
    @NonNull
    private final LoaderManager mLoaderManager;
    @NonNull
    private final LoaderProvider mLoaderProvider;
    private StatisticsContract.View mStatisticsView;

    public StatisticsPresenter(@NonNull TasksRepository tasksRepository,
                               @NonNull LoaderProvider loaderProvider,
                               @NonNull LoaderManager loaderManager,
                               @NonNull StatisticsContract.View statisticsView) {
        mStatisticsView = checkNotNull(statisticsView, "StatisticsView cannot be null!");
        mLoaderProvider = checkNotNull(loaderProvider, "loaderProvider provider cannot be null");
        mLoaderManager = checkNotNull(loaderManager, "loaderManager provider cannot be null");
        mTasksRepository = checkNotNull(tasksRepository, "tasksInteractor cannot be null!");
        mStatisticsView.setPresenter(this);
    }

    @Override
    public void start() {
        mTasksRepository.getTasks(this);
        mLoaderManager.initLoader(TASKS_LOADER, null, this);
    }

    @Override
    public void onDataLoaded(Cursor data) {
        loadStatistics(data);
    }

    @Override
    public void onDataEmpty() {
        mStatisticsView.showStatistics(0, 0);
    }

    @Override
    public void onTasksLoaded(List<Task> tasks) {

    }

    @Override
    public void onDataNotAvailable() {
        mStatisticsView.setProgressIndicator(false);
        mStatisticsView.showLoadingStatisticsError();
    }

    @Override
    public void onDataReset() {

    }

    private void loadStatistics(Cursor data) {
        mStatisticsView.setProgressIndicator(false);
        int activeTasks = 0;
        int completedTasks = 0;

        List<Task> tasks = convertCursorToTasks(data);

        // Calculate number of active and completed tasks
        for (Task task : tasks) {
            if (task.isCompleted()) {
                completedTasks += 1;
            } else {
                activeTasks += 1;
            }
        }
        mStatisticsView.showStatistics(activeTasks, completedTasks);
    }

    private List<Task> convertCursorToTasks(Cursor data) {
        List<Task> tasks = new ArrayList<>();

        if (data.moveToFirst()) {
            do {
                Task task = Task.from(data);
                tasks.add(task);
            } while (data.moveToNext());
        }

        return tasks;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return mLoaderProvider.createFilteredTasksLoader(TaskFilter.from(TasksFilterType.ALL_TASKS));
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null) {
            if (data.moveToLast()) {
                onDataLoaded(data);
            } else {
                onDataEmpty();
            }
        } else {
            onDataNotAvailable();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
