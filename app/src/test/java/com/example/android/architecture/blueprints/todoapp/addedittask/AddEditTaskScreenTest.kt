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
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.clearText
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.data.FakeTasksRemoteDataSource
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.DefaultTasksRepository
import com.example.android.architecture.blueprints.todoapp.util.rotateOrientation
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests for the add task screen.
 */
@RunWith(AndroidJUnit4::class)
class AddEditTaskScreenTest {

    @Test
    @Ignore("hangs in robolectric, see issue #4724")
    fun emptyTask_isNotSaved() {
        val activityScenario = ActivityScenario.launch(AddEditTaskActivity::class.java)

        // Add invalid title and description combination
        onView(withId(R.id.add_task_title)).perform(clearText())
        onView(withId(R.id.add_task_description)).perform(clearText())
        // Try to save the task
        // This line hangs due to https://github.com/robolectric/robolectric/issues/4724
        onView(withId(R.id.fab_edit_task_done)).perform(click())

        // Verify that the activity is still displayed (a correct task would close it).
        onView(withId(R.id.add_task_title)).check(matches(isDisplayed()))
    }

    @Test
    fun toolbarTitle_newTask_persistsRotation() {
        val activityScenario = ActivityScenario.launch(AddEditTaskActivity::class.java)

        // Check that the toolbar shows the correct title
        onView(withId(R.id.toolbar)).check(matches(withToolbarTitle(R.string.add_task)))

        // Rotate activity
        activityScenario.onActivity {
            it.rotateOrientation()
        }

        // Check that the toolbar title is persisted
        onView(withId(R.id.toolbar)).check(matches(withToolbarTitle(R.string.add_task)))
    }

    @Test
    fun toolbarTitle_editTask_persistsRotation() {
        val TASK_ID = "1"

        DefaultTasksRepository.destroyInstance()
        FakeTasksRemoteDataSource.addTasks(
                Task("title", "description", TASK_ID).apply { isCompleted = false }
        )

        val intent = Intent(getApplicationContext(),
                AddEditTaskActivity::class.java)
                .apply { putExtra(AddEditTaskFragment.ARGUMENT_EDIT_TASK_ID, TASK_ID) }
        val activityScenario = ActivityScenario.launch<AddEditTaskActivity>(intent)

        // Check that the toolbar shows the correct title
        onView(withId(R.id.toolbar)).check(matches(withToolbarTitle(R.string.edit_task)))

        // Rotate activity
        activityScenario.onActivity {
            it.rotateOrientation()
        }

        // check that the toolbar title is persisted
        onView(withId(R.id.toolbar)).check(matches(withToolbarTitle(R.string.edit_task)))
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

}
