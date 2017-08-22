/*
 * Copyright 2017, The Android Open Source Project
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
package com.example.android.architecture.blueprints.todoapp.tasks

import android.content.Context

import com.example.android.architecture.blueprints.todoapp.TaskViewModel
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository

import java.lang.ref.WeakReference


/**
 * Listens to user actions from the list item in ([TasksFragment]) and redirects them to the
 * Fragment's actions listener.
 */
class TaskItemViewModel(
        context: Context,
        tasksRepository: TasksRepository
) : TaskViewModel(context, tasksRepository) {

    // This navigator is s wrapped in a WeakReference to avoid leaks because it has references to an
    // activity. There's no straightforward way to clear it for each item in a list adapter.
    private var navigator: WeakReference<TaskItemNavigator>? = null

    fun setNavigator(navigator: TaskItemNavigator) {
        this.navigator = WeakReference(navigator)
    }

    /**
     * Called by the Data Binding library when the row is clicked.
     */
    fun taskClicked() {
        val taskId = taskId ?: return // Click happened before task was loaded, no-op.
        navigator?.get()?.openTaskDetails(taskId)
    }
}
