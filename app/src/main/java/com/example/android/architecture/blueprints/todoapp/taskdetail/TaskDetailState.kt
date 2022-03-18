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
package com.example.android.architecture.blueprints.todoapp.taskdetail

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
 * Create and remember a [TaskDetailState].
 */
@Composable
fun rememberTaskDetailState(
    taskId: String,
    viewModel: TaskDetailViewModel,
    onDeleteTask: () -> Unit,
    scaffoldState: ScaffoldState = rememberScaffoldState(),
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    context: Context = LocalContext.current,
    coroutineScope: CoroutineScope = rememberCoroutineScope()
): TaskDetailState {
    val onDeleteTaskUpdateState by rememberUpdatedState(onDeleteTask)
    // If any of the parameters passed to `remember` change, a new instance of TaskDetailState
    // will be created, and the old one will be destroyed.
    return remember(taskId, viewModel, scaffoldState, lifecycleOwner, context, coroutineScope) {
        TaskDetailState(
            scaffoldState, viewModel, coroutineScope, onDeleteTaskUpdateState, taskId,
            lifecycleOwner, context,
        )
    }
}

/**
 * Responsible for holding state and containing UI-related logic related to [TaskDetailScreen].
 */
@Stable
class TaskDetailState(
    val scaffoldState: ScaffoldState,
    private val viewModel: TaskDetailViewModel,
    private val coroutineScope: CoroutineScope,
    private val onDeleteTask: () -> Unit,
    taskId: String,
    lifecycleOwner: LifecycleOwner,
    context: Context
) {
    private var currentSnackbarJob: Job? = null

    init {
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

    fun onDelete() {
        coroutineScope.launch { viewModel.deleteTask() }
        onDeleteTask()
    }
}
