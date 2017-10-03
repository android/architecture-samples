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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import rx.Completable;
import rx.Observable;

/**
 * Implementation of a remote data source with static access to the data for easy testing.
 */
public class FakeTasksRemoteDataSource implements TasksDataSource {

    private static final Map<String, Task> TASKS_SERVICE_DATA = new LinkedHashMap<>();
    private static FakeTasksRemoteDataSource INSTANCE;

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
        return Observable.just(values);
    }

    @Override
    public Observable<Task> getTask(@NonNull String taskId) {
        Task task = TASKS_SERVICE_DATA.get(taskId);
        return Observable.just(task);
    }

    @Override
    public Completable saveTask(@NonNull Task task) {
        return Completable.fromAction(() -> TASKS_SERVICE_DATA.put(task.getId(), task));
    }


    @Override
    public Completable saveTasks(@NonNull List<Task> tasks) {
        return Observable.from(tasks)
                .doOnNext(this::saveTask)
                .toCompletable();
    }

    @Override
    public Completable completeTask(@NonNull Task task) {
        return Completable.fromAction(() -> {
            Task completedTask = new Task(task.getTitle(), task.getDescription(), task.getId(), true);
            TASKS_SERVICE_DATA.put(task.getId(), completedTask);
        });
    }

    @Override
    public Completable completeTask(@NonNull String taskId) {
        return Completable.fromAction(() -> {
            Task task = TASKS_SERVICE_DATA.get(taskId);
            Task completedTask = new Task(task.getTitle(), task.getDescription(), task.getId(), true);
            TASKS_SERVICE_DATA.put(taskId, completedTask);
        });
    }

    @Override
    public Completable activateTask(@NonNull Task task) {
        return Completable.fromAction(() -> {
            Task activeTask = new Task(task.getTitle(), task.getDescription(), task.getId());
            TASKS_SERVICE_DATA.put(task.getId(), activeTask);
        });
    }

    @Override
    public Completable activateTask(@NonNull String taskId) {
        return Completable.fromAction(() -> {
            Task task = TASKS_SERVICE_DATA.get(taskId);
            Task activeTask = new Task(task.getTitle(), task.getDescription(), task.getId());
            TASKS_SERVICE_DATA.put(taskId, activeTask);
        });
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
    }

    public Completable refreshTasks() {
        return Completable.complete();
    }

    @Override
    public void deleteTask(@NonNull String taskId) {
        TASKS_SERVICE_DATA.remove(taskId);
    }

    @Override
    public void deleteAllTasks() {
        TASKS_SERVICE_DATA.clear();
    }

    @VisibleForTesting
    public void addTasks(Task... tasks) {
        for (Task task : tasks) {
            TASKS_SERVICE_DATA.put(task.getId(), task);
        }
    }
}
