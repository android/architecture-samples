/*
 * Copyright (C) 2015 The Android Open Source Project
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

import com.example.android.architecture.blueprints.todoapp.BR;
import com.example.android.architecture.blueprints.todoapp.R;


/**
 * Created by Wang, Sheng-Yuan on 2016/10/12.
 *
 * Exposes the data to be used in the {@link StatisticsContract.View}.
 * Note that in this case the view model is also the view, not the fragment.
 */
public class StatisticsViewModel extends BaseObservable implements StatisticsContract.View {

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
    @Bindable
    public String getNumberOfActiveTasks() {
        return mContext.getResources().getString(R.string.statistics_active_tasks)
                + " " + mNumberOfActiveTasks;
    }

    /**
     * Returns a string showing the number of completed tasks.
     */
    @Bindable
    public String getNumberOfCompletedTasks() {
        return mContext.getResources().getString(R.string.statistics_completed_tasks)
                + " " + mNumberOfCompletedTasks;
    }

    /**
     * Controls whether the stats are shown or a "No data" message.
     */
    @Bindable
    public boolean isEmpty() {
        return mNumberOfActiveTasks + mNumberOfCompletedTasks == 0;
    }

    /**
     * Returns a string with the current status to show to the user.
     */
    @Bindable
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
     * Controls whether the status view is shown in case there's an error or data
     * is still loading.
     */
    @Bindable
    public boolean getShowStatus() {
        return mError || mIsLoading;
    }

    /**
     * Method from {@link StatisticsContract.View} that controls whether a Loading
     * message should be displayed.
     */
    public void setProgressIndicator(boolean active) {
        mIsLoading = active;
        notifyPropertyChanged(BR.status);
        notifyPropertyChanged(BR.numberOfActiveTasks);
        notifyPropertyChanged(BR.numberOfCompletedTasks);
        notifyPropertyChanged(BR.showStatus);
        notifyPropertyChanged(BR.empty);
    }


    /**
     * Method from {@link StatisticsContract.View} called when new data is ready.
     */
    public void showStatistics(int numberOfIncompleteTasks, int numberOfCompletedTasks) {
        mNumberOfCompletedTasks += numberOfCompletedTasks;
        mNumberOfActiveTasks += numberOfIncompleteTasks;
        // mIsLoading = false;
    }

    /**
     * Method from {@link StatisticsContract.View} called when there was an error
     * loading data.
     */
    @Override
    public void showLoadingStatisticsError() {
        mError = true;
        mIsLoading = false;
    }
}
