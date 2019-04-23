package com.example.android.architecture.blueprints.todoapp.tasks

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ListView
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.filters.SmallTest
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.ServiceLocator
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository
import com.example.android.architecture.blueprints.todoapp.util.saveTaskBlocking
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import org.hamcrest.core.IsNot.not
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.robolectric.annotation.LooperMode
import org.robolectric.annotation.TextLayoutMode

/**
 * Tests for the main tasks screen that do not involve having to navigate to another screen
 *
 */
@RunWith(AndroidJUnit4::class)
@SmallTest
@LooperMode(LooperMode.Mode.PAUSED)
@TextLayoutMode(TextLayoutMode.Mode.REALISTIC)
class TasksSingleScreenTest {

    private lateinit var repository: TasksRepository

    @Before
    fun setup() {
        repository = ServiceLocator.provideTasksRepository(getApplicationContext())
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
    fun clickAddTaskButton_navigateToAddEditFragment() {
        // GIVEN - On the home screen
        val scenario = launchFragmentInContainer<TasksFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        // WHEN - Click on the "+" button
        onView(withId(R.id.fab_add_task)).perform(click())

        // THEN - Verify that we navigate to the add screen
        verify(navController).navigate(
          TasksFragmentDirections.actionTasksFragmentToAddEditTaskFragment(
            null, getApplicationContext<Context>().getString(R.string.add_task)))
    }

    @Test
    @MediumTest
    fun displayTask_whenRepositoryHasData() {
        // GIVEN - One task already in the repository
        val repository = ServiceLocator.provideTasksRepository(getApplicationContext())
        repository.saveTaskBlocking(Task("title", "description"))

        // WHEN - On startup
        launchFragmentInContainer<TasksFragment>(Bundle(), R.style.AppTheme)

        // THEN - Verify task is displayed on screen
        onView(withItemText("title")).check(matches(isDisplayed()))
    }

    @Test
    @Ignore("blocked on robolectric/4862")
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
    @Ignore("blocked on robolectric/4862")
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
    @Ignore("blocked on robolectric/4862")
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
    @Ignore("blocked on robolectric/4862")
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