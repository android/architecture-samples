package com.example.android.architecture.blueprints.todoapp.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

interface StateSetter<State> {
    suspend fun setState(mutation: Mutation<State>)
}

/**
 * Manges state production for [State] while guaranteeing any coroutines launched in it are only
 * active as specified by the [SharingStarted] specified.
 */
class StateProducer<State : Any>(
    private val scope: CoroutineScope,
    initial: State,
    started: SharingStarted = WhileUiSubscribed,
    mutationFlows: List<Flow<Mutation<State>>>
) {

    /**
     * Pipeline for launching adhoc requests, used make requests run in parallel.
     */
    private val setStateMutations = MutableSharedFlow<Flow<Mutation<State>>>()

    val state = scope.produceState(
        initial = initial,
        started = started,
        mutationFlows = mutationFlows + setStateMutations.flatMapMerge(
            // Run all mutation flows in parallel
            concurrency = Int.MAX_VALUE,
            transform = { it }
        )
    )

    /**
     * Runs [block] in [scope] independently, but limits it to the [SharingStarted] specified.
     */
    fun launch(block: suspend StateSetter<State>.() -> Unit) {
        scope.launch {
            // Use the flow builder to make sure every suspend function launched runs in parallel
            setStateMutations.emit(
                flow {
                    block(this.asStateSetter())
                }
            )
        }
    }
}

private fun <State> FlowCollector<Mutation<State>>.asStateSetter(): StateSetter<State> =
    object : StateSetter<State> {
        override suspend fun setState(mutation: Mutation<State>) = emit(mutation)
    }
