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

import static com.google.common.base.Preconditions.checkNotNull;

import android.support.annotation.NonNull;

import com.example.android.architecture.blueprints.todoapp.UseCase;
import com.example.android.architecture.blueprints.todoapp.UseCaseHandler;
import com.example.android.architecture.blueprints.todoapp.statistics.domain.usecase.GetStatistics;
import com.example.android.architecture.blueprints.todoapp.statistics.domain.usecase.Statistics;

/**
 * Listens to user actions from the UI ({@link StatisticsFragment}), retrieves the data and updates
 * the UI as required.
 */
public class StatisticsPresenter implements StatisticsContract.Presenter {

    private final StatisticsContract.View mStatisticsView;
    private final UseCaseHandler mUseCaseHandler;
    private final GetStatistics mGetStatistics;

    public StatisticsPresenter(
            @NonNull UseCaseHandler useCaseHandler,
            @NonNull StatisticsContract.View statisticsView,
            @NonNull GetStatistics getStatistics) {
        mUseCaseHandler = checkNotNull(useCaseHandler, "useCaseHandler cannot be null!");
        mStatisticsView = checkNotNull(statisticsView, "StatisticsView cannot be null!");
        mGetStatistics = checkNotNull(getStatistics,"getStatistics cannot be null!");

        mStatisticsView.setPresenter(this);
    }

    @Override
    public void start() {
        loadStatistics();
    }

    private void loadStatistics() {
        mStatisticsView.setProgressIndicator(true);

        mUseCaseHandler.execute(mGetStatistics, new GetStatistics.RequestValues(),
                new UseCase.UseCaseCallback<GetStatistics.ResponseValue>() {
            @Override
            public void onSuccess(GetStatistics.ResponseValue response) {
                Statistics statistics = response.getStatistics();
                // The view may not be able to handle UI updates anymore
                if (!mStatisticsView.isActive()) {
                    return;
                }
                mStatisticsView.setProgressIndicator(false);

                mStatisticsView.showStatistics(statistics.getActiveTasks(), statistics.getCompletedTasks());
            }

            @Override
            public void onError() {
                // The view may not be able to handle UI updates anymore
                if (!mStatisticsView.isActive()) {
                    return;
                }
                mStatisticsView.showLoadingStatisticsError();
            }
        });
    }
}
