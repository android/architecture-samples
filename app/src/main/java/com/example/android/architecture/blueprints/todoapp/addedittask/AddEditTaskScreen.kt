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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ContentAlpha
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.util.getViewModelFactory
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@Composable
fun AddEditTaskScreen(
    taskId: String?,
    onTaskUpdate: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AddEditTaskViewModel = viewModel(factory = getViewModelFactory())
) {
    val state = rememberAddEditTaskState(taskId, viewModel, onTaskUpdate)
    Scaffold(
        modifier = modifier.fillMaxSize(),
        scaffoldState = state.scaffoldState,
        floatingActionButton = {
            FloatingActionButton(onClick = state::onFabClick) {
                Icon(Icons.Filled.Done, stringResource(id = R.string.cd_save_task))
            }
        }
    ) { paddingValues ->
        val loading by viewModel.dataLoading.observeAsState(initial = false)
        val title by viewModel.title.observeAsState(initial = "")
        val description by viewModel.description.observeAsState(initial = "")

        AddEditTaskContent(
            loading = loading,
            title = title,
            description = description,
            onTitleChanged = state::onTitleChanged,
            onDescriptionChanged = state::onDescriptionChanged,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@Composable
private fun AddEditTaskContent(
    loading: Boolean,
    title: String,
    description: String,
    onTitleChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (loading) {
        SwipeRefresh(
            // Show the loading spinner—`loading` is `true` in this code path
            state = rememberSwipeRefreshState(true),
            onRefresh = { /* DO NOTHING */ },
            content = { },
        )
    } else {
        Column(
            modifier
                .fillMaxWidth()
                .padding(all = dimensionResource(id = R.dimen.activity_horizontal_margin))
                .verticalScroll(rememberScrollState())
        ) {
            val textFieldColors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                cursorColor = MaterialTheme.colors.secondary.copy(alpha = ContentAlpha.high)
            )
            OutlinedTextField(
                value = title,
                modifier = Modifier.fillMaxWidth(),
                onValueChange = onTitleChanged,
                placeholder = {
                    Text(
                        text = stringResource(id = R.string.title_hint),
                        style = MaterialTheme.typography.h6
                    )
                },
                textStyle = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold),
                maxLines = 1,
                colors = textFieldColors
            )
            OutlinedTextField(
                value = description,
                onValueChange = onDescriptionChanged,
                placeholder = { Text(stringResource(id = R.string.description_hint)) },
                modifier = Modifier
                    .height(350.dp)
                    .fillMaxWidth(),
                colors = textFieldColors
            )
        }
    }
}
