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

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LifecycleRegistry


object TestUtils {
    val TEST_OBSERVER: LifecycleOwner = object : LifecycleOwner {

        private val registry = LifecycleRegistry(this).apply {
            // Creates a LifecycleRegistry in RESUMED state.
            handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
            handleLifecycleEvent(Lifecycle.Event.ON_START)
            handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        }

        override fun getLifecycle() = registry
    }
}
