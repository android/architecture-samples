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

import android.support.annotation.NonNull;

import com.example.android.architecture.blueprints.todoapp.R;
import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;
import com.example.android.architecture.blueprints.todoapp.util.EspressoIdlingResource;
import com.example.android.architecture.blueprints.todoapp.util.providers.BaseResourceProvider;

import java.util.List;

import rx.Observable;
import rx.functions.Func1;
import rx.subjects.BehaviorSubject;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Listens to user actions from the UI ({@link StatisticsFragment}), retrieves the data and exposes
 * updates for the progress of retrieveing the statistics, and the statistics.
 */
public class StatisticsViewModel {

    @NonNull
    private final TasksRepository mTasksRepository;

    @NonNull
    private final BaseResourceProvider mResourceProvider;

    @NonNull
    private final BehaviorSubject<Boolean> mProgressIndicatorSubject;

    public StatisticsViewModel(@NonNull TasksRepository tasksRepository,
                               @NonNull BaseResourceProvider resourceProvider) {
        mTasksRepository = checkNotNull(tasksRepository, "tasksRepository cannot be null");
        mResourceProvider = checkNotNull(resourceProvider, "resourceProvider cannot be null");

        mProgressIndicatorSubject = BehaviorSubject.create(false);
    }

    /**
     * @return A stream of statistics to be displayed.
     */
    @NonNull
    public Observable<String> getStatistics() {
        // The network request might be handled in a different thread so make sure Espresso knows
        // that the app is busy until the response is handled.
        EspressoIdlingResource.increment(); // App is busy until further notice

        Observable<Task> tasks = mTasksRepository
                .getTasks()
                .flatMap(new Func1<List<Task>, Observable<Task>>() {
                    @Override
                    public Observable<Task> call(List<Task> tasks) {
                        return Observable.from(tasks);
                    }
                });
        Observable<Integer> completedTasks = tasks.filter(Task::isCompleted).count();
        Observable<Integer> activeTasks = tasks.filter(Task::isActive).count();

        return Observable
                .zip(completedTasks,
                        activeTasks,
                        (completed, active) -> getStatisticsString(active, completed))
                .doOnSubscribe(() -> {
                    // the progress indicator should be visible
                    mProgressIndicatorSubject.onNext(true);
                })
                .doOnTerminate(() -> mProgressIndicatorSubject.onNext(false));
    }

    @NonNull
    private String getStatisticsString(int numberOfActiveTasks, int numberOfCompletedTasks) {
        if (numberOfCompletedTasks == 0 && numberOfActiveTasks == 0) {
            return mResourceProvider.getString(R.string.statistics_no_tasks);
        } else {
            return mResourceProvider.getString(R.string.statistics_active_completed_tasks,
                    numberOfActiveTasks, numberOfCompletedTasks);
        }
    }

    /**
     * @return a stream of data, indicating whether the retrieval of the statistics is in progress or not
     */
    @NonNull
    public Observable<Boolean> getProgressIndicator() {
        return mProgressIndicatorSubject.asObservable();
    }
}
