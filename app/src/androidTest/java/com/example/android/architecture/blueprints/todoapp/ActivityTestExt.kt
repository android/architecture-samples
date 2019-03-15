/*
 * Copyright (C) 2017 The Android Open Source Project
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

/**
 * Various extension functions for Activity, helpful for instrumentation testing.
 */

import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import androidx.annotation.IdRes
import androidx.appcompat.widget.Toolbar
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.lifecycle.ActivityLifecycleMonitor
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage

private fun Activity.rotateToLandscape() {
    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
}

private fun Activity.rotateToPortrait() {
    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
}

fun Activity.rotateOrientation() {
    when (resources.configuration.orientation) {
        Configuration.ORIENTATION_LANDSCAPE -> rotateToPortrait()
        Configuration.ORIENTATION_PORTRAIT -> rotateToLandscape()
        else -> rotateToLandscape()
    }
}

/**
 * Returns the content description for the navigation button view in the toolbar.
 */
fun Activity.getToolbarNavigationContentDescription(@IdRes toolbarId: Int) =
        findViewById<Toolbar>(toolbarId).navigationContentDescription as String

/**
 * Gets an Activity in the RESUMED stage.
 *
 *
 * This method should never be called from the Main thread. In certain situations there might
 * be more than one Activities in RESUMED stage, but only one is returned.
 * See [ActivityLifecycleMonitor].
 */
// The array is just to wrap the Activity and be able to access it from the Runnable.
val currentActivity: Activity
    get() {
        // The array is just to wrap the Activity and be able to access it from the Runnable.
        val resumedActivity = arrayOfNulls<Activity>(1)
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            val resumedActivities = ActivityLifecycleMonitorRegistry.getInstance()
                    .getActivitiesInStage(Stage.RESUMED)
            if (resumedActivities.iterator().hasNext()) {
                resumedActivity[0] = resumedActivities.iterator().next() as Activity
            } else {
                throw IllegalStateException("No Activity in stage RESUMED")
            }
        }

        // Ugly but necessary since resumedActivity[0] needs to be declared in the outer scope
        // and assigned in the runnable.
        return resumedActivity[0]!!
    }