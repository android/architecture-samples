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

import android.view.Gravity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.DrawerActions.open
import androidx.test.espresso.contrib.DrawerMatchers.isClosed
import androidx.test.espresso.contrib.DrawerMatchers.isOpen
import androidx.test.espresso.contrib.NavigationViewActions.navigateTo
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.ServiceLocator
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository
import com.example.android.architecture.blueprints.todoapp.util.DataBindingIdlingResource
import com.example.android.architecture.blueprints.todoapp.util.EspressoIdlingResource
import com.example.android.architecture.blueprints.todoapp.util.monitorActivity
import com.example.android.architecture.blueprints.todoapp.util.saveTaskBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests for the [DrawerLayout] layout component in [TasksActivity] which manages
 * navigation within the app.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class AppNavigationTest {

    private lateinit var tasksRepository: TasksRepository

    // An Idling Resource that waits for Data Binding to have no pending bindings
    private val dataBindingIdlingResource = DataBindingIdlingResource()

    @get:Rule
    val composeTestRule = createAndroidComposeRule<TasksActivity>()
    private val activity by lazy { composeTestRule.activity }

    @Before
    fun init() {
        tasksRepository = ServiceLocator.provideTasksRepository(getApplicationContext())
    }

    @After
    fun reset() {
        ServiceLocator.resetRepository()
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
    fun drawerNavigationFromTasksToStatistics() {
        // start up Tasks screen
        dataBindingIdlingResource.monitorActivity(composeTestRule.activityRule.scenario)

        onView(withId(R.id.drawer_layout))
            .check(matches(isClosed(Gravity.START))) // Left Drawer should be closed.
            .perform(open()) // Open Drawer

        // Start statistics screen.
        onView(withId(R.id.nav_view))
            .perform(navigateTo(R.id.statistics_fragment_dest))

        // Check that statistics screen was opened.
        composeTestRule.onNodeWithText("You have no tasks.").assertIsDisplayed()
        composeTestRule.waitForIdle()

        onView(withId(R.id.drawer_layout))
            .check(matches(isClosed(Gravity.START))) // Left Drawer should be closed.
            .perform(open()) // Open Drawer

        // Start tasks screen.
        onView(withId(R.id.nav_view))
            .perform(navigateTo(R.id.tasks_fragment_dest))

        // Check that tasks screen was opened.
        onView(withId(R.id.tasks_container_layout)).check(matches(isDisplayed()))
    }

    @Test
    fun tasksScreen_clickOnAndroidHomeIcon_OpensNavigation() {
        // start up Tasks screen
        dataBindingIdlingResource.monitorActivity(composeTestRule.activityRule.scenario)

        // Check that left drawer is closed at startup
        onView(withId(R.id.drawer_layout))
            .check(matches(isClosed(Gravity.START))) // Left Drawer should be closed.

        // Open Drawer
        onView(
            withContentDescription(
                composeTestRule.activityRule.scenario.getToolbarNavigationContentDescription()
            )
        ).perform(click())

        // Check if drawer is open
        onView(withId(R.id.drawer_layout))
            .check(matches(isOpen(Gravity.START))) // Left drawer is open open.
    }

    @Test
    fun statsScreen_clickOnAndroidHomeIcon_OpensNavigation() {
        dataBindingIdlingResource.monitorActivity(composeTestRule.activityRule.scenario)

        // When the user navigates to the stats screen
        composeTestRule.activityRule.scenario.onActivity {
            it.findNavController(R.id.nav_host_fragment).navigate(R.id.statistics_fragment_dest)
        }
        composeTestRule.waitForIdle()

        // Then check that left drawer is closed at startup
        onView(withId(R.id.drawer_layout))
            .check(matches(isClosed(Gravity.START))) // Left Drawer should be closed.

        // When the drawer is opened
        onView(
            withContentDescription(
                composeTestRule.activityRule.scenario.getToolbarNavigationContentDescription()
            )
        ).perform(click())

        // Then check that the drawer is open
        onView(withId(R.id.drawer_layout))
            .check(matches(isOpen(Gravity.START))) // Left drawer is open open.
    }

    @Test
    fun taskDetailScreen_doubleUIBackButton() {
        dataBindingIdlingResource.monitorActivity(composeTestRule.activityRule.scenario)

        val task = Task("UI <- button", "Description")
        tasksRepository.saveTaskBlocking(task)
        composeTestRule.waitForIdle()

        // Click on the task on the list
        onView(withText("UI <- button")).perform(click())
        // Click on the edit task button
        composeTestRule.onNodeWithContentDescription(activity.getString(R.string.edit_task))
            .performClick()

        // Confirm that if we click "<-" once, we end up back at the task details page
        onView(
            withContentDescription(
                composeTestRule.activityRule.scenario.getToolbarNavigationContentDescription()
            )
        ).perform(click())
        composeTestRule.onNodeWithText("UI <- button").assertIsDisplayed()

        // Confirm that if we click "<-" a second time, we end up back at the home screen
        onView(
            withContentDescription(
                composeTestRule.activityRule.scenario.getToolbarNavigationContentDescription()
            )
        ).perform(click())
        onView(withId(R.id.tasks_container_layout)).check(matches(isDisplayed()))
    }

    @Test
    fun taskDetailScreen_doubleBackButton() {
        dataBindingIdlingResource.monitorActivity(composeTestRule.activityRule.scenario)

        val task = Task("Back button", "Description")
        tasksRepository.saveTaskBlocking(task)
        composeTestRule.waitForIdle()

        // Click on the task on the list
        onView(withText("Back button")).perform(click())
        // Click on the edit task button
        composeTestRule.onNodeWithContentDescription(activity.getString(R.string.edit_task))
            .performClick()

        // Confirm that if we click back once, we end up back at the task details page
        pressBack()
        composeTestRule.onNodeWithText("Back button").assertIsDisplayed()

        // Confirm that if we click back a second time, we end up back at the home screen
        pressBack()
        onView(withId(R.id.tasks_container_layout)).check(matches(isDisplayed()))
    }
}
