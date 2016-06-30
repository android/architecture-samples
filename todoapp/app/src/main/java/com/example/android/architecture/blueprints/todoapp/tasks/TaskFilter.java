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

package com.example.android.architecture.blueprints.todoapp.tasks;

import android.os.Bundle;

import com.example.android.architecture.blueprints.todoapp.BuildConfig;

public class TaskFilter {

    public final static String KEY_TASK_FILTER = BuildConfig.APPLICATION_ID + "TASK_FILTER";
    private TasksFilterType tasksFilterType = TasksFilterType.ALL_TASKS;
    private Bundle filterExtras;

    protected TaskFilter(Bundle extras) {
        this.filterExtras = extras;
        this.tasksFilterType = (TasksFilterType) extras.getSerializable(KEY_TASK_FILTER);
    }

    public static TaskFilter from(TasksFilterType tasksFilterType){
        Bundle bundle = new Bundle();
        bundle.putSerializable(KEY_TASK_FILTER, tasksFilterType);
        return new TaskFilter(bundle);
    }

    public TasksFilterType getTasksFilterType() {
        return tasksFilterType;
    }

    public Bundle getFilterExtras() {
        return filterExtras;
    }
}
