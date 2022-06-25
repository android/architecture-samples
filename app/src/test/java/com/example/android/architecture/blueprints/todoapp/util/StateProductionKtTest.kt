package com.example.android.architecture.blueprints.todoapp.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

private data class State(
    val value: Int = 0
)

@ExperimentalCoroutinesApi
class StateProductionKtTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var scope: CoroutineScope
    private lateinit var eventStateChanges: MutableSharedFlow<Mutation<State>>

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        scope = TestScope(testDispatcher)
        eventStateChanges = MutableSharedFlow()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun test_simple_state_production() = runTest {
        val state = scope.produceState(
            initial = State(),
            mutationFlows = listOf(
                eventStateChanges
            )
        )

        state.whileSubscribed {
            val state1 = state.first()
            assertEquals(State(0), state1)

            eventStateChanges.emit { copy(value = value + 1) }
            val state2 = state.first { it != state1 }
            assertEquals(State(1), state2)
        }
    }

    @Test
    fun test_state_production_persists_after_unsubscribing() = runTest {
        val state = scope.produceState(
            initial = State(),
            started = SharingStarted.WhileSubscribed(),
            mutationFlows = listOf(
                eventStateChanges
            )
        )

        // Subscribe the first time
        state.whileSubscribed {
            val state1 = state.first()
            assertEquals(State(0), state1)

            eventStateChanges.emit { copy(value = value + 1) }
            val state2 = state.first { it != state1 }
            assertEquals(State(1), state2)
        }

        // Subscribe again. The state flow value should not be reset by the pipeline restarting
        state.whileSubscribed {
            val state2 = state.first()
            assertEquals(State(1), state2)
        }
    }

    @Test
    fun test_state_production_with_merged_flows() = runTest {
        val state = scope.produceState(
            initial = State(),
            started = SharingStarted.WhileSubscribed(),
            mutationFlows = listOf(
                eventStateChanges,
                flow {
                    delay(1000)
                    emit(mutation { copy(value = 3) })

                    delay(1000)
                    emit(mutation { copy(value = 7) })
                }
            )
        )

        state.whileSubscribed {
            val state1 = state.first()
            assertEquals(State(0), state1)

            advanceTimeBy(1200)
            val state2 = state.first { it != state1 }
            assertEquals(State(3), state2)

            advanceTimeBy(1200)
            val state3 = state.first { it != state2 }
            assertEquals(State(7), state3)

            eventStateChanges.emit { copy(value = 0) }
            val state4 = state.first { it != state3 }
            assertEquals(State(0), state4)
        }
    }

    @Test
    fun test_state_change_addition() {
        val additionMutation = mutation<State> {
            copy(value = value + 1)
        } +
            mutation {
                copy(value = value + 1)
            } +
            mutation {
                copy(value = value + 1)
            }

        val state = additionMutation(State())

        assertEquals(State(3), state)
    }
}
