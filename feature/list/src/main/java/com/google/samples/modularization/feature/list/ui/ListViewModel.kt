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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.samples.modularization.core.data.MyModel
import com.google.samples.modularization.core.data.MyModelRepository
import com.google.samples.modularization.feature.list.ui.ListUiState.Success
import com.google.samples.modularization.util.timestampToReadableDate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ListViewModel @Inject constructor(
    private val myModelRepository: MyModelRepository
) : ViewModel() {

    val uiState: StateFlow<ListUiState> = myModelRepository
        .observeAllModels
        .map<List<MyModel>, ListUiState> { items ->
            items.map { myModel ->
                MyModelUiState(
                    id = myModel.id,
                    title = myModel.title,
                    date = timestampToReadableDate(myModel.timestamp),
                    isBookmarked = myModel.isBookmarked
                )
            }.let(::Success)
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            ListUiState.Loading
        )

    fun bookmark(id: Long, isBookmarked: Boolean) {
        viewModelScope.launch {
            myModelRepository.bookmark(id, isBookmarked)
        }
    }
}
