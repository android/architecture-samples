/*
 * Copyright (C) 2023 The Android Open Source Project
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

package com.google.samples.modularization.feature.wear.home.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.items
import com.google.samples.modularization.ui.Loading
import com.google.samples.modularization.core.data.MyModel

@Composable
fun HomeRoute(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    HomeScreen(state, modifier)
}

@Composable
internal fun HomeScreen(
    state: HomeUiState,
    modifier: Modifier = Modifier
) {
    when(state) {
        HomeUiState.Loading -> Loading(modifier)
        is HomeUiState.Success -> Content(state.data, modifier)
    }
}

@Composable
internal fun Content(
    items: List<MyModel>,
    modifier: Modifier = Modifier
) {
    ScalingLazyColumn(modifier = modifier.fillMaxSize()) {
        items(items = items) { item ->
            Chip(
                label = { Text(text = item.title) },
                onClick = { /* no-op */ },
                modifier = modifier.fillMaxWidth()
            )
        }
    }
}
