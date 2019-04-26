/*
 * Copyright 2017, The Android Open Source Project
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
package com.example.android.architecture.blueprints.todoapp.statistics

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
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
import com.example.android.architecture.blueprints.todoapp.util.saveTaskBlocking
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration test for the statistics screen.
 */
@RunWith(AndroidJUnit4::class)
@MediumTest
class StatisticsFragmentTest {
    private lateinit var repository: TasksRepository

    @Before
    fun initRepository() {
        repository = ServiceLocator.provideTasksRepository(getApplicationContext())
    }

    @After
    fun cleanupDb() = runBlocking {
        ServiceLocator.resetForTests()
    }

    @Test
    fun tasks_showsNonEmptyMessage() {
        // Given some tasks
        repository.apply {
            saveTaskBlocking(Task("Title1", "Description1", false))
            saveTaskBlocking(Task("Title2", "Description2", true))
        }

        launchFragmentInContainer<StatisticsFragment>(Bundle(), R.style.AppTheme)
        val expectedActiveTaskText = getApplicationContext<Context>()
            .getString(R.string.statistics_active_tasks, 50.0f)
        val expectedCompletedTaskText = getApplicationContext<Context>()
            .getString(R.string.statistics_completed_tasks, 50.0f)
        // check that both info boxes are displayed and contain the correct info
        onView(withId(R.id.stats_active_text)).check(matches(isDisplayed()))
        onView(withId(R.id.stats_active_text)).check(matches(withText(expectedActiveTaskText)))
        onView(withId(R.id.stats_completed_text)).check(matches(isDisplayed()))
        onView(withId(R.id.stats_completed_text)).check(matches(withText(expectedCompletedTaskText)))
    }
}
