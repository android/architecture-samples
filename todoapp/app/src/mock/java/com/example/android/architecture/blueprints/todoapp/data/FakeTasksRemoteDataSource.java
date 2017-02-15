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

package com.example.android.architecture.blueprints.todoapp.data;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import rx.Completable;
import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Implementation of a remote data source with static access to the data for easy testing.
 */
public class FakeTasksRemoteDataSource implements TasksDataSource {

    private static final Map<String, Task> TASKS_SERVICE_DATA = new LinkedHashMap<>();
    private static FakeTasksRemoteDataSource INSTANCE;
    private PublishSubject<Boolean> mRepeatWhen = PublishSubject.create();

    // Prevent direct instantiation.
    private FakeTasksRemoteDataSource() {
    }

    public static FakeTasksRemoteDataSource getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FakeTasksRemoteDataSource();
        }
        return INSTANCE;
    }

    @Override
    public Observable<List<Task>> getTasks() {
        List<Task> values = new ArrayList<>(TASKS_SERVICE_DATA.values());
        return Observable.just(values)
                .repeatWhen(observable -> mRepeatWhen);
    }

    @Override
    public Observable<Task> getTask(@NonNull String taskId) {
        Task task = TASKS_SERVICE_DATA.get(taskId);
        return Observable.just(task)
                .repeatWhen(observable -> mRepeatWhen);
    }

    @Override
    public void saveTask(@NonNull Task task) {
        TASKS_SERVICE_DATA.put(task.getId(), task);
        mRepeatWhen.onNext(true);
    }

    @Override
    public Completable saveTasks(@NonNull List<Task> tasks) {
        return Observable.from(tasks)
                .doOnNext(this::saveTask)
                .toCompletable();
    }

    @Override
    public void completeTask(@NonNull Task task) {
        Task completedTask = new Task(task.getTitle(), task.getDescription(), task.getId(), true);
        TASKS_SERVICE_DATA.put(task.getId(), completedTask);
        mRepeatWhen.onNext(true);
    }

    @Override
    public void completeTask(@NonNull String taskId) {
        Task task = TASKS_SERVICE_DATA.get(taskId);
        Task completedTask = new Task(task.getTitle(), task.getDescription(), task.getId(), true);
        TASKS_SERVICE_DATA.put(taskId, completedTask);
        mRepeatWhen.onNext(true);
    }

    @Override
    public void activateTask(@NonNull Task task) {
        Task activeTask = new Task(task.getTitle(), task.getDescription(), task.getId());
        TASKS_SERVICE_DATA.put(task.getId(), activeTask);
        mRepeatWhen.onNext(true);
    }

    @Override
    public void activateTask(@NonNull String taskId) {
        Task task = TASKS_SERVICE_DATA.get(taskId);
        Task activeTask = new Task(task.getTitle(), task.getDescription(), task.getId());
        TASKS_SERVICE_DATA.put(taskId, activeTask);
        mRepeatWhen.onNext(true);
    }

    @Override
    public void clearCompletedTasks() {
        Iterator<Map.Entry<String, Task>> it = TASKS_SERVICE_DATA.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Task> entry = it.next();
            if (entry.getValue().isCompleted()) {
                it.remove();
            }
        }
        mRepeatWhen.onNext(true);
    }

    public void refreshTasks() {
        mRepeatWhen.onNext(true);
    }

    @Override
    public void deleteTask(@NonNull String taskId) {
        TASKS_SERVICE_DATA.remove(taskId);
        mRepeatWhen.onNext(true);
    }

    @Override
    public void deleteAllTasks() {
        TASKS_SERVICE_DATA.clear();
        mRepeatWhen.onNext(true);
    }

    @VisibleForTesting
    public void addTasks(Task... tasks) {
        for (Task task : tasks) {
            TASKS_SERVICE_DATA.put(task.getId(), task);
        }
        mRepeatWhen.onNext(true);
    }
}
