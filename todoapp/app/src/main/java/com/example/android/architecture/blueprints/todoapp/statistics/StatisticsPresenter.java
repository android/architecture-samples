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
import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.tasks.TasksFilterType;
import com.example.android.architecture.blueprints.todoapp.tasks.domain.usecase.GetTasks;

import java.util.List;

/**
 * Listens to user actions from the UI ({@link StatisticsFragment}), retrieves the data and updates
 * the UI as required.
 */
public class StatisticsPresenter implements StatisticsContract.Presenter {

    private final StatisticsContract.View mStatisticsView;
    private final UseCaseHandler mUseCaseHandler;
    private final GetTasks mGetTasks;

    public StatisticsPresenter(
            @NonNull UseCaseHandler useCaseHandler,
            @NonNull StatisticsContract.View statisticsView,
            @NonNull GetTasks getTasks) {
        mUseCaseHandler = checkNotNull(useCaseHandler, "useCaseHandler cannot be null!");
        mStatisticsView = checkNotNull(statisticsView, "StatisticsView cannot be null!");
        mGetTasks = checkNotNull(getTasks,"getTasks cannot be null!");

        mStatisticsView.setPresenter(this);
    }

    @Override
    public void start() {
        loadStatistics();
    }

    private void loadStatistics() {
        mStatisticsView.setProgressIndicator(true);

        mUseCaseHandler.execute(mGetTasks, new GetTasks.RequestValues(false,
                TasksFilterType.ALL_TASKS), new UseCase.UseCaseCallback<GetTasks.ResponseValue>() {
            @Override
            public void onSuccess(GetTasks.ResponseValue response) {
                int activeTasks = 0;
                int completedTasks = 0;

                List<Task> tasks = response.getTasks();

                // We calculate number of active and completed tasks
                for (Task task : tasks) {
                    if (task.isCompleted()) {
                        completedTasks += 1;
                    } else {
                        activeTasks += 1;
                    }
                }
                // The view may not be able to handle UI updates anymore
                if (!mStatisticsView.isActive()) {
                    return;
                }
                mStatisticsView.setProgressIndicator(false);

                mStatisticsView.showStatistics(activeTasks, completedTasks);
            }

            @Override
            public void onError(Error error) {
                // The view may not be able to handle UI updates anymore
                if (!mStatisticsView.isActive()) {
                    return;
                }
                mStatisticsView.showLoadingStatisticsError();
            }
        });
    }
}
