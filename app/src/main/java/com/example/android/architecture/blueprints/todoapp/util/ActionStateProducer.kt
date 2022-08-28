package com.example.android.architecture.blueprints.todoapp.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Interface for a type that produces a [StateFlow] of [State] by processing [Action]
 */
interface ActionStateProducer<Action : Any, State : Any> {
    val process: (Action) -> Unit

    val state: StateFlow<State>
}

/**
 * Defines a [ActionStateProducer] to convert a [Flow] of [Action] into a [StateFlow] of [State].
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
 * [actionTransform]: Defines the transformations to the [Action] [Flow] to create [Mutation]s
 * of state that will be reduced into the [initialState]. This is often achieved through the
 * [toMutationStream] [Flow] extension function.
 */
fun <Action : Any, State : Any> actionStateProducer(
    scope: CoroutineScope,
    initialState: State,
    started: SharingStarted = WhileUiSubscribed,
    mutationFlows: List<Flow<Mutation<State>>> = listOf(),
    actionTransform: (Flow<Action>) -> Flow<Mutation<State>>
): ActionStateProducer<Action, State> = object : ActionStateProducer<Action, State> {
    val actions = MutableSharedFlow<Action>()

    override val state: StateFlow<State> = scope.produceState(
        initial = initialState,
        started = started,
        mutationFlows = mutationFlows + actionTransform(actions)
    )

    override val process: (Action) -> Unit = { action ->
        scope.launch {
            // Suspend till downstream is connected
            actions.subscriptionCount.first { it > 0 }
            actions.emit(action)
        }
    }
}
