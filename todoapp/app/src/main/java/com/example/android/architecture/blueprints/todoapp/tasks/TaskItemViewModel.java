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

import android.content.Context;

import com.example.android.architecture.blueprints.todoapp.TaskViewModel;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;

import java.lang.ref.WeakReference;


/**
 * Listens to user actions from the list item in ({@link TasksFragment}) and redirects them to the
 * Fragment's actions listener.
 */
public class TaskItemViewModel extends TaskViewModel {

    // Navigator has references to Activity so it must be wrapped to prevent leaks.
    private final WeakReference<TaskItemNavigator> mTaskItemNavigator;

    public TaskItemViewModel(Context context, TasksRepository tasksRepository,
                             TaskItemNavigator itemNavigator) {
        super(context, tasksRepository);
        mTaskItemNavigator = new WeakReference<>(itemNavigator);
    }

    /**
     * Called by the Data Binding library when the row is clicked.
     */
    public void taskClicked() {
        String taskId = getTaskId();
        if (taskId == null) {
            // Click happened before task was loaded, no-op.
            return;
        }
        if (mTaskItemNavigator.get() != null) {
            mTaskItemNavigator.get().openTaskDetails(taskId);
        }
    }
}