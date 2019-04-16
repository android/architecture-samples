package com.example.android.architecture.blueprints.todoapp.tasks

import android.app.Service
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.ServiceLocator
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository
import com.example.android.architecture.blueprints.todoapp.util.saveTaskBlocking
import kotlinx.coroutines.runBlocking
import org.hamcrest.core.IsNot.not
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.LooperMode
import org.robolectric.annotation.TextLayoutMode

/**
 * Tests for the main tasks screen that do not involve having to navigate to another screen
 *
 */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@TextLayoutMode(TextLayoutMode.Mode.REALISTIC)
@Ignore("blocked on robolectric/4862")
class TasksSingleScreenTest {

    private lateinit var repository: TasksRepository

    @Before
    fun setup() {
        repository = ServiceLocator.provideTasksRepository(getApplicationContext())
    }

    @Test
    fun displayActiveTask() {
        repository.saveTaskBlocking(Task(TITLE1, DESCRIPTION1))

        val activityScenario = ActivityScenario.launch(TasksActivity::class.java)

        onView(withText(TITLE1)).check(matches(isDisplayed()))

        viewActiveTasks()
        onView(withText(TITLE1)).check(matches(isDisplayed()))

        viewCompletedTasks()
        onView(withText(TITLE1)).check(matches(not(isDisplayed())))
    }

    @Test
    fun displayCompletedTask() {
        repository.saveTaskBlocking(Task(TITLE1, DESCRIPTION1, true))

        val activityScenario = ActivityScenario.launch(TasksActivity::class.java)

        onView(withText(TITLE1)).check(matches(isDisplayed()))

        viewActiveTasks()
        onView(withText(TITLE1)).check(matches(not(isDisplayed())))

        viewCompletedTasks()
        onView(withText(TITLE1)).check(matches(isDisplayed()))
    }

    @Test
    fun deleteOneTest() {
        repository.saveTaskBlocking(Task(TITLE1, DESCRIPTION1))

        val activityScenario = ActivityScenario.launch(TasksActivity::class.java)

        // Open it in details view
        onView(withText(TITLE1)).perform(ViewActions.click())

        // Click delete task in menu
        onView(withId(R.id.menu_delete)).perform(ViewActions.click())

        // Verify it was deleted
        viewAllTasks()
        onView(withText(TITLE1)).check(matches(not(isDisplayed())))
    }

    @Test
    fun deleteOneOfTwoTests() {
        repository.saveTaskBlocking(Task(TITLE1, DESCRIPTION1))
        repository.saveTaskBlocking(Task(TITLE2, DESCRIPTION2))

        val activityScenario = ActivityScenario.launch(TasksActivity::class.java)

        // Open it in details view
        onView(withText(TITLE1)).perform(ViewActions.click())

        // Click delete task in menu
        onView(withId(R.id.menu_delete)).perform(ViewActions.click())

        // Verify it was deleted
        viewAllTasks()
        onView(withText(TITLE1)).check(matches(not(isDisplayed())))
        // but not the other one
        onView(withText(TITLE2)).check(matches(isDisplayed()))
    }

    private fun viewAllTasks() {
        onView(ViewMatchers.withId(R.id.menu_filter)).perform(ViewActions.click())
        onView(withText(R.string.nav_all)).perform(ViewActions.click())
    }

    private fun viewActiveTasks() {
        onView(ViewMatchers.withId(R.id.menu_filter)).perform(ViewActions.click())
        onView(withText(R.string.nav_active)).perform(ViewActions.click())
    }

    private fun viewCompletedTasks() {
        onView(ViewMatchers.withId(R.id.menu_filter)).perform(ViewActions.click())
        onView(withText(R.string.nav_completed)).perform(ViewActions.click())
    }

    companion object {

        const val TITLE1 = "TITLE1"
        const val TITLE2 = "TITLE2"

        const val DESCRIPTION1 = "DESCRIPTION1"
        const val DESCRIPTION2 = "DESCRIPTION2"
    }
}