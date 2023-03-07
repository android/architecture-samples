/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.architecture.blueprints.todoapp.tasks

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso.pressBack
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.example.android.architecture.blueprints.todoapp.HiltTestActivity
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.TodoNavGraph
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository
import com.google.accompanist.appcompattheme.AppCompatTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import javax.inject.Inject
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests for scenarios that requires navigating within the app.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
@HiltAndroidTest
class AppNavigationTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    // Executes tasks in the Architecture Components in the same thread
    @get:Rule(order = 1)
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule(order = 2)
    val composeTestRule = createAndroidComposeRule<HiltTestActivity>()
    private val activity get() = composeTestRule.activity

    @Inject
    lateinit var tasksRepository: TasksRepository

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun drawerNavigationFromTasksToStatistics() {
        setContent()

        openDrawer()
        // Start statistics screen.
        composeTestRule.onNodeWithText(activity.getString(R.string.statistics_title)).performClick()
        // Check that statistics screen was opened.
        composeTestRule.onNodeWithText(activity.getString(R.string.statistics_no_tasks))
            .assertIsDisplayed()

        openDrawer()
        // Start tasks screen.
        composeTestRule.onNodeWithText(activity.getString(R.string.list_title)).performClick()
        // Check that tasks screen was opened.
        composeTestRule.onNodeWithText(activity.getString(R.string.no_tasks_all))
            .assertIsDisplayed()
    }

    @Test
    fun tasksScreen_clickOnAndroidHomeIcon_OpensNavigation() {
        setContent()

        // Check that left drawer is closed at startup
        composeTestRule.onNodeWithText(activity.getString(R.string.list_title))
            .assertIsNotDisplayed()
        composeTestRule.onNodeWithText(activity.getString(R.string.statistics_title))
            .assertIsNotDisplayed()

        openDrawer()

        // Check if drawer is open
        composeTestRule.onNodeWithText(activity.getString(R.string.list_title)).assertIsDisplayed()
        composeTestRule.onNodeWithText(activity.getString(R.string.statistics_title))
            .assertIsDisplayed()
    }

    @Test
    fun statsScreen_clickOnAndroidHomeIcon_OpensNavigation() {
        setContent()

        // When the user navigates to the stats screen
        openDrawer()
        composeTestRule.onNodeWithText(activity.getString(R.string.statistics_title)).performClick()

        composeTestRule.onNodeWithText(activity.getString(R.string.list_title))
            .assertIsNotDisplayed()

        openDrawer()

        // Check if drawer is open
        composeTestRule.onNodeWithText(activity.getString(R.string.list_title)).assertIsDisplayed()
        assertTrue(
            composeTestRule.onAllNodesWithText(activity.getString(R.string.statistics_title))
                .fetchSemanticsNodes().isNotEmpty()
        )
    }

    @Test
    fun taskDetailScreen_doubleUIBackButton() = runTest {
        val taskName = "UI <- button"
        tasksRepository.createTask(taskName, "Description")

        setContent()

        // Click on the task on the list
        composeTestRule.onNodeWithText(activity.getString(R.string.label_all)).assertIsDisplayed()
        composeTestRule.onNodeWithText(taskName).assertIsDisplayed()
        composeTestRule.onNodeWithText(taskName).performClick()

        // Click on the edit task button
        composeTestRule.onNodeWithContentDescription(activity.getString(R.string.edit_task))
            .assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(activity.getString(R.string.edit_task))
            .performClick()

        // Confirm that if we click "<-" once, we end up back at the task details page
        composeTestRule.onNodeWithContentDescription(activity.getString(R.string.menu_back))
            .performClick()
        composeTestRule.onNodeWithText(taskName).assertIsDisplayed()

        // Confirm that if we click "<-" a second time, we end up back at the home screen
        composeTestRule.onNodeWithContentDescription(activity.getString(R.string.menu_back))
            .performClick()
        composeTestRule.onNodeWithText(activity.getString(R.string.label_all)).assertIsDisplayed()
    }

    @Test
    fun taskDetailScreen_doubleBackButton() = runTest {
        val taskName = "Back button"
        tasksRepository.createTask(taskName, "Description")

        setContent()

        // Click on the task on the list
        composeTestRule.onNodeWithText(taskName).assertIsDisplayed()
        composeTestRule.onNodeWithText(taskName).performClick()
        // Click on the edit task button
        composeTestRule.onNodeWithContentDescription(activity.getString(R.string.edit_task))
            .performClick()

        // Confirm that if we click back once, we end up back at the task details page
        pressBack()
        composeTestRule.onNodeWithText(taskName).assertIsDisplayed()

        // Confirm that if we click back a second time, we end up back at the home screen
        pressBack()
        composeTestRule.onNodeWithText(activity.getString(R.string.label_all)).assertIsDisplayed()
    }

    private fun setContent() {
        composeTestRule.setContent {
            AppCompatTheme {
                TodoNavGraph()
            }
        }
    }

    private fun openDrawer() {
        composeTestRule.onNodeWithContentDescription(activity.getString(R.string.open_drawer))
            .performClick()
    }
}
