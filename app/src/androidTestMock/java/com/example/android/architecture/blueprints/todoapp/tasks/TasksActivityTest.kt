/*
 * Copyright (C) 2022 The Android Open Source Project
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
package com.example.android.architecture.blueprints.todoapp.tasks

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.R.string
import com.example.android.architecture.blueprints.todoapp.ServiceLocator
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository
import com.example.android.architecture.blueprints.todoapp.util.DataBindingIdlingResource
import com.example.android.architecture.blueprints.todoapp.util.EspressoIdlingResource
import com.example.android.architecture.blueprints.todoapp.util.deleteAllTasksBlocking
import com.example.android.architecture.blueprints.todoapp.util.monitorActivity
import com.example.android.architecture.blueprints.todoapp.util.saveTaskBlocking
import org.hamcrest.Matchers.allOf
import org.hamcrest.core.IsNot.not
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Large End-to-End test for the tasks module.
 *
 * UI tests usually use [ActivityTestRule] but there's no API to perform an action before
 * each test. The workaround is to use `ActivityScenario.launch()` and `ActivityScenario.close()`.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class TasksActivityTest {

    private lateinit var repository: TasksRepository

    @get:Rule
    val composeTestRule = createAndroidComposeRule<TasksActivity>()
    private val activity by lazy { composeTestRule.activity }

    // An Idling Resource that waits for Data Binding to have no pending bindings
    private val dataBindingIdlingResource = DataBindingIdlingResource()

    @Before
    fun init() {
        // Run on UI thread to make sure the same instance of the SL is used.
        runOnUiThread {
            ServiceLocator.createDataBase(getApplicationContext(), inMemory = true)
            repository = ServiceLocator.provideTasksRepository(getApplicationContext())
            repository.deleteAllTasksBlocking()
        }
    }

    @After
    fun reset() {
        runOnUiThread {
            ServiceLocator.resetRepository()
        }
    }

    /**
     * Idling resources tell Espresso that the app is idle or busy. This is needed when operations
     * are not scheduled in the main Looper (for example when executed on a different thread).
     */
    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    /**
     * Unregister your Idling Resource so it can be garbage collected and does not leak any memory.
     */
    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }

    @Test
    fun editTask() {
        dataBindingIdlingResource.monitorActivity(composeTestRule.activityRule.scenario)

        repository.saveTaskBlocking(Task("TITLE1", "DESCRIPTION"))
        composeTestRule.waitForIdle()

        // Click on the task on the list and verify that all the data is correct
        onView(withText("TITLE1")).perform(click())
        composeTestRule.onNodeWithText("TITLE1").assertIsDisplayed()
        composeTestRule.onNodeWithText("DESCRIPTION").assertIsDisplayed()
        composeTestRule.onNode(isToggleable()).assertIsOff()

        // Click on the edit button, edit, and save
        composeTestRule.onNodeWithContentDescription(activity.getString(R.string.edit_task))
            .performClick()
        onView(withId(R.id.add_task_title_edit_text)).perform(replaceText("NEW TITLE"))
        onView(withId(R.id.add_task_description_edit_text)).perform(replaceText("NEW DESCRIPTION"))
        onView(withId(R.id.save_task_fab)).perform(click())

        // Verify task is displayed on screen in the task list.
        onView(withText("NEW TITLE")).check(matches(isDisplayed()))
        // Verify previous task is not displayed
        onView(withText("TITLE1")).check(doesNotExist())
    }

    @Test
    fun createOneTask_deleteTask() {
        dataBindingIdlingResource.monitorActivity(composeTestRule.activityRule.scenario)

        // Add active task
        onView(withId(R.id.add_task_fab)).perform(click())
        onView(withId(R.id.add_task_title_edit_text))
            .perform(typeText("TITLE1"), closeSoftKeyboard())
        onView(withId(R.id.add_task_description_edit_text)).perform(typeText("DESCRIPTION"))
        onView(withId(R.id.save_task_fab)).perform(click())

        // Open it in details view
        onView(withText("TITLE1")).perform(click())
        // Click delete task in menu
        onView(withId(R.id.menu_delete)).perform(click())

        // Verify it was deleted
        onView(withId(R.id.menu_filter)).perform(click())
        onView(withText(string.nav_all)).perform(click())
        onView(withText("TITLE1")).check(doesNotExist())
    }

    @Test
    fun createTwoTasks_deleteOneTask() {
        dataBindingIdlingResource.monitorActivity(composeTestRule.activityRule.scenario)

        repository.saveTaskBlocking(Task("TITLE1", "DESCRIPTION"))
        repository.saveTaskBlocking(Task("TITLE2", "DESCRIPTION"))
        composeTestRule.waitForIdle()

        // Open the second task in details view
        onView(withText("TITLE2")).perform(click())
        // Click delete task in menu
        onView(withId(R.id.menu_delete)).perform(click())

        // Verify only one task was deleted
        onView(withId(R.id.menu_filter)).perform(click())
        onView(withText(string.nav_all)).perform(click())
        onView(withText("TITLE1")).check(matches(isDisplayed()))
        onView(withText("TITLE2")).check(doesNotExist())
    }

    @Test
    fun markTaskAsCompleteOnDetailScreen_taskIsCompleteInList() {
        dataBindingIdlingResource.monitorActivity(composeTestRule.activityRule.scenario)

        // Add 1 active task
        val taskTitle = "COMPLETED"
        repository.saveTaskBlocking(Task(taskTitle, "DESCRIPTION"))
        composeTestRule.waitForIdle()

        // Click on the task on the list
        onView(withText(taskTitle)).perform(click())

        // Click on the checkbox in task details screen
        composeTestRule.onNode(isToggleable()).performClick()

        // Click on the navigation up button to go back to the list
        onView(
            withContentDescription(
                composeTestRule.activityRule.scenario.getToolbarNavigationContentDescription()
            )
        ).perform(click())

        // Check that the task is marked as completed
        onView(allOf(withId(R.id.complete_checkbox), hasSibling(withText(taskTitle))))
            .check(matches(isChecked()))
    }

    @Test
    fun markTaskAsActiveOnDetailScreen_taskIsActiveInList() {
        dataBindingIdlingResource.monitorActivity(composeTestRule.activityRule.scenario)

        // Add 1 completed task
        val taskTitle = "ACTIVE"
        repository.saveTaskBlocking(Task(taskTitle, "DESCRIPTION", true))
        composeTestRule.waitForIdle()

        // Click on the task on the list
        onView(withText(taskTitle)).perform(click())
        // Click on the checkbox in task details screen
        composeTestRule.onNode(isToggleable()).performClick()

        // Click on the navigation up button to go back to the list
        onView(
            withContentDescription(
                composeTestRule.activityRule.scenario.getToolbarNavigationContentDescription()
            )
        ).perform(click())

        // Check that the task is marked as active
        onView(allOf(withId(R.id.complete_checkbox), hasSibling(withText(taskTitle))))
            .check(matches(not(isChecked())))
    }

    @Test
    fun markTaskAsCompleteAndActiveOnDetailScreen_taskIsActiveInList() {
        dataBindingIdlingResource.monitorActivity(composeTestRule.activityRule.scenario)

        // Add 1 active task
        val taskTitle = "ACT-COMP"
        repository.saveTaskBlocking(Task(taskTitle, "DESCRIPTION"))
        composeTestRule.waitForIdle()

        // Click on the task on the list
        onView(withText(taskTitle)).perform(click())
        // Click on the checkbox in task details screen
        composeTestRule.onNode(isToggleable()).performClick()
        // Click again to restore it to original state
        composeTestRule.onNode(isToggleable()).performClick()

        // Click on the navigation up button to go back to the list
        onView(
            withContentDescription(
                composeTestRule.activityRule.scenario.getToolbarNavigationContentDescription()
            )
        ).perform(click())

        // Check that the task is marked as active
        onView(allOf(withId(R.id.complete_checkbox), hasSibling(withText(taskTitle))))
            .check(matches(not(isChecked())))
    }

    @Test
    fun markTaskAsActiveAndCompleteOnDetailScreen_taskIsCompleteInList() {
        dataBindingIdlingResource.monitorActivity(composeTestRule.activityRule.scenario)

        // Add 1 completed task
        val taskTitle = "COMP-ACT"
        repository.saveTaskBlocking(Task(taskTitle, "DESCRIPTION", true))
        composeTestRule.waitForIdle()

        // Click on the task on the list
        onView(withText(taskTitle)).perform(click())
        // Click on the checkbox in task details screen
        composeTestRule.onNode(isToggleable()).performClick()
        // Click again to restore it to original state
        composeTestRule.onNode(isToggleable()).performClick()

        // Click on the navigation up button to go back to the list
        onView(
            withContentDescription(
                composeTestRule.activityRule.scenario.getToolbarNavigationContentDescription()
            )
        ).perform(click())

        // Check that the task is marked as active
        onView(allOf(withId(R.id.complete_checkbox), hasSibling(withText(taskTitle))))
            .check(matches(isChecked()))
    }

    @Test
    fun createTask() {
        dataBindingIdlingResource.monitorActivity(composeTestRule.activityRule.scenario)

        // Click on the "+" button, add details, and save
        onView(withId(R.id.add_task_fab)).perform(click())
        onView(withId(R.id.add_task_title_edit_text))
            .perform(typeText("title"), closeSoftKeyboard())
        onView(withId(R.id.add_task_description_edit_text)).perform(typeText("description"))
        onView(withId(R.id.save_task_fab)).perform(click())

        // Then verify task is displayed on screen
        onView(withText("title")).check(matches(isDisplayed()))
    }
}
