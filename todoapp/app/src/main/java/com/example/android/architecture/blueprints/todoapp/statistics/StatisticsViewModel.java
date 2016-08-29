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
import android.support.v4.util.Pair;

import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;
import com.example.android.architecture.blueprints.todoapp.util.EspressoIdlingResource;

import java.util.List;

import rx.Observable;
import rx.functions.Action0;
import rx.functions.Func1;
import rx.functions.Func2;
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
    private final BehaviorSubject<Boolean> mProgressIndicatorSubject;

    public StatisticsViewModel(@NonNull TasksRepository tasksRepository) {
        mTasksRepository = checkNotNull(tasksRepository, "tasksRepository cannot be null");

        mProgressIndicatorSubject = BehaviorSubject.create(false);
    }

    /**
     * @return A stream of statistics data: <active tasks, completed tasks> pair.
     */
    @NonNull
    public Observable<Pair<Integer, Integer>> getStatistics() {
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
        Observable<Integer> completedTasks = tasks.filter(new Func1<Task, Boolean>() {
            @Override
            public Boolean call(Task task) {
                return task.isCompleted();
            }
        }).count();
        Observable<Integer> activeTasks = tasks.filter(new Func1<Task, Boolean>() {
            @Override
            public Boolean call(Task task) {
                return task.isActive();
            }
        }).count();

        return Observable
                .zip(completedTasks, activeTasks, new Func2<Integer, Integer, Pair<Integer, Integer>>() {
                    @Override
                    public Pair<Integer, Integer> call(Integer completed, Integer active) {
                        return Pair.create(active, completed);
                    }
                })
                .doOnSubscribe(new Action0() {
                    @Override
                    public void call() {
                        // the progress indicator should be visible
                        mProgressIndicatorSubject.onNext(true);
                    }
                })
                .doOnTerminate(new Action0() {
                    @Override
                    public void call() {
                        mProgressIndicatorSubject.onNext(false);
                    }
                });
    }

    /**
     * @return a stream of data, indicating whether the retrieval of the statistics is in progress or not
     */
    @NonNull
    public Observable<Boolean> getProgressIndicator() {
        return mProgressIndicatorSubject.asObservable();
    }
}
