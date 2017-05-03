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

package com.example.android.architecture.blueprints.todoapp;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskNavigator;
import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskViewModel;
import com.example.android.architecture.blueprints.todoapp.data.FakeTasksRemoteDataSource;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;
import com.example.android.architecture.blueprints.todoapp.data.source.local.TasksLocalDataSource;
import com.example.android.architecture.blueprints.todoapp.statistics.StatisticsViewModel;
import com.example.android.architecture.blueprints.todoapp.taskdetail.TaskDetailNavigator;
import com.example.android.architecture.blueprints.todoapp.taskdetail.TaskDetailViewModel;
import com.example.android.architecture.blueprints.todoapp.tasks.TasksNavigator;
import com.example.android.architecture.blueprints.todoapp.tasks.TasksViewModel;
import com.example.android.architecture.blueprints.todoapp.util.providers.BaseNavigationProvider;
import com.example.android.architecture.blueprints.todoapp.util.providers.BaseResourceProvider;
import com.example.android.architecture.blueprints.todoapp.util.providers.NavigationProvider;
import com.example.android.architecture.blueprints.todoapp.util.providers.ResourceProvider;
import com.example.android.architecture.blueprints.todoapp.util.schedulers.BaseSchedulerProvider;
import com.example.android.architecture.blueprints.todoapp.util.schedulers.SchedulerProvider;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Enables injection of mock implementations for
 * {@link TasksDataSource} at compile time. This is useful for testing, since it allows us to use
 * a fake instance of the class to isolate the dependencies and run a test hermetically.
 */
public class Injection {

    @NonNull
    public static TasksRepository provideTasksRepository(@NonNull Context context) {
        checkNotNull(context);
        return TasksRepository.getInstance(FakeTasksRemoteDataSource.getInstance(),
                TasksLocalDataSource.getInstance(context, provideSchedulerProvider()),
                provideSchedulerProvider());
    }

    @NonNull
    public static BaseSchedulerProvider provideSchedulerProvider() {
        return SchedulerProvider.getInstance();
    }

    @NonNull
    public static BaseResourceProvider createResourceProvider(@NonNull Context context) {
        return new ResourceProvider(context);
    }

    @NonNull
    public static StatisticsViewModel createStatisticsViewModel(@NonNull Context context) {
        return new StatisticsViewModel(provideTasksRepository(context),
                createResourceProvider(context));
    }

    @NonNull
    public static TaskDetailViewModel createTaskDetailsViewModel(
            @Nullable String taskId,
            @NonNull Activity activity) {
        Context appContext = activity.getApplicationContext();
        BaseNavigationProvider navigationProvider = createNavigationProvider(activity);
        return new TaskDetailViewModel(taskId, provideTasksRepository(appContext),
                createTaskDetailNavigator(navigationProvider));
    }

    @NonNull
    public static TaskDetailNavigator createTaskDetailNavigator(
            @NonNull BaseNavigationProvider navigationProvider) {
        return new TaskDetailNavigator(navigationProvider);
    }

    @NonNull
    public static BaseNavigationProvider createNavigationProvider(@NonNull Activity activity) {
        return new NavigationProvider(activity);
    }

    @NonNull
    public static AddEditTaskViewModel createAddEditTaskViewModel(@Nullable String taskId,
                                                                  @NonNull Activity activity) {
        Context appContext = activity.getApplicationContext();
        BaseNavigationProvider navigationProvider = createNavigationProvider(activity);
        return new AddEditTaskViewModel(taskId, provideTasksRepository(appContext),
                createAddEditTaskNavigator(navigationProvider));
    }

    @NonNull
    public static AddEditTaskNavigator createAddEditTaskNavigator(
            @NonNull BaseNavigationProvider navigationProvider) {
        return new AddEditTaskNavigator(navigationProvider);
    }

    @NonNull
    public static TasksViewModel createTasksViewModel(@NonNull Activity activity) {
        Context appContext = activity.getApplicationContext();
        BaseNavigationProvider navigationProvider = createNavigationProvider(activity);
        return new TasksViewModel(provideTasksRepository(appContext),
                createTasksNavigator(navigationProvider), provideSchedulerProvider());
    }

    @NonNull
    public static TasksNavigator createTasksNavigator(
            @NonNull BaseNavigationProvider navigationProvider) {
        return new TasksNavigator(navigationProvider);
    }
}
