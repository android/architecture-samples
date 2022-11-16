package com.example.android.architecture.blueprints.todoapp.util

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.scan

/**
 * Class holding the context of the [Action] emitted that is being split out into
 * a [Mutation] [Flow].
 */
data class TransformationContext<Action : Any>(
    val flow: Flow<Action>
)

/**
 * Transforms a [Flow] of [Action] to a [Flow] of [Mutation] of [State], allowing for finer grained
 * transforms on subtypes of [Action]. This allows for certain actions to be processed differently
 * than others. For example: a certain action may need to only cause changes on distinct
 * emissions, whereas other actions may need to use more complex [Flow] transformations like
 * [Flow.flatMapMerge] and so on.
 *
 *  [keySelector]: The mapping for the [Action] to the key used to identify it. This is useful
 *  for nested class hierarchies. By default each distinct type will be split out, but if you want
 *  to treat certain subtypes as one type, this lets you do that.
 *
 *  [transform]: a function for mapping independent [Flow]s of [Action] to [Flow]s of [State]
 *  [Mutation]s
 * @see [splitByType]
 */
fun <Action : Any, State : Any> Flow<Action>.toMutationStream(
    keySelector: (Action) -> String = Any::defaultKeySelector,
    // Ergonomic hack to simulate multiple receivers
    transform: TransformationContext<Action>.() -> Flow<Mutation<State>>
): Flow<Mutation<State>> = splitByType(
    typeSelector = { it },
    keySelector = keySelector,
    transform = transform
)

/**
 * Transforms a [Flow] of [Input] to a [Flow] of [Output] by splitting the original into [Flow]s
 * of type [Selector]. Each independent [Flow] of the [Selector] type can then be transformed
 * into a [Flow] of [Output].
 *
 * [typeSelector]: The mapping to the type the [Input] [Flow] should be split into
 *
 * [keySelector]: The mapping to the [Selector] to the key used to identify it. This is useful
 * for nested class hierarchies. By default each distinct type will be split out, but if you want
 * to treat certain subtypes as one type, this lets you do that.
 * [transform]: a function for mapping independent [Flow]s of [Selector] to [Flow]s of [Output]
 */
fun <Input : Any, Selector : Any, Output : Any> Flow<Input>.splitByType(
    typeSelector: (Input) -> Selector,
    keySelector: (Selector) -> String = Any::defaultKeySelector,
    // Ergonomic hack to simulate multiple receivers
    transform: TransformationContext<Selector>.() -> Flow<Output>
): Flow<Output> =
    channelFlow mutationFlow@{
        val keysToFlowHolders = mutableMapOf<String, FlowHolder<Selector>>()
        this@splitByType
            .collect { item ->
                val selected = typeSelector(item)
                val flowKey = keySelector(selected)
                when (val existingHolder = keysToFlowHolders[flowKey]) {
                    null -> {
                        val holder = FlowHolder(selected)
                        keysToFlowHolders[flowKey] = holder
                        val context = TransformationContext(holder.exposedFlow)
                        val mutationFlow = transform(context)
                        channel.send(mutationFlow)
                    }
                    else -> {
                        // Wait for downstream to be connected
                        existingHolder.internalSharedFlow.subscriptionCount.first { it > 0 }
                        existingHolder.internalSharedFlow.emit(selected)
                    }
                }
            }
    }
        .flatMapMerge(
            concurrency = Int.MAX_VALUE,
            transform = { it }
        )

fun <State : Any> Flow<Mutation<State>>.reduceInto(initialState: State): Flow<State> =
    scan(initialState) { state, mutation -> mutation(state) }

/**
 * Container for representing a [Flow] of a subtype of [Action] that has been split out from
 * a [Flow] of [Action]
 */
private data class FlowHolder<Action>(
    val firstEmission: Action,
) {
    val internalSharedFlow: MutableSharedFlow<Action> = MutableSharedFlow()
    val exposedFlow: Flow<Action> = internalSharedFlow.onStart { emit(firstEmission) }
}

private fun Any.defaultKeySelector(): String = this::class.simpleName
    ?: throw IllegalArgumentException(
        "Only well defined classes can be split or specify a different key selector"
    )
