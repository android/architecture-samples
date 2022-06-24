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

package com.example.android.architecture.blueprints.todoapp.util

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

private const val StopTimeoutMillis: Long = 5000

/**
 * A [SharingStarted] meant to be used with a [StateFlow] to expose data to the UI.
 *
 * When the UI stops observing, upstream flows stay active for some time to allow the system to
 * come back from a short-lived configuration change (such as rotations). If the UI stops
 * observing for longer, the cache is kept but the upstream flows are stopped. When the UI comes
 * back, the latest value is replayed and the upstream flows are executed again. This is done to
 * save resources when the app is in the background but let users switch between apps quickly.
 */
private val WhileUiSubscribed: SharingStarted = SharingStarted.WhileSubscribed(StopTimeoutMillis)

/**
 * Returns a [StateFlow] configured to safely expose data to the UI. Upstreams flows will be
 * cancelled 5 seconds after the UI cancels [this] StateFlow collection.
 */
fun <T, R> ViewModel.produceUiState(
    flow: Flow<T>,
    initialValue: R,
    started: SharingStarted = WhileUiSubscribed,
    uiStateProducerBlock: (T) -> R
): StateFlow<R> = flow.map(uiStateProducerBlock)
    .stateIn(
        scope = viewModelScope,
        started = started,
        initialValue = initialValue
    )

fun <T1, T2, R> ViewModel.produceUiState(
    flow: Flow<T1>,
    flow2: Flow<T2>,
    initialValue: R,
    started: SharingStarted = WhileUiSubscribed,
    uiStateProducerBlock: (T1, T2) -> R
): StateFlow<R> = combine(flow, flow2, uiStateProducerBlock)
    .stateIn(
        scope = viewModelScope,
        started = started,
        initialValue = initialValue
    )

fun <T1, T2, T3, R> ViewModel.produceUiState(
    flow: Flow<T1>,
    flow2: Flow<T2>,
    flow3: Flow<T3>,
    initialValue: R,
    started: SharingStarted = WhileUiSubscribed,
    uiStateProducerBlock: (T1, T2, T3) -> R
): StateFlow<R> = combine(flow, flow2, flow3, uiStateProducerBlock)
    .stateIn(
        scope = viewModelScope,
        started = started,
        initialValue = initialValue
    )

fun <T1, T2, T3, T4, R> ViewModel.produceUiState(
    flow: Flow<T1>,
    flow2: Flow<T2>,
    flow3: Flow<T3>,
    flow4: Flow<T4>,
    initialValue: R,
    started: SharingStarted = WhileUiSubscribed,
    uiStateProducerBlock: (T1, T2, T3, T4) -> R
): StateFlow<R> = combine(flow, flow2, flow3, flow4, uiStateProducerBlock)
    .stateIn(
        scope = viewModelScope,
        started = started,
        initialValue = initialValue
    )

fun <T1, T2, T3, T4, T5, R> ViewModel.produceUiState(
    flow: Flow<T1>,
    flow2: Flow<T2>,
    flow3: Flow<T3>,
    flow4: Flow<T4>,
    flow5: Flow<T5>,
    initialValue: R,
    started: SharingStarted = WhileUiSubscribed,
    uiStateProducerBlock: (T1, T2, T3, T4, T5) -> R
): StateFlow<R> = combine(flow, flow2, flow3, flow4, flow5, uiStateProducerBlock)
    .stateIn(
        scope = viewModelScope,
        started = started,
        initialValue = initialValue
    )
