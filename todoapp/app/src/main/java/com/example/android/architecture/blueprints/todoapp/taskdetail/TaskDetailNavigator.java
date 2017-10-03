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

package com.example.android.architecture.blueprints.todoapp.taskdetail;

import android.support.annotation.NonNull;

import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskActivity;
import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskFragment;
import com.example.android.architecture.blueprints.todoapp.util.providers.BaseNavigator;

/**
 * Defines the navigation actions that can be called from the Details screen.
 */
public class TaskDetailNavigator {

    @NonNull
    private final BaseNavigator mNavigationProvider;

    public TaskDetailNavigator(@NonNull BaseNavigator navigationProvider) {
        mNavigationProvider = navigationProvider;
    }

    /**
     * Finish the activity when task was deleted.
     */
    void onTaskDeleted() {
        mNavigationProvider.finishActivity();
    }

    /**
     * Open the AddEditTaskActivity to start editing the task.
     *
     * @param taskId the id of the task to be edited.
     */
    void onStartEditTask(String taskId) {
        mNavigationProvider.startActivityForResultWithExtra(AddEditTaskActivity.class,
                TaskDetailActivity.REQUEST_EDIT_TASK, AddEditTaskFragment.ARGUMENT_EDIT_TASK_ID,
                taskId);
    }

    /**
     * Finish the activity when a task was added
     */
    void onTaskEdited() {
        mNavigationProvider.finishActivity();
    }
}
