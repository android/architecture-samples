package com.example.android.architecture.blueprints.todoapp.util

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher

/**
 * Runs [testBody] while subscribed to [this@whileSubscribedTo]
 */
@ExperimentalCoroutinesApi
suspend fun <T> StateFlow<T>.whileSubscribed(
    testBody: suspend () -> Unit
) {
    coroutineScope {
        val collectJob = launch(UnconfinedTestDispatcher()) { collect {} }
        testBody()
        collectJob.cancel()
    }
}
