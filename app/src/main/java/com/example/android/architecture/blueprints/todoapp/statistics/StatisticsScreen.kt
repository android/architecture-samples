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
package com.example.android.architecture.blueprints.todoapp.statistics

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.util.LoadingContent
import com.example.android.architecture.blueprints.todoapp.util.getViewModelFactory
import com.google.accompanist.appcompattheme.AppCompatTheme

@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel = viewModel(factory = getViewModelFactory())
) {
    val loading by viewModel.dataLoading.observeAsState(initial = false)
    val empty by viewModel.empty.observeAsState(initial = true)
    val activeTasksPercent by viewModel.activeTasksPercent.observeAsState(initial = 0f)
    val completedTasksPercent by viewModel.completedTasksPercent.observeAsState(initial = 0f)

    StatisticsContent(
        loading = loading,
        empty = empty,
        activeTasksPercent = activeTasksPercent,
        completedTasksPercent = completedTasksPercent,
        onRefresh = { viewModel.refresh() }
    )
}

@Composable
private fun StatisticsContent(
    loading: Boolean,
    empty: Boolean,
    activeTasksPercent: Float,
    completedTasksPercent: Float,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val commonModifier = modifier
        .fillMaxWidth()
        .padding(all = dimensionResource(id = R.dimen.activity_horizontal_margin))

    LoadingContent(
        loading = loading,
        empty = empty,
        onRefresh = onRefresh,
        modifier = modifier,
        emptyContent = {
            Text(
                text = stringResource(id = R.string.statistics_no_tasks),
                modifier = commonModifier
            )
        }
    ) {
        Column(
            commonModifier.verticalScroll(rememberScrollState())
        ) {
            Text(stringResource(id = R.string.statistics_active_tasks, activeTasksPercent))
            Text(stringResource(id = R.string.statistics_completed_tasks, completedTasksPercent))
        }
    }
}

@Preview
@Composable
fun StatisticsContentPreview() {
    AppCompatTheme {
        Surface {
            StatisticsContent(
                loading = false,
                empty = false,
                activeTasksPercent = 80f,
                completedTasksPercent = 20f,
                onRefresh = { }
            )
        }
    }
}

@Preview
@Composable
fun StatisticsContentEmptyPreview() {
    AppCompatTheme {
        Surface {
            StatisticsContent(
                loading = false,
                empty = true,
                activeTasksPercent = 0f,
                completedTasksPercent = 0f,
                onRefresh = { }
            )
        }
    }
}
