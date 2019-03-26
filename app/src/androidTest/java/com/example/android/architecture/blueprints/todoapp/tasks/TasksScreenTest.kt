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

import android.view.View
import android.widget.ListView
import androidx.test.annotation.UiThreadTest
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.filters.SdkSuppress

import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.R.string
import com.example.android.architecture.blueprints.todoapp.TodoApplication
import com.example.android.architecture.blueprints.todoapp.currentActivity
import com.example.android.architecture.blueprints.todoapp.util.rotateOrientation
import com.example.android.architecture.blueprints.todoapp.data.FakeTasksRemoteDataSource
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.util.EspressoIdlingResource
import com.google.common.base.Preconditions.checkArgument
import kotlinx.coroutines.runBlocking
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.TypeSafeMatcher
import org.hamcrest.core.IsNot.not
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests for the tasks screen, the main screen which contains a list of all tasks.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class TasksScreenTest {

    private val TITLE1 = "TITLE1"
    private val TITLE2 = "TITLE2"
    private val TITLE3 = "TITLE3"
    private val DESCRIPTION = "DESCR"

    /**
     * Make sure the tasks repository has no tasks on initialization.
     * The tasks repository needs to be instantiated on the main thread.
     */
    @UiThreadTest
    @Before
    fun resetState() = runBlocking {
        (getApplicationContext() as TodoApplication).taskRepository.deleteAllTasks()
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

    /**
     * A custom [Matcher] which matches an item in a [ListView] by its text.
     *
     *
     * View constraint: View must be a child of a [ListView]
     *
     * @param itemText the text to match
     *
     * @return Matcher that matches text in the given view
     */
    private fun withItemText(itemText: String): Matcher<View> {
        checkArgument(itemText.isNotEmpty(), "itemText cannot be null or empty")
        return object : TypeSafeMatcher<View>() {
            override fun matchesSafely(item: View) = allOf(
                isDescendantOfA(isAssignableFrom(ListView::class.java)),
                withText(itemText)
            ).matches(item)

            override fun describeTo(description: Description) {
                description.appendText("is isDescendantOfA LV with text $itemText")
            }
        }
    }

    @Test
    fun clickAddTaskButton_opensAddTaskUi() {
        val activityScenario = ActivityScenario.launch(TasksActivity::class.java)

        // Click on the add task button
        onView(withId(R.id.fab_add_task)).perform(click())

        // Check if the add task screen is displayed
        onView(withId(R.id.add_task_title)).check(matches(isDisplayed()))
    }

    @Test
    fun editTask() {
        FakeTasksRemoteDataSource.addTasks(Task(TITLE1, DESCRIPTION))
        val activityScenario = ActivityScenario.launch(TasksActivity::class.java)

        // Click on the task on the list
        onView(withText(TITLE1)).perform(click())

        // Click on the edit task button
        onView(withId(R.id.fab_edit_task)).perform(click())

        val editTaskTitle = TITLE2
        val editTaskDescription = "New Description"

        // Edit task title and description
        onView(withId(R.id.add_task_title))
            .perform(replaceText(editTaskTitle), closeSoftKeyboard()) // Type new task title
        onView(withId(R.id.add_task_description)).perform(
            replaceText(editTaskDescription),
            closeSoftKeyboard()
        ) // Type new task description and close the keyboard

        // Save the task
        onView(withId(R.id.fab_edit_task_done)).perform(click())

        // Verify task is displayed on screen in the task list.
        onView(withItemText(editTaskTitle)).check(matches(isDisplayed()))

        // Verify previous task is not displayed
        onView(withItemText(TITLE1)).check(doesNotExist())
    }

    @Test
    fun addTaskToTasksList() {
        FakeTasksRemoteDataSource.addTasks(Task(TITLE1, DESCRIPTION))
        val activityScenario = ActivityScenario.launch(TasksActivity::class.java)

        // Verify task is displayed on screen
        onView(withItemText(TITLE1)).check(matches(isDisplayed()))
    }

    // TODO Move this to TasksSingleScreenTest once #4810 is fixed
    @Test
    fun markTaskAsComplete() {
        FakeTasksRemoteDataSource.addTasks(Task(TITLE1, DESCRIPTION))
        val activityScenario = ActivityScenario.launch(TasksActivity::class.java)

        // Mark the task as complete
        clickCheckBoxForTask(TITLE1)

        // Verify task is shown as complete
        viewAllTasks()
        onView(withItemText(TITLE1)).check(matches(isDisplayed()))
        viewActiveTasks()
        onView(withItemText(TITLE1)).check(matches(not(isDisplayed())))
        viewCompletedTasks()
        onView(withItemText(TITLE1)).check(matches(isDisplayed()))
    }

    // TODO Move this to TasksSingleScreenTest once #4810 is fixed
    @Test
    fun markTaskAsActive() {
        FakeTasksRemoteDataSource.addTasks(Task(TITLE1, DESCRIPTION).apply {
            isCompleted = true
        })
        val activityScenario = ActivityScenario.launch(TasksActivity::class.java)

        // Mark the task as active
        clickCheckBoxForTask(TITLE1)

        // Verify task is shown as active
        viewAllTasks()
        onView(withItemText(TITLE1)).check(matches(isDisplayed()))
        viewActiveTasks()
        onView(withItemText(TITLE1)).check(matches(isDisplayed()))
        viewCompletedTasks()
        onView(withItemText(TITLE1)).check(matches(not(isDisplayed())))
    }

    // TODO Move this to TasksSingleScreenTest once #4810 is fixed
    @Test
    fun showAllTasks() {
        // Add one active task and one completed task
        FakeTasksRemoteDataSource.addTasks(Task(TITLE1, DESCRIPTION))
        FakeTasksRemoteDataSource.addTasks(Task(TITLE2, DESCRIPTION).apply {
            isCompleted = true
        })
        val activityScenario = ActivityScenario.launch(TasksActivity::class.java)

        // Verify that both of our tasks are shown
        viewAllTasks()
        onView(withItemText(TITLE1)).check(matches(isDisplayed()))
        onView(withItemText(TITLE2)).check(matches(isDisplayed()))
    }

    // TODO Move this to TasksSingleScreenTest once #4810 is fixed
    @Test
    fun showActiveTasks() {
        // Add 2 active tasks and one completed task
        FakeTasksRemoteDataSource.addTasks(Task(TITLE1, DESCRIPTION))
        FakeTasksRemoteDataSource.addTasks(Task(TITLE2, DESCRIPTION))
        FakeTasksRemoteDataSource.addTasks(Task(TITLE3, DESCRIPTION).apply {
            isCompleted = true
        })
        val activityScenario = ActivityScenario.launch(TasksActivity::class.java)

        // Verify that the active tasks (but not the completed task) are shown
        viewActiveTasks()
        onView(withItemText(TITLE1)).check(matches(isDisplayed()))
        onView(withItemText(TITLE2)).check(matches(isDisplayed()))
        onView(withItemText(TITLE3)).check(doesNotExist())
    }

    // TODO Move this to TasksSingleScreenTest once #4810 is fixed
    @Test
    fun showCompletedTasks() {
        // Add one active task and 2 completed tasks
        FakeTasksRemoteDataSource.addTasks(Task(TITLE1, DESCRIPTION))
        FakeTasksRemoteDataSource.addTasks(Task(TITLE2, DESCRIPTION).apply {
            isCompleted = true
        })
        FakeTasksRemoteDataSource.addTasks(Task(TITLE3, DESCRIPTION).apply {
            isCompleted = true
        })
        val activityScenario = ActivityScenario.launch(TasksActivity::class.java)

        // Verify that the completed tasks (but not the active task) are shown
        viewCompletedTasks()
        onView(withItemText(TITLE1)).check(doesNotExist())
        onView(withItemText(TITLE2)).check(matches(isDisplayed()))
        onView(withItemText(TITLE3)).check(matches(isDisplayed()))
    }

    // TODO Move this to TasksSingleScreenTest once #4810 is fixed
    @Test
    fun clearCompletedTasks() {
        // Add one active task and one completed task
        FakeTasksRemoteDataSource.addTasks(Task(TITLE1, DESCRIPTION))
        FakeTasksRemoteDataSource.addTasks(Task(TITLE2, DESCRIPTION).apply {
            isCompleted = true
        })
        val activityScenario = ActivityScenario.launch(TasksActivity::class.java)

        viewAllTasks()
        // Click clear completed in menu
        openActionBarOverflowOrOptionsMenu(getApplicationContext())
        onView(withText(string.menu_clear)).perform(click())

        viewAllTasks()
        // Verify that only the active task is shown
        onView(withItemText(TITLE1)).check(matches(isDisplayed()))
        onView(withItemText(TITLE2)).check(doesNotExist())
    }

    @Test
    fun createOneTask_deleteTask() {
        val activityScenario = ActivityScenario.launch(TasksActivity::class.java)
        viewAllTasks()

        // Add active task
        createTask(TITLE1, DESCRIPTION)

        // Open it in details view
        onView(withText(TITLE1)).perform(click())

        // Click delete task in menu
        onView(withId(R.id.menu_delete)).perform(click())

        // Verify it was deleted
        viewAllTasks()
        onView(withText(TITLE1)).check(doesNotExist())
    }

    @Test
    fun createTwoTasks_deleteOneTask() {
        val activityScenario = ActivityScenario.launch(TasksActivity::class.java)
        // Add 2 active tasks
        createTask(TITLE1, DESCRIPTION)
        createTask(TITLE2, DESCRIPTION)

        // Open the second task in details view
        onView(withText(TITLE2)).perform(click())

        // Click delete task in menu
        onView(withId(R.id.menu_delete)).perform(click())

        // Verify only one task was deleted
        viewAllTasks()
        onView(withText(TITLE1)).check(matches(isDisplayed()))
        onView(withText(TITLE2)).check(doesNotExist())
    }

    @Test
    fun markTaskAsCompleteOnDetailScreen_taskIsCompleteInList() {
        // Add 1 active task
        FakeTasksRemoteDataSource.addTasks(Task(TITLE1, DESCRIPTION))
        val activityScenario = ActivityScenario.launch(TasksActivity::class.java)
        viewAllTasks()

        // Click on the task on the list
        onView(withText(TITLE1)).perform(click())

        // Click on the checkbox in task details screen
        onView(withId(R.id.task_detail_complete)).perform(click())

        // Press back button to go back to the list
        Espresso.pressBack();

        // Check that the task is marked as completed
        onView(allOf(withId(R.id.complete), hasSibling(withText(TITLE1))))
            .check(matches(isChecked()))
    }

    @Test
    fun markTaskAsActiveOnDetailScreen_taskIsActiveInList() {
        // Add 1 completed task
        FakeTasksRemoteDataSource.addTasks(Task(TITLE1, DESCRIPTION).apply {
            isCompleted = true
        })
        val activityScenario = ActivityScenario.launch(TasksActivity::class.java)
        viewAllTasks()

        // Click on the task on the list
        onView(withText(TITLE1)).perform(click())

        // Click on the checkbox in task details screen
        onView(withId(R.id.task_detail_complete)).perform(click())

        // Press back button to go back to the list
        Espresso.pressBack();

        // Check that the task is marked as active
        onView(allOf(withId(R.id.complete), hasSibling(withText(TITLE1))))
            .check(matches(not(isChecked())))
    }

    @Test
    fun markTaskAsCompleteAndActiveOnDetailScreen_taskIsActiveInList() {
        // Add 1 active task
        FakeTasksRemoteDataSource.addTasks(Task(TITLE1, DESCRIPTION))
        val activityScenario = ActivityScenario.launch(TasksActivity::class.java)
        viewAllTasks()

        // Click on the task on the list
        onView(withText(TITLE1)).perform(click())

        // Click on the checkbox in task details screen
        onView(withId(R.id.task_detail_complete)).perform(click())

        // Click again to restore it to original state
        onView(withId(R.id.task_detail_complete)).perform(click())

        // Press back button to go back to the list
        Espresso.pressBack();

        // Check that the task is marked as active
        onView(allOf(withId(R.id.complete), hasSibling(withText(TITLE1))))
            .check(matches(not(isChecked())))
    }

    @Test
    fun markTaskAsActiveAndCompleteOnDetailScreen_taskIsCompleteInList() {
        // Add 1 completed task
        FakeTasksRemoteDataSource.addTasks(Task(TITLE1, DESCRIPTION).apply {
            isCompleted = true
        })
        val activityScenario = ActivityScenario.launch(TasksActivity::class.java)
        viewAllTasks()

        // Click on the task on the list
        onView(withText(TITLE1)).perform(click())

        // Click on the checkbox in task details screen
        onView(withId(R.id.task_detail_complete)).perform(click())

        // Click again to restore it to original state
        onView(withId(R.id.task_detail_complete)).perform(click())

        // Press back button to go back to the list
        Espresso.pressBack();

        // Check that the task is marked as active
        onView(allOf(withId(R.id.complete), hasSibling(withText(TITLE1))))
            .check(matches(isChecked()))
    }

    // TODO Move this to TasksSingleScreenTest once #4810 is fixed
    @Test
    fun orientationChange_FilterActivePersists() {
        // Add 1 completed task
        FakeTasksRemoteDataSource.addTasks(Task(TITLE1, DESCRIPTION).apply {
            isCompleted = true
        })
        val activityScenario = ActivityScenario.launch(TasksActivity::class.java)

        // when switching to active tasks
        viewActiveTasks()

        // then no tasks should appear
        onView(withText(TITLE1)).check(matches(not(isDisplayed())))

        // when rotating the screen
        activityScenario.onActivity {
            it.rotateOrientation()
        }

        // then nothing changes
        onView(withText(TITLE1)).check(doesNotExist())
    }

    // TODO Move this to TasksSingleScreenTest once #4810 is fixed
    @Test
    fun orientationChange_FilterCompletedPersists() {
        // Add 1 completed task
        FakeTasksRemoteDataSource.addTasks(Task(TITLE1, DESCRIPTION).apply {
            isCompleted = true
        })
        val activityScenario = ActivityScenario.launch(TasksActivity::class.java)

        // when switching to completed tasks
        viewCompletedTasks()

        // the completed task should be displayed
        onView(withText(TITLE1)).check(matches(isDisplayed()))

        // when rotating the screen
        activityScenario.onActivity {
            it.rotateOrientation()
        }

        // then nothing changes
        onView(withText(TITLE1)).check(matches(isDisplayed()))
        onView(withText(string.label_completed)).check(matches(isDisplayed()))
    }

    // Blinking cursor after rotation breaks this in API 19
    @Test
    @SdkSuppress(minSdkVersion = 21)
    fun orientationChange_DuringEdit_ChangePersists() {
        // Add 1 completed task
        FakeTasksRemoteDataSource.addTasks(Task(TITLE1, DESCRIPTION).apply {
            isCompleted = true
        })
        val activityScenario = ActivityScenario.launch(TasksActivity::class.java)
        viewAllTasks()

        // Open the task in details view
        onView(withText(TITLE1)).perform(click())

        // Click on the edit task button
        onView(withId(R.id.fab_edit_task)).perform(click())

        // Change task title (but don't save)
        onView(withId(R.id.add_task_title))
            .perform(replaceText(TITLE2), closeSoftKeyboard()) // Type new task title

        // Rotate the screen
        currentActivity.rotateOrientation()

        // Verify task title is restored
        onView(withId(R.id.add_task_title)).check(matches(withText(TITLE2)))
    }

    // Blinking cursor after rotation breaks this in API 19
    @Test
    @SdkSuppress(minSdkVersion = 21)
    fun orientationChange_DuringEdit_NoDuplicate() {
        // Add 1 completed task
        FakeTasksRemoteDataSource.addTasks(Task(TITLE1, DESCRIPTION).apply {
            isCompleted = true
        })
        val activityScenario = ActivityScenario.launch(TasksActivity::class.java)

        // Open the task in details view
        onView(withText(TITLE1)).perform(click())

        // Click on the edit task button
        onView(withId(R.id.fab_edit_task)).perform(click())

        // when rotating the screen
        activityScenario.onActivity {
            it.rotateOrientation()
        }

        // Edit task title and description
        onView(withId(R.id.add_task_title))
            .perform(replaceText(TITLE2), closeSoftKeyboard()) // Type new task title
        onView(withId(R.id.add_task_description)).perform(
            replaceText(DESCRIPTION),
            closeSoftKeyboard()
        ) // Type new task description and close the keyboard

        // Save the task
        onView(withId(R.id.fab_edit_task_done)).perform(click())

        // Verify task is displayed on screen in the task list.
        onView(withItemText(TITLE2)).check(matches(isDisplayed()))

        // Verify previous task is not displayed
        onView(withItemText(TITLE1)).check(doesNotExist())
    }

    // TODO Move this to TasksSingleScreenTest once #4810 is fixed
    @Test
    fun noTasks_AllTasksFilter_AddTaskViewVisible() {
        val activityScenario = ActivityScenario.launch(TasksActivity::class.java)
        // Given an empty list of tasks, make sure "All tasks" filter is on
        viewAllTasks()

        // Add task View should be displayed
        onView(withId(R.id.noTasksAdd)).check(matches(isDisplayed()))
    }

    // TODO Move this to TasksSingleScreenTest once #4810 is fixed
    @Test
    fun noTasks_CompletedTasksFilter_AddTaskViewNotVisible() {
        val activityScenario = ActivityScenario.launch(TasksActivity::class.java)
        // Given an empty list of tasks, make sure "All tasks" filter is on
        viewCompletedTasks()

        // Add task View should be not be displayed
        onView(withId(R.id.noTasksAdd)).check(matches(not(isDisplayed())))
    }

    // TODO Move this to TasksSingleScreenTest once #4810 is fixed
    @Test
    fun noTasks_ActiveTasksFilter_AddTaskViewNotVisible() {
        val activityScenario = ActivityScenario.launch(TasksActivity::class.java)
        // Given an empty list of tasks, make sure "All tasks" filter is on
        viewActiveTasks()

        // Add task View should be not be displayed
        onView(withId(R.id.noTasksAdd)).check(matches(not(isDisplayed())))
    }

    private fun viewAllTasks() {
        onView(withId(R.id.menu_filter)).perform(click())
        onView(withText(string.nav_all)).perform(click())
    }

    private fun viewActiveTasks() {
        onView(withId(R.id.menu_filter)).perform(click())
        onView(withText(string.nav_active)).perform(click())
    }

    private fun viewCompletedTasks() {
        onView(withId(R.id.menu_filter)).perform(click())
        onView(withText(string.nav_completed)).perform(click())
    }

    private fun createTask(title: String, description: String) {
        // Click on the add task button
        onView(withId(R.id.fab_add_task)).perform(click())

        // Add task title and description
        onView(withId(R.id.add_task_title)).perform(
            typeText(title),
            closeSoftKeyboard()
        ) // Type new task title
        onView(withId(R.id.add_task_description)).perform(
            typeText(description),
            closeSoftKeyboard()
        ) // Type new task description and close the keyboard

        // Save the task
        onView(withId(R.id.fab_edit_task_done)).perform(click())
    }

    private fun clickCheckBoxForTask(title: String) {
        onView(allOf(withId(R.id.complete), hasSibling(withText(title)))).perform(click())
    }
}
