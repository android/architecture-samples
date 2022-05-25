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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Collects values from this [StateFlow] and represents its latest value via [State] in a
 * lifecycle-aware manner.
 *
 * The [StateFlow.value] is used as an initial value. Every time there would be new value posted
 * into the [StateFlow] the returned [State] will be updated causing recomposition of every
 * [State.value] usage whenever the [lifecycle] is at least [minActiveState].
 *
 * This [StateFlow] is collected every time [lifecycle] reaches the [minActiveState] Lifecycle
 * state. The collection stops when [lifecycle] falls below [minActiveState].
 *
 * @sample androidx.lifecycle.runtime.compose.samples.StateFlowCollectAsStateWithLifecycle
 *
 * Warning: [Lifecycle.State.INITIALIZED] is not allowed in this API. Passing it as a
 * parameter will throw an [IllegalArgumentException].
 *
 * @param lifecycle The [Lifecycle] where the restarting collecting from `this` flow work will be
 * kept alive.
 * @param minActiveState [Lifecycle.State] in which the upstream flow gets collected. The
 * collection will stop if the lifecycle falls below that state, and will restart if it's in that
 * state again.
 */
@Composable
fun <T> StateFlow<T>.collectAsStateWithLifecycle(
    lifecycle: Lifecycle = LocalLifecycleOwner.current.lifecycle,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED
): State<T> = collectAsStateWithLifecycle(
    initialValue = remember { this.value },
    lifecycle = lifecycle,
    minActiveState = minActiveState
)

/**
 * Collects values from this [Flow] and represents its latest value via [State] in a
 * lifecycle-aware manner.
 *
 * Every time there would be new value posted into the [Flow] the returned [State] will be updated
 * causing recomposition of every [State.value] usage whenever the [lifecycle] is at
 * least [minActiveState].
 *
 * This [Flow] is collected every time [lifecycle] reaches the [minActiveState] Lifecycle
 * state. The collection stops when [lifecycle] falls below [minActiveState].
 *
 * @sample androidx.lifecycle.runtime.compose.samples.FlowCollectAsStateWithLifecycle
 *
 * Warning: [Lifecycle.State.INITIALIZED] is not allowed in this API. Passing it as a
 * parameter will throw an [IllegalArgumentException].
 *
 * @param initialValue The initial value given to the returned [State.value].
 * @param lifecycle The [Lifecycle] where the restarting collecting from `this` flow work will be
 * kept alive.
 * @param minActiveState [Lifecycle.State] in which the upstream flow gets collected. The
 * collection will stop if the lifecycle falls below that state, and will restart if it's in that
 * state again.
 */
@Composable
fun <T> Flow<T>.collectAsStateWithLifecycle(
    initialValue: T,
    lifecycle: Lifecycle = LocalLifecycleOwner.current.lifecycle,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED
): State<T> {
    val currentValue = remember(this) { initialValue }
    return produceState(
        initialValue = currentValue,
        key1 = this,
        key2 = lifecycle,
        key3 = minActiveState
    ) {
        lifecycle.repeatOnLifecycle(minActiveState) {
            this@collectAsStateWithLifecycle.collect {
                this@produceState.value = it
            }
        }
    }
}
