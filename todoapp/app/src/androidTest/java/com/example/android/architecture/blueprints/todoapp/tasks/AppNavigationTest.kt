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
package com.example.android.architecture.blueprints.todoapp.tasks

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.contrib.DrawerActions.open
import android.support.test.espresso.contrib.DrawerMatchers.isClosed
import android.support.test.espresso.contrib.DrawerMatchers.isOpen
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.matcher.ViewMatchers.withContentDescription
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.filters.LargeTest
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.support.v4.widget.DrawerLayout
import android.view.Gravity
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.custom.action.NavigationViewActions.navigateTo
import com.example.android.architecture.blueprints.todoapp.getToolbarNavigationContentDescription
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests for the [DrawerLayout] layout component in [TasksActivity] which manages
 * navigation within the app.
 */
@RunWith(AndroidJUnit4::class) @LargeTest class AppNavigationTest {

    /**
     * [ActivityTestRule] is a JUnit [@Rule][Rule] to launch your activity under test.

     *
     *
     * Rules are interceptors which are executed for each test method and are important building
     * blocks of Junit tests.
     */
    @get:Rule var activityTestRule = ActivityTestRule(TasksActivity::class.java)

    @Test fun clickOnStatisticsNavigationItem_ShowsStatisticsScreen() {
        // Open Drawer to click on navigation.
        onView(withId(R.id.drawer_layout))
                .check(matches(isClosed(Gravity.START))) // Left Drawer should be closed.
                .perform(open()) // Open Drawer

        // Start statistics screen.
        onView(withId(R.id.nav_view)).perform(navigateTo(R.id.statistics_navigation_menu_item))

        // Check that statistics Activity was opened.
        onView(withId(R.id.statistics)).check(matches(isDisplayed()))
    }

    @Test fun clickOnListNavigationItem_ShowsListScreen() {
        // Open Drawer to click on navigation.
        onView(withId(R.id.drawer_layout))
                .check(matches(isClosed(Gravity.START))) // Left Drawer should be closed.
                .perform(open()) // Open Drawer

        // Start statistics screen.
        onView(withId(R.id.nav_view))
                .perform(navigateTo(R.id.statistics_navigation_menu_item))

        // Open Drawer to click on navigation.
        onView(withId(R.id.drawer_layout))
                .check(matches(isClosed(Gravity.START))) // Left Drawer should be closed.
                .perform(open()) // Open Drawer

        // Start tasks list screen.
        onView(withId(R.id.nav_view))
                .perform(navigateTo(R.id.list_navigation_menu_item))

        // Check that Tasks Activity was opened.
        onView(withId(R.id.tasksContainer)).check(matches(isDisplayed()))
    }

    @Test fun clickOnAndroidHomeIcon_OpensNavigation() {
        // Check that left drawer is closed at startup
        onView(withId(R.id.drawer_layout))
                .check(matches(isClosed(Gravity.START))) // Left Drawer should be closed.

        // Open Drawer
        onView(withContentDescription(activityTestRule.activity
                .getToolbarNavigationContentDescription(R.id.toolbar))).perform(click())

        // Check if drawer is open
        onView(withId(R.id.drawer_layout))
                .check(matches(isOpen(Gravity.START))) // Left drawer is open open.
    }
}
