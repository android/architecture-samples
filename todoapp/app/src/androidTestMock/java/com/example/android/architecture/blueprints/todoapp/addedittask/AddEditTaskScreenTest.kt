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

import android.content.Intent
import android.content.res.Resources
import android.support.test.InstrumentationRegistry
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.clearText
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.intent.rule.IntentsTestRule
import android.support.test.espresso.matcher.BoundedMatcher
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.filters.LargeTest
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.support.v7.widget.Toolbar
import android.view.View
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.R.id.toolbar
import com.example.android.architecture.blueprints.todoapp.TestUtils
import com.example.android.architecture.blueprints.todoapp.data.FakeTasksRemoteDataSource
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository
import org.hamcrest.Description
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

     *
     *
     * Rules are interceptors which are executed for each test method and are important building
     * blocks of Junit tests.
     */
    @Rule @JvmField var activityTestRule = ActivityTestRule(AddEditTaskActivity::class.java,
            false, false)

    @Test fun emptyTask_isNotSaved() {
        // Launch activity to add a new task
        launchNewTaskActivity(null)

        // Add invalid title and description combination
        onView(withId(R.id.add_task_title)).perform(clearText())
        onView(withId(R.id.add_task_description)).perform(clearText())
        // Try to save the task
        onView(withId(R.id.fab_edit_task_done)).perform(click())

        // Verify that the activity is still displayed (a correct task would close it).
        onView(withId(R.id.add_task_title)).check(matches(isDisplayed()))
    }

    @Test fun toolbarTitle_newTask_persistsRotation() {
        // Launch activity to add a new task
        launchNewTaskActivity(null)

        // Check that the toolbar shows the correct title
        onView(withId(toolbar)).check(matches(withToolbarTitle(R.string.add_task)))

        // Rotate activity
        TestUtils.rotateOrientation(activityTestRule.activity)

        // Check that the toolbar title is persisted
        onView(withId(toolbar)).check(matches(withToolbarTitle(R.string.add_task)))
    }

    @Test fun toolbarTitle_editTask_persistsRotation() {
        // Put a task in the repository and start the activity to edit it
        TasksRepository.destroyInstance()
        FakeTasksRemoteDataSource.getInstance().addTasks(Task("Title1", "", TASK_ID).apply {
            isCompleted = false
        })
        launchNewTaskActivity(TASK_ID)

        // Check that the toolbar shows the correct title
        onView(withId(toolbar)).check(matches(withToolbarTitle(R.string.edit_task)))

        // Rotate activity
        TestUtils.rotateOrientation(activityTestRule.activity)

        // check that the toolbar title is persisted
        onView(withId(toolbar)).check(matches(withToolbarTitle(R.string.edit_task)))
    }

    /**
     * @param taskId is null if used to add a new task, otherwise it edits the task.
     */
    private fun launchNewTaskActivity(taskId: String?) {
        val intent = Intent(InstrumentationRegistry.getInstrumentation()
                .targetContext, AddEditTaskActivity::class.java)
                .apply {
                    putExtra(AddEditTaskFragment.ARGUMENT_EDIT_TASK_ID, taskId)
                }
        activityTestRule.launchActivity(intent)
    }

    /**
     * Matches the toolbar title with a specific string resource.

     * @param resourceId the ID of the string resource to match
     */
    private fun withToolbarTitle(resourceId: Int) = object :
            BoundedMatcher<View, Toolbar>(Toolbar::class.java) {

        override fun describeTo(description: Description) {
            with(description) {
                appendText("with toolbar title from resource id: ")
                appendValue(resourceId)
            }
        }

        override fun matchesSafely(toolbar: Toolbar): Boolean {
            var expectedText: CharSequence = ""
            try {
                expectedText = toolbar.resources.getString(resourceId)
            } catch (ignored: Resources.NotFoundException) {
                /* view could be from a context unaware of the resource id. */
            }
            return expectedText == toolbar.title
        }
    }


    companion object {
        val TASK_ID = "1"
    }
}
