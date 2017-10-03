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

import android.support.annotation.NonNull;

import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskActivity;
import com.example.android.architecture.blueprints.todoapp.taskdetail.TaskDetailActivity;
import com.example.android.architecture.blueprints.todoapp.util.providers.BaseNavigator;

/**
 * Defines the navigation actions that can be called from the task list screen.
 */
public class TasksNavigator {

    @NonNull
    private final BaseNavigator mNavigationProvider;

    public TasksNavigator(@NonNull BaseNavigator mNavigationProvider) {
        this.mNavigationProvider = mNavigationProvider;
    }

    /**
     * Start the activity that allows adding a new task.
     */
    void addNewTask() {
        mNavigationProvider.startActivityForResult(AddEditTaskActivity.class,
                AddEditTaskActivity.REQUEST_ADD_TASK);
    }

    /**
     * Open the details of a task.
     *
     * @param taskId id of the task.
     */
    void openTaskDetails(String taskId) {
        mNavigationProvider.startActivityForResultWithExtra(TaskDetailActivity.class, -1,
                TaskDetailActivity.EXTRA_TASK_ID, taskId);
    }

}
