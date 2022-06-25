package com.example.android.architecture.blueprints.todoapp.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Definition of a unit of change for a State.
 */
typealias Mutation<State> = State.() -> State

/**
 * Syntactic sugar for creating a [Mutation]
 */
fun <State> mutation(mutation: State.() -> State): Mutation<State> = mutation

object Mutations {
    /**
     * Identity state change function; semantically a no op [Mutation]
     */
    fun <T : Any> identity(): Mutation<T> = mutation { this }
}

/**
 * Combines two state changes into a single state change
 */
operator fun <T : Any> Mutation<T>.plus(other: Mutation<T>): Mutation<T> = inner@{
    val result = this@plus(this@inner)
    other.invoke(result)
}

/**
 * Produces a [StateFlow] by merging [mutationFlows] and reducing them into an
 * [initial] state
 */
fun <State : Any> CoroutineScope.produceState(
    initial: State,
    started: SharingStarted = WhileUiSubscribed,
    mutationFlows: List<Flow<Mutation<State>>>
): StateFlow<State> {
    // Set the seed for the state
    var seed = initial

    // Use the flow factory function to capture the seed variable
    return flow {
        emitAll(
            merge(*mutationFlows.toTypedArray())
                // Reduce into the seed so if resubscribed, the last value of state is persisted
                // when the flow pipeline is started again
                .scan(seed) { state, mutation -> mutation(state) }
                // Set seed after each emission
                .onEach { seed = it }
        )
    }
        .stateIn(
            scope = this,
            started = started,
            initialValue = seed
        )
}

/**
 * Manges state production for [State]
 */
class StateProducer<State : Any>(
    private val scope: CoroutineScope,
    initial: State,
    started: SharingStarted = WhileUiSubscribed,
    mutationFlows: List<Flow<Mutation<State>>>
) {
    private val stateSetter = MutableSharedFlow<Mutation<State>>()

    val state = scope.produceState(
        initial = initial,
        started = started,
        mutationFlows = mutationFlows + stateSetter
    )

    suspend fun setState(mutation: Mutation<State>) = stateSetter.emit(mutation)

    /**
     * Runs [block] in [CoroutineScope] that has a [Job] as the child of the [Job] of [scope].
     * This allows for the child [Job] to be passed a parameter in the [block] lambda allowing
     * for cancelling the coroutine that runs [block].
     */
    fun launch(
        block: suspend StateProducer<State>.() -> Unit
    ) {
        scope.launch {
            block()
        }
    }
}
