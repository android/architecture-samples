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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Checkbox
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.util.LoadingContent
import com.example.android.architecture.blueprints.todoapp.util.getViewModelFactory
import com.google.accompanist.appcompattheme.AppCompatTheme

@Composable
fun TaskDetailScreen(
    taskId: String?,
    onEditTask: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TaskDetailViewModel = viewModel(factory = getViewModelFactory()),
) {
    val scaffoldState = rememberScaffoldState()
    Scaffold(
        scaffoldState = scaffoldState,
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(onClick = onEditTask) {
                Icon(Icons.Filled.Edit, stringResource(id = R.string.edit_task))
            }
        }
    ) { paddingValues ->
        val loading by viewModel.dataLoading.observeAsState(initial = false)
        val dataAvailable by viewModel.isDataAvailable.observeAsState(initial = true)
        val completed by viewModel.completed.observeAsState(initial = false)
        val task by viewModel.task.observeAsState()

        EditTaskContent(
            loading = loading,
            empty = !dataAvailable,
            task = task,
            taskCompleted = completed,
            onRefresh = viewModel::refresh,
            onTaskCheck = viewModel::setCompleted,
            modifier = Modifier.padding(paddingValues)
        )

        // Note that the code below could be simplified with a state holder
        // similar to AddEditTaskState

        // Start loading data
        LaunchedEffect(Unit) {
            viewModel.start(taskId)
        }

        // Process snackbar events
        val snackbarEvent by viewModel.snackbarText.observeAsState()
        snackbarEvent?.getContentIfNotHandled()?.let { messageId ->
            val message = stringResource(id = messageId)
            LaunchedEffect(message) {
                scaffoldState.snackbarHostState.showSnackbar(message)
            }
        }
    }
}

@Composable
private fun EditTaskContent(
    loading: Boolean,
    empty: Boolean,
    task: Task?,
    taskCompleted: Boolean,
    onTaskCheck: (Boolean) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val screenPadding = Modifier.padding(
        horizontal = dimensionResource(id = R.dimen.activity_horizontal_margin),
        vertical = dimensionResource(id = R.dimen.activity_vertical_margin),
    )
    val commonModifier = modifier
        .fillMaxWidth()
        .then(screenPadding)

    LoadingContent(
        loading = loading,
        empty = empty,
        emptyContent = {
            Text(
                text = stringResource(id = R.string.no_data),
                modifier = commonModifier
            )
        },
        onRefresh = onRefresh
    ) {
        Column(commonModifier.verticalScroll(rememberScrollState())) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .then(screenPadding),

                ) {
                Checkbox(taskCompleted, onTaskCheck)
                Column {
                    Text(text = task?.title ?: "", style = MaterialTheme.typography.h6)
                    Text(text = task?.description ?: "", style = MaterialTheme.typography.body1)
                }
            }
        }
    }
}

@Preview
@Composable
private fun EditTaskContentPreview() {
    AppCompatTheme {
        Surface {
            EditTaskContent(
                loading = false,
                empty = false,
                Task("Title", "Description"),
                taskCompleted = false,
                onTaskCheck = { },
                onRefresh = { }
            )
        }
    }
}

@Preview
@Composable
private fun EditTaskContentTaskCompletedPreview() {
    AppCompatTheme {
        Surface {
            EditTaskContent(
                loading = false,
                empty = false,
                Task("Title", "Description"),
                taskCompleted = true,
                onTaskCheck = { },
                onRefresh = { }
            )
        }
    }
}

@Preview
@Composable
private fun EditTaskContentEmptyPreview() {
    AppCompatTheme {
        Surface {
            EditTaskContent(
                loading = false,
                empty = true,
                Task("Title", "Description"),
                taskCompleted = false,
                onTaskCheck = { },
                onRefresh = { }
            )
        }
    }
}
