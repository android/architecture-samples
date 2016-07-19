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

import com.example.android.architecture.blueprints.todoapp.UseCase;
import com.example.android.architecture.blueprints.todoapp.UseCaseHandler;
import com.example.android.architecture.blueprints.todoapp.statistics.domain.model.Statistics;
import com.example.android.architecture.blueprints.todoapp.statistics.domain.usecase.GetStatistics;

import javax.inject.Inject;

/**
 * Listens to user actions from the UI ({@link StatisticsFragment}), retrieves the data and updates
 * the UI as required.
 * <p />
 * By marking the constructor with {@code @Inject}, Dagger injects the dependencies required to
 * create an instance of the StatisticsPresenter (if it fails, it emits a compiler error). It uses
 * {@link StatisticsPresenterModule} to do so.
 * <p />
 * Dagger generated code doesn't require public access to the constructor or class, and
 * therefore, to ensure the developer doesn't instantiate the class manually and bypasses Dagger,
 * it's good practice minimise the visibility of the class/constructor as much as possible.
 **/
final class StatisticsPresenter implements StatisticsContract.Presenter {
    private final StatisticsContract.View mStatisticsView;
    private final UseCaseHandler mUseCaseHandler;
    private final GetStatistics mGetStatistics;

    /**
     * Dagger strictly enforces that arguments not marked with {@code @Nullable} are not injected
     * with {@code @Nullable} values.
     */
    @Inject
    StatisticsPresenter(UseCaseHandler useCaseHandler, StatisticsContract.View statisticsView,
                        GetStatistics getStatistics) {
        mStatisticsView = statisticsView;
        mUseCaseHandler = useCaseHandler;
        mGetStatistics = getStatistics;
    }

    /**
     * Method injection is used here to safely reference {@code this} after the object is created.
     * For more information, see Java Concurrency in Practice.
     */
    @Inject
    void setupListeners() {
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
