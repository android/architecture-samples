/*
 * Copyright (C) 2022 The Android Open Source Project
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
package com.example.android.architecture.blueprints.todoapp.addedittask

import android.content.Context
import androidx.compose.material.ScaffoldState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Create and remember a [AddEditTaskState].
 */
@Composable
fun rememberAddEditTaskState(
    taskId: String?,
    viewModel: AddEditTaskViewModel,
    onTaskUpdate: () -> Unit,
    scaffoldState: ScaffoldState = rememberScaffoldState(),
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    context: Context = LocalContext.current,
    coroutineScope: CoroutineScope = rememberCoroutineScope()
): AddEditTaskState {
    val currentOnTaskUpdateState by rememberUpdatedState(onTaskUpdate)
    // If any of the parameters passed to `remember` change, a new instance of AddEditTaskState
    // will be created, and the old one will be destroyed.
    return remember(taskId, viewModel, scaffoldState, lifecycleOwner, context, coroutineScope) {
        AddEditTaskState(
            scaffoldState, viewModel, taskId, currentOnTaskUpdateState,
            lifecycleOwner, context, coroutineScope
        )
    }
}

/**
 * Responsible for holding state and containing UI-related logic related to [AddEditTaskScreen].
 */
@Stable
class AddEditTaskState(
    val scaffoldState: ScaffoldState,
    private val viewModel: AddEditTaskViewModel,
    taskId: String?,
    onTaskUpdate: () -> Unit,
    lifecycleOwner: LifecycleOwner,
    context: Context,
    coroutineScope: CoroutineScope
) {
    private var currentSnackbarJob: Job? = null

    init {
        // Listen for taskUpdated events
        viewModel.taskUpdatedEvent.observe(lifecycleOwner) { taskUpdated ->
            if (taskUpdated) onTaskUpdate()
        }

        // Listen for snackbar messages
        viewModel.snackbarText.observe(lifecycleOwner) { snackbarMessage ->
            if (snackbarMessage != null) {
                currentSnackbarJob?.cancel()
                val snackbarText = context.getString(snackbarMessage)
                currentSnackbarJob = coroutineScope.launch {
                    scaffoldState.snackbarHostState.showSnackbar(snackbarText)
                    viewModel.snackbarMessageShown()
                }
            }
        }

        // Start loading data
        viewModel.start(taskId)
    }

    fun onFabClick() {
        viewModel.saveTask()
    }

    fun onTitleChanged(newTitle: String) {
        viewModel.title.value = newTitle
    }

    fun onDescriptionChanged(newDescription: String) {
        viewModel.description.value = newDescription
    }
}
