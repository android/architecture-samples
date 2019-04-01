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

import android.content.Intent
import androidx.test.core.app.ActivityScenario
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
import com.example.android.architecture.blueprints.todoapp.util.rotateOrientation
import kotlinx.coroutines.runBlocking
import org.hamcrest.core.IsNot.not
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests for the tasks screen, the main screen which contains a list of all tasks.
 */
@RunWith(AndroidJUnit4::class)
class TaskDetailScreenTest {

    private lateinit var activityScenario : ActivityScenario<TaskDetailActivity>

    @Before
    fun clearTaskRepository() {
        // Add a task stub to the fake service api layer.
        ServiceLocator.provideTasksRepository(getApplicationContext()).apply {
            runBlocking {
                deleteAllTasks()
            }
        }
    }

    @Test
    fun activeTaskDetails_DisplayedInUi() {
        FakeTasksRemoteDataSource.addTasks(ACTIVE_TASK)

        val startIntent = Intent(getApplicationContext(),
            TaskDetailActivity::class.java).apply {
            putExtra(TaskDetailActivity.EXTRA_TASK_ID, ACTIVE_TASK.id)
        }

        activityScenario = ActivityScenario.launch(startIntent)

        activityScenario.onActivity {
            // make sure that the title/description are both shown and correct
            onView(withId(R.id.task_detail_title)).check(matches(isDisplayed()))
            onView(withId(R.id.task_detail_title)).check(matches(withText(TASK_TITLE)))
            onView(withId(R.id.task_detail_description)).check(matches(isDisplayed()))
            onView(withId(R.id.task_detail_description)).check(matches(withText(TASK_DESCRIPTION)))
            // and make sure the "active" checkbox is shown unchecked
            onView(withId(R.id.task_detail_complete)).check(matches(isDisplayed()))
            onView(withId(R.id.task_detail_complete)).check(matches(not(isChecked())))
        }
    }

    @Test
    fun completedTaskDetails_DisplayedInUi() {
        FakeTasksRemoteDataSource.addTasks(COMPLETED_TASK)

        val startIntent = Intent(getApplicationContext(),
            TaskDetailActivity::class.java).apply {
            putExtra(TaskDetailActivity.EXTRA_TASK_ID, COMPLETED_TASK.id)
        }

        activityScenario = ActivityScenario.launch(startIntent)

        activityScenario.onActivity {
            // make sure that the title/description are both shown and correct
            onView(withId(R.id.task_detail_title)).check(matches(isDisplayed()))
            onView(withId(R.id.task_detail_title)).check(matches(withText(TASK_TITLE)))
            onView(withId(R.id.task_detail_description)).check(matches(isDisplayed()))
            onView(withId(R.id.task_detail_description)).check(matches(withText(TASK_DESCRIPTION)))
            // and make sure the "active" checkbox is shown unchecked
            onView(withId(R.id.task_detail_complete)).check(matches(isDisplayed()))
            onView(withId(R.id.task_detail_complete)).check(matches(isChecked()))
        }
    }

    @Test
    fun orientationChange_menuAndTaskPersist() {
        FakeTasksRemoteDataSource.addTasks(ACTIVE_TASK)

        val startIntent = Intent(getApplicationContext(),
            TaskDetailActivity::class.java).apply {
            putExtra(TaskDetailActivity.EXTRA_TASK_ID, ACTIVE_TASK.id)
        }

        activityScenario = ActivityScenario.launch(startIntent)

        activityScenario.onActivity {
            // Check delete menu item is displayed and is unique
            onView(withId(R.id.menu_delete)).check(matches(isDisplayed()))

            it.rotateOrientation()

            // make sure that the title/description are both shown and correct
            onView(withId(R.id.task_detail_title)).check(matches(isDisplayed()))
            onView(withId(R.id.task_detail_title)).check(matches(withText(TASK_TITLE)))
            onView(withId(R.id.task_detail_description)).check(matches(isDisplayed()))
            onView(withId(R.id.task_detail_description)).check(matches(withText(TASK_DESCRIPTION)))
            // and make sure the "active" checkbox is shown unchecked
            onView(withId(R.id.task_detail_complete)).check(matches(isDisplayed()))
            onView(withId(R.id.task_detail_complete)).check(matches(not(isChecked())))
            // Check delete menu item is displayed and is unique
            onView(withId(R.id.menu_delete)).check(matches(isDisplayed()))
        }
    }

    companion object {

        private val TASK_TITLE = "AndroidX Test"

        private val TASK_DESCRIPTION = "Rocks"

        /**
         * [Task] stub that is added to the fake service API layer.
         */
        private val ACTIVE_TASK = Task(TASK_TITLE, TASK_DESCRIPTION).apply {
            isCompleted = false
        }

        /**
         * [Task] stub that is added to the fake service API layer.
         */
        private val COMPLETED_TASK = Task(TASK_TITLE, TASK_DESCRIPTION).apply {
            isCompleted = true
        }
    }
}
