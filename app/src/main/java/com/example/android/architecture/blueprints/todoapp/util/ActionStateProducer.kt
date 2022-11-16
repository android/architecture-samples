package com.example.android.architecture.blueprints.todoapp.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ActionTransformBuilder<Action : Any, State: Any> {
    val actionHandlers = ArrayList<(Flow<Action>) -> Flow<Mutation<State>>>()

    inline fun <reified T: Action> onAction(
        noinline block: TransformationContext<T>.() -> Flow<Mutation<State>>
    ) {
        actionHandlers += { action ->
            action.filterIsInstance<T>().toMutationStream(transform = block)
        }
    }
}

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
    actionTransform: ActionTransformBuilder<Action, State>.() -> Unit
): ActionStateProducer<Action, State> = object : ActionStateProducer<Action, State> {
    val actions = MutableSharedFlow<Action>()
    val builder = ActionTransformBuilder<Action, State>()

    init {
        actionTransform(builder)
    }

    override val state: StateFlow<State> = scope.produceState(
        initial = initialState,
        started = started,
        mutationFlows = mutationFlows + builder.actionHandlers.map { it(actions) }
    )

    override val process: (Action) -> Unit = { action ->
        scope.launch {
            // Suspend till downstream is connected
            actions.subscriptionCount.first { it > 0 }
            actions.emit(action)
        }
    }
}
