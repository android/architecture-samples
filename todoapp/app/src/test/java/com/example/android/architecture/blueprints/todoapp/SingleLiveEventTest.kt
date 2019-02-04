/*
 *  Copyright 2017 Google Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.example.android.architecture.blueprints.todoapp

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.Observer
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations


class SingleLiveEventTest {

    // Execute tasks synchronously
    @get:Rule var instantExecutorRule = InstantTaskExecutorRule()
    // The class that has the lifecycle
    @Mock private lateinit var owner: LifecycleOwner
    // The observer of the event under test
    @Mock private lateinit var eventObserver: Observer<Int>
    // Defines the Android Lifecycle of an object, used to trigger different events
    private lateinit var lifecycle: LifecycleRegistry
    // Event object under test
    private val singleLiveEvent = SingleLiveEvent<Int>()

    @Before fun setUpLifecycles() {
        MockitoAnnotations.initMocks(this)

        // Link custom lifecycle owner with the lifecyle register.
        lifecycle = LifecycleRegistry(owner)
        `when`(owner.lifecycle).thenReturn(lifecycle)

        // Start observing
        singleLiveEvent.observe(owner, eventObserver)

        // Start in a non-active state
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
    }

    @Test fun valueNotSet_onFirstOnResume() {
        // On resume
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        // no update should be emitted because no value has been set
        verify<Observer<Int>>(eventObserver, never()).onChanged(ArgumentMatchers.anyInt())
    }

    @Test fun singleUpdate_onSecondOnResume_updatesOnce() {
        // After a value is set
        singleLiveEvent.setValue(42)

        with(lifecycle) {
            // observers are called once on resume
            handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

            // on second resume, no update should be emitted.
            handleLifecycleEvent(Lifecycle.Event.ON_STOP)
            handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        }

        // Check that the observer is called once
        verify<Observer<Int>>(eventObserver, times(1)).onChanged(ArgumentMatchers.anyInt())
    }

    @Test fun twoUpdates_updatesTwice() {
        // After a value is set
        singleLiveEvent.setValue(42)

        // observers are called once on resume
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        // when the value is set again, observers are called again.
        singleLiveEvent.setValue(23)

        // Check that the observer has been called twice
        verify<Observer<Int>>(eventObserver, times(2)).onChanged(ArgumentMatchers.anyInt())
    }

    @Test fun twoUpdates_noUpdateUntilActive() {
        // Set a value
        singleLiveEvent.setValue(42)

        // which doesn't emit a change
        verify<Observer<Int>>(eventObserver, never()).onChanged(42)

        // and set it again
        singleLiveEvent.setValue(42)

        // observers are called once on resume.
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        // Check that the observer is called only once
        verify<Observer<Int>>(eventObserver, times(1)).onChanged(ArgumentMatchers.anyInt())
    }
}
