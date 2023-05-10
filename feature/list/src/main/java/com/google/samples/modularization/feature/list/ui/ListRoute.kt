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

package com.google.samples.modularization.feature.list.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.samples.modularization.ui.Loading

@Composable
fun ListRoute(
    onGoToItem: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ListViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    ListScreen(state, onGoToItem, viewModel::bookmark, modifier)
}

@Composable
internal fun ListScreen(
    state: ListUiState,
    onGoToItem: (Long) -> Unit,
    onBookmarkItem: (Long, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    when (state) {
        ListUiState.Loading -> Loading(modifier)
        is ListUiState.Success -> Content(state.data, onGoToItem, onBookmarkItem, modifier)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun Content(
    items: List<MyModelUiState>,
    onGoToItem: (Long) -> Unit,
    onBookmarkItem: (Long, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        itemsIndexed(items = items) { index, item ->
            ListItem(
                headlineText = { Text(text = item.title) },
                trailingContent = { Text(text = item.date) },
                leadingContent = {
                    Checkbox(
                        checked = item.isBookmarked,
                        onCheckedChange = { isChecked ->
                            onBookmarkItem(item.id, isChecked)
                        }
                    )
                },
                modifier = Modifier
                    .clickable {
                        onGoToItem(item.id)
                    }
                    .testTag("item_$index")
            )
        }
    }
}
