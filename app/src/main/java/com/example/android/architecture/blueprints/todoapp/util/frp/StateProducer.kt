package com.example.android.architecture.blueprints.todoapp.util.frp

import com.example.android.architecture.blueprints.todoapp.util.WhileUiSubscribed
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Interface for a type that produces a [StateFlow] of [State] by processing [Action]
 */
interface StateProducer<Action : Any, State : Any> {
    val process: (Action) -> Unit

    val state: StateFlow<State>
}

/**
 * Data class holding a change transform for a some state [T].
 */
data class StateChange<T : Any>(
    val mutate: T.() -> T
) {
    companion object {
        /**
         * Identity state change function; semantically a no op [StateChange]
         */
        fun <T : Any> identity(): StateChange<T> = StateChange { this }
    }
}

/**
 * Defines a [StateProducer] to convert a [Flow] of [Action] into a [StateFlow] of [State].
 *
 * [scope]: The [CoroutineScope] for the resulting [StateFlow]. Any [Action]s sent if there are no
 * subscribers to the output [StateFlow] will suspend until there is as least one subscriber.
 *
 * [initialState]: The seed state for the resulting [StateFlow].
 *
 * [started]: Semantics for the "hotness" of the output [StateFlow] @see [Flow.stateIn]
 *
 * [stateTransform]: Further transformations o be applied to the output [StateFlow]
 *
 * [actionTransform]: Defines the transformations to the [Action] [Flow] to create [StateChange]s
 * of state that will be reduced into the [initialState]. This is often achieved through the
 * [toStateChangeStream] [Flow] extension function.
 */
fun <Action : Any, State : Any> stateProducer(
    scope: CoroutineScope,
    initialState: State,
    started: SharingStarted = WhileUiSubscribed,
    stateTransform: (Flow<State>) -> Flow<State> = { it },
    actionTransform: (Flow<Action>) -> Flow<StateChange<State>>
): StateProducer<Action, State> = object : StateProducer<Action, State> {
    var seed = initialState
    val actions = MutableSharedFlow<Action>()

    override val state: StateFlow<State> =
        stateTransform(
            flow {
                // Seed the reduction with the last produced state
                emitAll(
                    actionTransform(actions)
                        .reduceInto(seed)
                        .onEach(::seed::set)
                )
            }
        )
            .stateIn(
                scope = scope,
                started = started,
                initialValue = initialState
            )

    override val process: (Action) -> Unit = { action ->
        scope.launch {
            // Suspend till downstream is connected
            actions.subscriptionCount.first { it > 0 }
            actions.emit(action)
        }
    }
}
