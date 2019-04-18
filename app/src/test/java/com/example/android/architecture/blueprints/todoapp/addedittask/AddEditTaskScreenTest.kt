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

import android.content.Context
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.clearText
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.android.architecture.blueprints.todoapp.R
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.LooperMode
import org.robolectric.annotation.TextLayoutMode

/**
 * Tests for the add task screen.
 */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@TextLayoutMode(TextLayoutMode.Mode.REALISTIC)
class AddEditTaskScreenTest {

    @Test
    fun emptyTask_isNotSaved() {
        // GIVEN - On the "Add Task" screen.
        val bundle = AddEditTaskFragmentArgs(null,
            getApplicationContext<Context>().getString(R.string.add_task)).toBundle()
        launchFragmentInContainer<AddEditTaskFragment>(bundle, R.style.AppTheme)

        // WHEN - Enter invalid title and description combination and click save
        onView(withId(R.id.add_task_title)).perform(clearText())
        onView(withId(R.id.add_task_description)).perform(clearText())
        onView(withId(R.id.fab_save_task)).perform(click())

        // THEN - Entered Task is still displayed (a correct task would close it).
        onView(withId(R.id.add_task_title)).check(matches(isDisplayed()))
    }
}