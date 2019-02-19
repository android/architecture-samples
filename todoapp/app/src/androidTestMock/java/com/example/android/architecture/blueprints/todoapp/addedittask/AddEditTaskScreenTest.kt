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
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.clearText
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.data.FakeTasksRemoteDataSource
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository
import com.example.android.architecture.blueprints.todoapp.rotateOrientation
import com.example.android.architecture.blueprints.todoapp.util.EspressoIdlingResource
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests for the add task screen.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class AddEditTaskScreenTest {

    val TASK_ID = "1"

    /**
     * [IntentsTestRule] is an [ActivityTestRule] which inits and releases Espresso
     * Intents before and after each test run.
     * <p>
     * <p>
     * Rules are interceptors which are executed for each test method and are important building
     * blocks of Junit tests.
     */
    @get:Rule var activityTestRule = ActivityTestRule(AddEditTaskActivity::class.java, false, false)

    /**
     * Prepare your test fixture for this test. In this case we register an IdlingResources with
     * Espresso. IdlingResource resource is a great way to tell Espresso when your app is in an
     * idle state. This helps Espresso to synchronize your test actions, which makes tests
     * significantly more reliable.
     */
    @Before fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
    }

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
        onView(withId(R.id.toolbar)).check(matches(withToolbarTitle(R.string.add_task)))

        // Rotate activity
        activityTestRule.activity.rotateOrientation()

        // Check that the toolbar title is persisted
        onView(withId(R.id.toolbar)).check(matches(withToolbarTitle(R.string.add_task)))
    }

    @Test fun toolbarTitle_editTask_persistsRotation() {
        // Put a task in the repository and start the activity to edit it
        TasksRepository.destroyInstance()
        FakeTasksRemoteDataSource.addTasks(
                Task("Title1", "", TASK_ID).apply { isCompleted = false }
        )
        launchNewTaskActivity(TASK_ID)

        // Check that the toolbar shows the correct title
        onView(withId(R.id.toolbar)).check(matches(withToolbarTitle(R.string.edit_task)))

        // Rotate activity
        activityTestRule.activity.rotateOrientation()

        // check that the toolbar title is persisted
        onView(withId(R.id.toolbar)).check(matches(withToolbarTitle(R.string.edit_task)))
    }

    /**
     * @param taskId is null if used to add a new task, otherwise it edits the task.
     */
    private fun launchNewTaskActivity(taskId: String?) {
        val intent = Intent(ApplicationProvider.getApplicationContext(),
                AddEditTaskActivity::class.java)
                .apply { putExtra(AddEditTaskFragment.ARGUMENT_EDIT_TASK_ID, taskId) }
        activityTestRule.launchActivity(intent)
    }

    /**
     * Matches the toolbar title with a specific string resource.
     *
     * @param resourceId the ID of the string resource to match
     */
    private fun withToolbarTitle(resourceId: Int): Matcher<View> =
            object : BoundedMatcher<View, Toolbar>(Toolbar::class.java) {

                override fun describeTo(description: Description) {
                    description.appendText("with toolbar title from resource id: ")
                    description.appendValue(resourceId)
                }

                override fun matchesSafely(toolbar: Toolbar): Boolean {
                    var expectedText = ""
                    try {
                        expectedText = toolbar.resources.getString(resourceId)
                    } catch (ignored: Resources.NotFoundException) {
                        /* View could be from a context unaware of the resource id. */
                    }
                    val actualText = toolbar.title
                    return expectedText == actualText
                }
            }


    /**
     * Unregister your Idling Resource so it can be garbage collected and does not leak any memory.
     */
    @After fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
    }
}
