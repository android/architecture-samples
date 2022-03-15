/*
 * Copyright (C) 2019 The Android Open Source Project
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
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.Navigation.findNavController
import androidx.navigation.findNavController
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.ServiceLocator
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.FakeRepository
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository
import com.example.android.architecture.blueprints.todoapp.util.saveTaskBlocking
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.LooperMode
import org.robolectric.annotation.TextLayoutMode

/**
 * Integration test for the Task List screen.
 */
// TODO - Use FragmentScenario, see: https://github.com/android/android-test/issues/291
@RunWith(AndroidJUnit4::class)
@MediumTest
@LooperMode(LooperMode.Mode.PAUSED)
@TextLayoutMode(TextLayoutMode.Mode.REALISTIC)
@ExperimentalCoroutinesApi
class TasksScreenTest {

    private lateinit var repository: TasksRepository

    @get:Rule
    val composeTestRule = createAndroidComposeRule<TasksActivity>()
    private val activity get() = composeTestRule.activity

    @Before
    fun initRepository() {
        repository = FakeRepository()
        ServiceLocator.tasksRepository = repository
    }

    @After
    fun cleanupDb() = runBlockingTest {
        ServiceLocator.resetRepository()
    }

    @Test
    fun displayTask_whenRepositoryHasData() {
        // GIVEN - One task already in the repository
        repository.saveTaskBlocking(Task("TITLE1", "DESCRIPTION1"))

        // WHEN - On startup
        launchScreen()

        // THEN - Verify task is displayed on screen
        composeTestRule.onNodeWithText("TITLE1").assertIsDisplayed()
    }

    @Test
    fun displayActiveTask() {
        repository.saveTaskBlocking(Task("TITLE1", "DESCRIPTION1"))

        launchScreen()

        composeTestRule.onNodeWithText("TITLE1").assertIsDisplayed()

        onView(withId(R.id.menu_filter)).perform(click())
        onView(withText(R.string.nav_active)).perform(click())
        composeTestRule.onNodeWithText("TITLE1").assertIsDisplayed()

        onView(withId(R.id.menu_filter)).perform(click())
        onView(withText(R.string.nav_completed)).perform(click())
        composeTestRule.onNodeWithText("TITLE1").assertDoesNotExist()
    }

    @Test
    fun displayCompletedTask() {
        repository.saveTaskBlocking(Task("TITLE1", "DESCRIPTION1", true))

        launchScreen()

        composeTestRule.onNodeWithText("TITLE1").assertIsDisplayed()

        onView(withId(R.id.menu_filter)).perform(click())
        onView(withText(R.string.nav_active)).perform(click())
        composeTestRule.onNodeWithText("TITLE1").assertDoesNotExist()

        onView(withId(R.id.menu_filter)).perform(click())
        onView(withText(R.string.nav_completed)).perform(click())

        composeTestRule.onNodeWithText("TITLE1").assertIsDisplayed()
    }

    @Test
    fun deleteOneTask() {
        repository.saveTaskBlocking(Task("TITLE1", "DESCRIPTION1"))

        launchScreen()

        // Open it in details view
        composeTestRule.onNodeWithText("TITLE1").performClick()

        // Click delete task in menu
        onView(withId(R.id.menu_delete)).perform(click())

        // Verify it was deleted
        onView(withId(R.id.menu_filter)).perform(click())
        onView(withText(R.string.nav_all)).perform(click())
        composeTestRule.onNodeWithText("TITLE1").assertDoesNotExist()
    }

    @Test
    fun deleteOneOfTwoTasks() {
        repository.saveTaskBlocking(Task("TITLE1", "DESCRIPTION1"))
        repository.saveTaskBlocking(Task("TITLE2", "DESCRIPTION2"))

        launchScreen()

        // Open it in details view
        composeTestRule.onNodeWithText("TITLE1").performClick()

        // Click delete task in menu
        onView(withId(R.id.menu_delete)).perform(click())

        // Verify it was deleted
        onView(withId(R.id.menu_filter)).perform(click())
        onView(withText(R.string.nav_all)).perform(click())
        composeTestRule.onNodeWithText("TITLE1").assertDoesNotExist()
        // but not the other one
        composeTestRule.onNodeWithText("TITLE2").assertIsDisplayed()
    }

    @Test
    fun markTaskAsComplete() {
        repository.saveTaskBlocking(Task("TITLE1", "DESCRIPTION1"))

        launchScreen()

        // Mark the task as complete
        composeTestRule.onNode(isToggleable()).performClick()

        // Verify task is shown as complete
        onView(withId(R.id.menu_filter)).perform(click())
        onView(withText(R.string.nav_all)).perform(click())
        composeTestRule.onNodeWithText("TITLE1").assertIsDisplayed()
        onView(withId(R.id.menu_filter)).perform(click())
        onView(withText(R.string.nav_active)).perform(click())
        composeTestRule.onNodeWithText("TITLE1").assertDoesNotExist()
        onView(withId(R.id.menu_filter)).perform(click())
        onView(withText(R.string.nav_completed)).perform(click())
        composeTestRule.onNodeWithText("TITLE1").assertIsDisplayed()
    }

