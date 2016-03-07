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

import com.example.android.architecture.blueprints.todoapp.data.Task;


/**
 * Listens to user actions from the list item in ({@link TasksFragment}) and redirects them to the
 * Fragment's actions listener.
 */
public class TasksItemActionHandler {

    private TasksContract.Presenter mListener;

    public TasksItemActionHandler(TasksContract.Presenter listener) {
        mListener = listener;
    }

    /**
     * Called by the Data Binding library when the checkbox is toggled.
     */
    public void completeChanged(Task task, boolean isChecked) {
        if (isChecked) {
            mListener.completeTask(task);
        } else {
            mListener.activateTask(task);
        }
    }

    /**
     * Called by the Data Binding library when the row is clicked.
     */
    public void taskClicked(Task task) {
        mListener.openTaskDetails(task);
    }
}
