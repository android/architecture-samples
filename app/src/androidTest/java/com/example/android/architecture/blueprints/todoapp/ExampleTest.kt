package com.example.android.architecture.blueprints.todoapp

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.android.architecture.blueprints.todoapp.tasks.TasksActivity

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Rule

@RunWith(AndroidJUnit4::class)
class ExampleTest {
    @get:Rule
    var activityRule = ActivityScenarioRule(TasksActivity::class.java)

    @Test
    fun testAddTask() {
        onView(withId(R.id.add_task_fab)).perform(click())
        onView(withId(R.id.add_task_title_edit_text)).perform(typeText("Adopt Android Espresso Tests"))
        onView(withId(R.id.add_task_description_edit_text)).perform(typeText("Test Everything!"))
        onView(withId(R.id.save_task_fab)).perform(click())
        onView(withId(R.id.title_text)).check(matches(withText("Adopt Android Espresso Tests")))
    }
}