    @Test
    fun markTaskAsActive() {
        repository.saveTaskBlocking(Task("TITLE1", "DESCRIPTION1", true))

        launchScreen()

        // Mark the task as active
        composeTestRule.onNode(isToggleable()).performClick()

        // Verify task is shown as active
        onView(withId(R.id.menu_filter)).perform(click())
        onView(withText(R.string.nav_all)).perform(click())
        composeTestRule.onNodeWithText("TITLE1").assertIsDisplayed()
        onView(withId(R.id.menu_filter)).perform(click())
        onView(withText(R.string.nav_active)).perform(click())
        composeTestRule.onNodeWithText("TITLE1").assertIsDisplayed()
        onView(withId(R.id.menu_filter)).perform(click())
        onView(withText(R.string.nav_completed)).perform(click())
        composeTestRule.onNodeWithText("TITLE1").assertDoesNotExist()
    }

    @Test
    fun showAllTasks() {
        // Add one active task and one completed task
        repository.saveTaskBlocking(Task("TITLE1", "DESCRIPTION1"))
        repository.saveTaskBlocking(Task("TITLE2", "DESCRIPTION2", true))

        launchScreen()

        // Verify that both of our tasks are shown
        onView(withId(R.id.menu_filter)).perform(click())
        onView(withText(R.string.nav_all)).perform(click())
        composeTestRule.onNodeWithText("TITLE1").assertIsDisplayed()
        composeTestRule.onNodeWithText("TITLE2").assertIsDisplayed()
    }

    @Test
    fun showActiveTasks() {
        // Add 2 active tasks and one completed task
        repository.saveTaskBlocking(Task("TITLE1", "DESCRIPTION1"))
        repository.saveTaskBlocking(Task("TITLE2", "DESCRIPTION2"))
        repository.saveTaskBlocking(Task("TITLE3", "DESCRIPTION3", true))

        launchScreen()

        // Verify that the active tasks (but not the completed task) are shown
        onView(withId(R.id.menu_filter)).perform(click())
        onView(withText(R.string.nav_active)).perform(click())
        composeTestRule.onNodeWithText("TITLE1").assertIsDisplayed()
        composeTestRule.onNodeWithText("TITLE2").assertIsDisplayed()
        composeTestRule.onNodeWithText("TITLE3").assertDoesNotExist()
    }

    @Test
    fun showCompletedTasks() {
        // Add one active task and 2 completed tasks
        repository.saveTaskBlocking(Task("TITLE1", "DESCRIPTION1"))
        repository.saveTaskBlocking(Task("TITLE2", "DESCRIPTION2", true))
        repository.saveTaskBlocking(Task("TITLE3", "DESCRIPTION3", true))

        launchScreen()

        // Verify that the completed tasks (but not the active task) are shown
        onView(withId(R.id.menu_filter)).perform(click())
        onView(withText(R.string.nav_completed)).perform(click())
        composeTestRule.onNodeWithText("TITLE1").assertDoesNotExist()
        composeTestRule.onNodeWithText("TITLE2").assertIsDisplayed()
        composeTestRule.onNodeWithText("TITLE3").assertIsDisplayed()
    }

    @Test
    fun clearCompletedTasks() {
        // Add one active task and one completed task
        repository.saveTaskBlocking(Task("TITLE1", "DESCRIPTION1"))
        repository.saveTaskBlocking(Task("TITLE2", "DESCRIPTION2", true))

        launchScreen()

        // Click clear completed in menu
        openActionBarOverflowOrOptionsMenu(getApplicationContext())
        onView(withText(R.string.menu_clear)).perform(click())

        onView(withId(R.id.menu_filter)).perform(click())
        onView(withText(R.string.nav_all)).perform(click())
        // Verify that only the active task is shown
        composeTestRule.onNodeWithText("TITLE1").assertIsDisplayed()
        composeTestRule.onNodeWithText("TITLE2").assertDoesNotExist()
    }

    @Test
    fun noTasks_AllTasksFilter_AddTaskViewVisible() {
        launchScreen()

        onView(withId(R.id.menu_filter)).perform(click())
        onView(withText(R.string.nav_all)).perform(click())

        // Verify the "You have no tasks!" text is shown
        composeTestRule.onNodeWithText("You have no tasks!").assertIsDisplayed()
    }

    @Test
    fun noTasks_CompletedTasksFilter_AddTaskViewNotVisible() {
        launchScreen()

        onView(withId(R.id.menu_filter)).perform(click())
        onView(withText(R.string.nav_completed)).perform(click())

        // Verify the "You have no completed tasks!" text is shown
        composeTestRule.onNodeWithText("You have no completed tasks!").assertIsDisplayed()
    }

    @Test
    fun noTasks_ActiveTasksFilter_AddTaskViewNotVisible() {
        launchScreen()

        onView(withId(R.id.menu_filter)).perform(click())
        onView(withText(R.string.nav_active)).perform(click())

        // Verify the "You have no active tasks!" text is shown
        composeTestRule.onNodeWithText("You have no active tasks!").assertIsDisplayed()
    }

    @Test
    fun clickAddTaskButton_navigateToAddEditFragment() {
        // GIVEN - On the home screen
        launchScreen()

        // WHEN - Click on the "+" button
        composeTestRule.onNodeWithContentDescription(activity.getString(R.string.add_task))
            .performClick()

        // THEN - Verify that we navigate to the add screen
        assertEquals(
            activity.findNavController(R.id.nav_host_fragment).currentDestination?.id,
            R.id.add_edit_task_fragment_dest
        )
    }

    private fun launchScreen() {
        composeTestRule.activityRule.scenario.onActivity {
            findNavController(it, R.id.nav_host_fragment).apply {
                setGraph(R.navigation.nav_graph)
            }
        }
    }
}
