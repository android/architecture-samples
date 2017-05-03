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

package com.example.android.architecture.blueprints.todoapp.data.source;

import android.support.annotation.NonNull;

import com.example.android.architecture.blueprints.todoapp.data.Task;

import java.util.List;

import rx.Completable;
import rx.Observable;

/**
 * Main entry point for accessing tasks data.
 * <p>
 */
public interface TasksDataSource {

    @NonNull
    Observable<List<Task>> getTasks();

    @NonNull
    Observable<Task> getTask(@NonNull String taskId);

    @NonNull
    Completable saveTask(@NonNull Task task);

    @NonNull
    Completable saveTasks(@NonNull List<Task> tasks);

    @NonNull
    Completable completeTask(@NonNull Task task);

    @NonNull
    Completable completeTask(@NonNull String taskId);

    Completable activateTask(@NonNull Task task);

    Completable activateTask(@NonNull String taskId);

    void clearCompletedTasks();

    @NonNull
    Completable refreshTasks();

    void deleteAllTasks();

    void deleteTask(@NonNull String taskId);
}
