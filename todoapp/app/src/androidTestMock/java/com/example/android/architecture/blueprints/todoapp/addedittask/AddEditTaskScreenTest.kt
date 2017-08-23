/*
 * Copyright 2017, The Android Open Source Project
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
package com.example.android.architecture.blueprints.todoapp.addedittask

import android.support.test.espresso.Espresso
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.clearText
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.intent.rule.IntentsTestRule
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.filters.LargeTest
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.util.EspressoIdlingResource
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests for the add task screen.
 */
@RunWith(AndroidJUnit4::class) @LargeTest class AddEditTaskScreenTest {

    /**
     * [IntentsTestRule] is an [ActivityTestRule] which inits and releases Espresso
     * Intents before and after each test run.
     * <p>
     * <p>
     * Rules are interceptors which are executed for each test method and are important building
     * blocks of Junit tests.
     */
    @get:Rule var addTaskIntentsTestRule = IntentsTestRule(AddEditTaskActivity::class.java)

    /**
     * Prepare your test fixture for this test. In this case we register an IdlingResources with
     * Espresso. IdlingResource resource is a great way to tell Espresso when your app is in an
     * idle state. This helps Espresso to synchronize your test actions, which makes tests
     * significantly more reliable.
     */
    @Before fun registerIdlingResource() {
        Espresso.registerIdlingResources(EspressoIdlingResource.countingIdlingResource)
    }

    @Test fun emptyTask_isNotSaved() {
        // Add invalid title and description combination
        onView(withId(R.id.add_task_title)).perform(clearText())
        onView(withId(R.id.add_task_description)).perform(clearText())
        // Try to save the task
        onView(withId(R.id.fab_edit_task_done)).perform(click())

        // Verify that the activity is still displayed (a correct task would close it).
        onView(withId(R.id.add_task_title)).check(matches(isDisplayed()))
    }

    /**
     * Unregister your Idling Resource so it can be garbage collected and does not leak any memory.
     */
    @After fun unregisterIdlingResource() {
        Espresso.unregisterIdlingResources(EspressoIdlingResource.countingIdlingResource)
    }
}
