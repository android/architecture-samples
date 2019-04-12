/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.architecture.blueprints.todoapp

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineContext
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import java.util.concurrent.Executors
import kotlin.coroutines.ContinuationInterceptor

/**
 * Sets the main coroutines dispatcher for unit testing.
 *
 * Uses the deprecated TestCoroutineContext if provided. Otherwise it uses a new single thread
 * executor.
 * See https://medium.com/androiddevelopers/easy-coroutines-in-android-viewmodelscope-25bffb605471
 * and https://github.com/Kotlin/kotlinx.coroutines/issues/541
 */
@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class ViewModelScopeMainDispatcherRule(
    private val testContext: TestCoroutineContext? = null
) : TestWatcher() {

    private val singleThreadExecutor = Executors.newSingleThreadExecutor()

    override fun starting(description: Description?) {
        super.starting(description)
        if (testContext != null) {
            Dispatchers.setMain(testContext[ContinuationInterceptor] as CoroutineDispatcher)
        } else {
            Dispatchers.setMain(singleThreadExecutor.asCoroutineDispatcher())
        }
    }

    override fun finished(description: Description?) {
        super.finished(description)
        singleThreadExecutor.shutdownNow()
        Dispatchers.resetMain()
    }
}
