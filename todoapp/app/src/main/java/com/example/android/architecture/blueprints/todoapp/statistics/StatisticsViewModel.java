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

import com.example.android.architecture.blueprints.todoapp.R;
import com.example.android.architecture.blueprints.todoapp.data.Task;

import java.util.List;

/**
 * Exposes the data to be used in the {@link StatisticsContract.View}.
 * <p>
 * Note that in this case the view model is also the view, not the fragment.
 */
public class StatisticsViewModel implements StatisticsContract.View {

    private int mNumberOfCompletedTasks = 0;

    private int mNumberOfActiveTasks = 0;

    private Context mContext;

    private boolean mIsLoading;

    private boolean mError;

    public StatisticsViewModel(Context context) {
        mContext = context;
        mIsLoading = true;
        mError = false;
    }

    /**
     * Returns a string showing the number of active tasks.
     */
    public String getNumberOfActiveTasks() {
        return mContext.getResources().getString(R.string.statistics_active_tasks) + " " +
                mNumberOfActiveTasks;
    }

    /**
     * Returns a string showing the number of completed tasks.
     */
    public String getNumberOfCompletedTasks() {
        return mContext.getResources().getString(R.string.statistics_completed_tasks) + " " +
                mNumberOfCompletedTasks;
    }

    /**
     * Controls whether the stats are shown or a "No data" message.
     */
    public boolean isEmpty() {
        return mNumberOfActiveTasks + mNumberOfCompletedTasks == 0;
    }

    /**
     * Returns a string with the current status to show to the user.
     */
    public String getStatus() {
        if (mError) {
            return mContext.getResources().getString(R.string.loading_tasks_error);
        }
        if (mIsLoading) {
            return mContext.getResources().getString(R.string.loading);
        }
        return null;
    }

    /**
     * Controls whether the status view is shown in case there's an error or data is still loading.
     */
    public boolean showStatus() {
        return mError || mIsLoading;
    }

    /**
     * Method from {@link StatisticsContract.View} that controls whether a Loading message should be
     * displayed.
     */
    @Override
    public void setProgressIndicator(boolean active) {
        mIsLoading = active;
    }

    /**
     * Method from {@link StatisticsContract.View} called when new data is ready.
     */
    @Override
    public void displayStatistics(List<Task> tasks) {
        mNumberOfCompletedTasks = 0;
        mNumberOfActiveTasks = 0;

        for (Task task : tasks) {
            if (task.isCompleted()) {
                mNumberOfCompletedTasks += 1;
            } else {
                mNumberOfActiveTasks += 1;
            }
        }
        mIsLoading = false;
    }

    /**
     * Method from {@link StatisticsContract.View} called when there was an error loading data.
     */
    @Override
    public void showLoadingStatisticsError() {
        mError = true;
        mIsLoading = false;
    }
}
