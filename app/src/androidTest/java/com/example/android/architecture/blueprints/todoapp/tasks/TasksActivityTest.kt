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

import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.R.string
import com.example.android.architecture.blueprints.todoapp.ServiceLocator
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository
import com.example.android.architecture.blueprints.todoapp.util.EspressoIdlingResource
import com.example.android.architecture.blueprints.todoapp.util.saveTaskBlocking
import org.hamcrest.Matchers.allOf
import org.hamcrest.core.IsNot.not
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Large End-to-End test for the tasks module.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class TasksActivityTest {

    private lateinit var repository: TasksRepository

    @Before
    fun resetState() {
        repository = ServiceLocator.provideTasksRepository(getApplicationContext())
    }

    @After
    fun reset() {
        ServiceLocator.resetForTests()
    }

    /**
     * Prepare your test fixture for this test. In this case we register an IdlingResources with
     * Espresso. IdlingResource resource is a great way to tell Espresso when your app is in an
     * idle state. This helps Espresso to synchronize your test actions, which makes tests
     * significantly more reliable.
     */
    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
    }

    /**
     * Unregister your Idling Resource so it can be garbage collected and does not leak any memory.
     */
    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
    }

    @Test
    fun createTask() {
        // GIVEN - Start on home screen
        ActivityScenario.launch(TasksActivity::class.java)

        // WHEN - Click on the "+" button, add details, and save
        onView(withId(R.id.fab_add_task)).perform(click())
        onView(withId(R.id.add_task_title))
          .perform(typeText("title"))
        onView(withId(R.id.add_task_description))
          .perform(typeText("description"))
        onView(withId(R.id.fab_save_task)).perform(click())

        // THEN - Verify task is displayed on screen
        onView(withText("title")).check(matches(isDisplayed()))
    }

    @Test
    fun editTask() {
        repository.saveTaskBlocking(Task("TITLE1", "DESCRIPTION"))

        val activityScenario = ActivityScenario.launch(TasksActivity::class.java)

        // Click on the task on the list
        onView(withText("TITLE1")).perform(click())

        // Click on the edit task button
        onView(withId(R.id.fab_edit_task)).perform(click())

        // Edit task title and description
        onView(withId(R.id.add_task_title)).perform(replaceText("NEW TITLE"))
        onView(withId(R.id.add_task_description)).perform(replaceText("NEW DESCRIPTION"))

        // Save the task
        onView(withId(R.id.fab_save_task)).perform(click())

        // Verify task is displayed on screen in the task list.
        onView(withText("NEW TITLE")).check(matches(isDisplayed()))

        // Verify previous task is not displayed
        onView(withText("TITLE1")).check(doesNotExist())
    }

    @Test
    fun createOneTask_deleteTask() {
        val activityScenario = ActivityScenario.launch(TasksActivity::class.java)

        // Add active task
        onView(withId(R.id.fab_add_task)).perform(click())
        onView(withId(R.id.add_task_title)).perform(typeText("TITLE1"))
        onView(withId(R.id.add_task_description)).perform(typeText("DESCRIPTION"))
        onView(withId(R.id.fab_save_task)).perform(click())

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
        val activityScenario = ActivityScenario.launch(TasksActivity::class.java)

        // Add 2 active tasks
        onView(withId(R.id.fab_add_task)).perform(click())
        onView(withId(R.id.add_task_title)).perform(typeText("TITLE1"))
        onView(withId(R.id.add_task_description)).perform(typeText("DESCRIPTION"))
        onView(withId(R.id.fab_save_task)).perform(click())

        onView(withId(R.id.fab_add_task)).perform(click())
        onView(withId(R.id.add_task_title)).perform(typeText("TITLE2"))
        onView(withId(R.id.add_task_description)).perform(typeText("DESCRIPTION"))
        onView(withId(R.id.fab_save_task)).perform(click())

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
        // Add 1 active task
        repository.saveTaskBlocking(Task("TITLE1", "DESCRIPTION"))

        val activityScenario = ActivityScenario.launch(TasksActivity::class.java)

        // Click on the task on the list
        onView(withText("TITLE1")).perform(click())

        // Click on the checkbox in task details screen
        onView(withId(R.id.task_detail_complete)).perform(click())

        // Press back button to go back to the list
        pressBack()

        // Check that the task is marked as completed
        onView(allOf(withId(R.id.complete), hasSibling(withText("TITLE1"))))
            .check(matches(isChecked()))
    }

    @Test
    fun markTaskAsActiveOnDetailScreen_taskIsActiveInList() {
        // Add 1 completed task
        repository.saveTaskBlocking(Task("TITLE1", "DESCRIPTION", true))

        val activityScenario = ActivityScenario.launch(TasksActivity::class.java)

        // Click on the task on the list
        onView(withText("TITLE1")).perform(click())

        // Click on the checkbox in task details screen
        onView(withId(R.id.task_detail_complete)).perform(click())

        // Press back button to go back to the list
        pressBack()

        // Check that the task is marked as active
        onView(allOf(withId(R.id.complete), hasSibling(withText("TITLE1"))))
            .check(matches(not(isChecked())))
    }

    @Test
    fun markTaskAsCompleteAndActiveOnDetailScreen_taskIsActiveInList() {
        // Add 1 active task
        repository.saveTaskBlocking(Task("TITLE1", "DESCRIPTION"))

        val activityScenario = ActivityScenario.launch(TasksActivity::class.java)

        // Click on the task on the list
        onView(withText("TITLE1")).perform(click())

        // Click on the checkbox in task details screen
        onView(withId(R.id.task_detail_complete)).perform(click())

        // Click again to restore it to original state
        onView(withId(R.id.task_detail_complete)).perform(click())

        // Press back button to go back to the list
        pressBack()

        // Check that the task is marked as active
        onView(allOf(withId(R.id.complete), hasSibling(withText("TITLE1"))))
            .check(matches(not(isChecked())))
    }

    @Test
    fun markTaskAsActiveAndCompleteOnDetailScreen_taskIsCompleteInList() {
        // Add 1 completed task
        repository.saveTaskBlocking(Task("TITLE1", "DESCRIPTION", true))

        val activityScenario = ActivityScenario.launch(TasksActivity::class.java)

        // Click on the task on the list
        onView(withText("TITLE1")).perform(click())

        // Click on the checkbox in task details screen
        onView(withId(R.id.task_detail_complete)).perform(click())

        // Click again to restore it to original state
        onView(withId(R.id.task_detail_complete)).perform(click())

        // Press back button to go back to the list
        pressBack()

        // Check that the task is marked as active
        onView(allOf(withId(R.id.complete), hasSibling(withText("TITLE1"))))
            .check(matches(isChecked()))
    }
}
