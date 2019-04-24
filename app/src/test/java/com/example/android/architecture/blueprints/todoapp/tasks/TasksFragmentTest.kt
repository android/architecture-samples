package com.example.android.architecture.blueprints.todoapp.tasks

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.ServiceLocator
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository
import com.example.android.architecture.blueprints.todoapp.util.deleteAllTasksBlocking
import com.example.android.architecture.blueprints.todoapp.util.saveTaskBlocking
import org.hamcrest.core.IsNot.not
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
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
class TasksFragmentTest {

    private lateinit var repository: TasksRepository

    @Before
    fun setup() {
        repository = ServiceLocator.provideTasksRepository(getApplicationContext())
    }

    @After
    fun tearDown() {
        repository.deleteAllTasksBlocking()
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
        onView(withText("title")).check(matches(isDisplayed()))
    }

    @Test
    fun displayActiveTask() {
        repository.saveTaskBlocking(Task("TITLE1", "DESCRIPTION1"))

        ActivityScenario.launch(TasksActivity::class.java)

        onView(withText("TITLE1")).check(matches(isDisplayed()))

        onView(withId(R.id.menu_filter)).perform(click())
        onView(withText(R.string.nav_active)).perform(click())
        onView(withText("TITLE1")).check(matches(isDisplayed()))

        onView(withId(R.id.menu_filter)).perform(click())
        onView(withText(R.string.nav_completed)).perform(click())
        onView(withText("TITLE1")).check(matches(not(isDisplayed())))
    }

    @Test
    fun displayCompletedTask() {
        repository.saveTaskBlocking(Task("TITLE1", "DESCRIPTION1", true))

        ActivityScenario.launch(TasksActivity::class.java)

        onView(withText("TITLE1")).check(matches(isDisplayed()))

        onView(withId(R.id.menu_filter)).perform(click())
        onView(withText(R.string.nav_active)).perform(click())
        onView(withText("TITLE1")).check(matches(not(isDisplayed())))

        onView(withId(R.id.menu_filter)).perform(click())
        onView(withText(R.string.nav_completed)).perform(click())
        onView(withText("TITLE1")).check(matches(isDisplayed()))
    }

    @Test
    fun deleteOneTask() {
        repository.saveTaskBlocking(Task("TITLE1", "DESCRIPTION1"))

        ActivityScenario.launch(TasksActivity::class.java)

        // Open it in details view
        onView(withText("TITLE1")).perform(click())

        // Click delete task in menu
        onView(withId(R.id.menu_delete)).perform(click())

        // Verify it was deleted
        onView(withId(R.id.menu_filter)).perform(click())
        onView(withText(R.string.nav_all)).perform(click())
        onView(withText("TITLE1")).check(doesNotExist())
    }

    @Test
    fun deleteOneOfTwoTasks() {
        repository.saveTaskBlocking(Task("TITLE1", "DESCRIPTION1"))
        repository.saveTaskBlocking(Task("TITLE2", "DESCRIPTION2"))

        ActivityScenario.launch(TasksActivity::class.java)

        // Open it in details view
        onView(withText("TITLE1")).perform(click())

        // Click delete task in menu
        onView(withId(R.id.menu_delete)).perform(click())

        // Verify it was deleted
        onView(withId(R.id.menu_filter)).perform(click())
        onView(withText(R.string.nav_all)).perform(click())
        onView(withText("TITLE1")).check(doesNotExist())
        // but not the other one
        onView(withText("TITLE2")).check(matches(isDisplayed()))
    }
}