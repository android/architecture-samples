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

package com.example.android.architecture.blueprints.todoapp.tasks.tablet;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;

import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskFragment;

import static com.example.android.architecture.blueprints.todoapp.tasks.tablet.TasksMvpTabletController.ADD_EDIT_DIALOG_TAG;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Handles navigation for tablets: opens the add/edit dialog.
 */
public class TasksTabletNavigator {

    private final FragmentManager mFragmentManager;

    private final TasksMvpTabletController mTasksMvpTabletController;

    TasksTabletNavigator(FragmentManager manager, TasksMvpTabletController controller) {
        mFragmentManager = manager;
        mTasksMvpTabletController = controller;
    }

    /**
     * Shows the edit view for a new task.
     */
    void addNewTask() {
        showAddNewTaskDialogForTablet(null);
    }

    /**
     * Shows the edit view for an existing task.
     * @param taskId The task ID, which cannot be null.
     */
    void editTask(@NonNull String taskId) {
        checkNotNull(taskId);
        showAddNewTaskDialogForTablet(taskId);
    }

    private void showAddNewTaskDialogForTablet(@Nullable String taskId) {
        AddEditTaskFragment addEditTaskFragment
                = mTasksMvpTabletController.createAddEditDialogElements(taskId, true);
        addEditTaskFragment.show(mFragmentManager, ADD_EDIT_DIALOG_TAG);
    }
}
