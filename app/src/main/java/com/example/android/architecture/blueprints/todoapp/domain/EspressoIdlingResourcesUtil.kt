package com.example.android.architecture.blueprints.todoapp.domain

import com.example.android.architecture.blueprints.todoapp.util.EspressoIdlingResource

inline fun <T> wrapEspressoIdlingResource(function: () -> T) : T {
    // Espresso does not work well with coroutines yet. See
    // https://github.com/Kotlin/kotlinx.coroutines/issues/982
    EspressoIdlingResource.increment() // Set app as busy.
    return try {
        function()
    } finally {
        EspressoIdlingResource.decrement() // Set app as idle.
    }
}