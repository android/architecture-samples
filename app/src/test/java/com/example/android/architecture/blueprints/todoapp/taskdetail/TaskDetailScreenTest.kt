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
package com.example.android.architecture.blueprints.todoapp.taskdetail

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.ServiceLocator
import com.example.android.architecture.blueprints.todoapp.data.FakeTasksRemoteDataSource
import com.example.android.architecture.blueprints.todoapp.data.Task
import kotlinx.coroutines.runBlocking
import org.hamcrest.core.IsNot.not
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests for the tasks screen, the main screen which contains a list of all tasks.
 */
@RunWith(AndroidJUnit4::class)
class TaskDetailScreenTest {

    @After
    fun cleanupDb() {
        // Given some tasks
        ServiceLocator.provideTasksRepository(getApplicationContext()).apply {
            runBlocking {
                deleteAllTasks()
            }
        }
    }

    @Test
    fun activeTaskDetails_DisplayedInUi() {
        // GIVEN - Add active (incomplete) task to the DB
        val activeTask = Task("Active Task", "AndroidX Rocks").apply {
            isCompleted = false
        }
        FakeTasksRemoteDataSource.addTasks(activeTask)

        // WHEN - Details fragment launched to display task
        val bundle = TaskDetailFragmentArgs(activeTask.id).toBundle();
        val scenario = launchFragmentInContainer<TaskDetailFragment>(bundle, R.style.Theme_AppCompat)

        // THEN - Task details are displayed on the screen
        scenario.onFragment {
            // make sure that the title/description are both shown and correct
            onView(withId(R.id.task_detail_title)).check(matches(isDisplayed()))
            onView(withId(R.id.task_detail_title)).check(matches(withText("Active Task")))
            onView(withId(R.id.task_detail_description)).check(matches(isDisplayed()))
            onView(withId(R.id.task_detail_description)).check(matches(withText("AndroidX Rocks")))
            // and make sure the "active" checkbox is shown unchecked
            onView(withId(R.id.task_detail_complete)).check(matches(isDisplayed()))
            onView(withId(R.id.task_detail_complete)).check(matches(not(isChecked())))
        }
    }

    @Test
    fun completedTaskDetails_DisplayedInUi() {
        // GIVEN - Add completed task to the DB
        val completedTask = Task("Completed Task", "AndroidX Rocks").apply {
            isCompleted = true
        }
        FakeTasksRemoteDataSource.addTasks(completedTask)

        // WHEN - Details fragment launched to display task
        val bundle = TaskDetailFragmentArgs(completedTask.id).toBundle();
        val scenario = launchFragmentInContainer<TaskDetailFragment>(bundle, R.style.Theme_AppCompat)

        // THEN - Task details are displayed on the screen
        scenario.onFragment {
            // make sure that the title/description are both shown and correct
            onView(withId(R.id.task_detail_title)).check(matches(isDisplayed()))
            onView(withId(R.id.task_detail_title)).check(matches(withText("Completed Task")))
            onView(withId(R.id.task_detail_description)).check(matches(isDisplayed()))
            onView(withId(R.id.task_detail_description)).check(matches(withText("AndroidX Rocks")))
            // and make sure the "active" checkbox is shown unchecked
            onView(withId(R.id.task_detail_complete)).check(matches(isDisplayed()))
            onView(withId(R.id.task_detail_complete)).check(matches(isChecked()))
        }
    }
}
