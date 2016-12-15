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

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.ObservableBoolean;
import android.support.annotation.VisibleForTesting;

import com.example.android.architecture.blueprints.todoapp.BR;
import com.example.android.architecture.blueprints.todoapp.R;
import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;
import com.example.android.architecture.blueprints.todoapp.util.EspressoIdlingResource;

import java.util.List;

/**
 * Exposes the data to be used in the {@link StatisticsFragment}.
 * <p>
 * Note that in this case the view model is also the view, not the fragment.
 */
public class StatisticsViewModel extends BaseObservable {

    public final ObservableBoolean dataLoading = new ObservableBoolean(false);

    public final ObservableBoolean error = new ObservableBoolean(false);

    @VisibleForTesting
    public int mNumberOfActiveTasks = 0;

    @VisibleForTesting
    protected int mNumberOfCompletedTasks = 0;

    private Context mContext;

    private final TasksRepository mTasksRepository;


    public StatisticsViewModel(Context context, TasksRepository tasksRepository) {
        mContext = context;
        mTasksRepository = tasksRepository;
    }

    public void start() {
        loadStatistics();
    }

    public void loadStatistics() {
        dataLoading.set(true);

        // The network request might be handled in a different thread so make sure Espresso knows
        // that the app is busy until the response is handled.
        EspressoIdlingResource.increment(); // App is busy until further notice

        mTasksRepository.getTasks(new TasksDataSource.LoadTasksCallback() {
            @Override
            public void onTasksLoaded(List<Task> tasks) {

                // This callback may be called twice, once for the cache and once for loading
                // the data from the server API, so we check before decrementing, otherwise
                // it throws "Counter has been corrupted!" exception.
                if (!EspressoIdlingResource.getIdlingResource().isIdleNow()) {
                    EspressoIdlingResource.decrement(); // Set app as idle.
                }
                dataLoading.set(false);
                error.set(false);

                computeStats(tasks);
            }

            @Override
            public void onDataNotAvailable() {
                error.set(true);
            }
        });
    }
    /**
     * Returns a string showing the number of active tasks.
     */
    @Bindable
    public String getNumberOfActiveTasks() {
        return mContext.getResources().getString(R.string.statistics_active_tasks) + " " +
                mNumberOfActiveTasks;
    }

    /**
     * Returns a string showing the number of completed tasks.
     */
    @Bindable
    public String getNumberOfCompletedTasks() {
        return mContext.getResources().getString(R.string.statistics_completed_tasks) + " " +
                mNumberOfActiveTasks;
    }

    /**
     * Controls whether the stats are shown or a "No data" message.
     */
    @Bindable
    public boolean isEmpty() {
        return mNumberOfActiveTasks + mNumberOfCompletedTasks == 0;
    }

    /**
     * Called when new data is ready.
     */
    private void computeStats(List<Task> tasks) {
        int completed = 0;
        int active = 0;

        for (Task task : tasks) {
            if (task.isCompleted()) {
                completed += 1;
            } else {
                active += 1;
            }
        }
        mNumberOfActiveTasks = active;
        notifyPropertyChanged(BR.numberOfCompletedTasks);
        mNumberOfCompletedTasks = completed;
        notifyPropertyChanged(BR.numberOfActiveTasks);
    }
}
