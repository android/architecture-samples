/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.architecture.blueprints.todoapp.tasks

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.example.android.architecture.blueprints.todoapp.HiltTestActivity
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.TodoNavGraph
import com.example.android.architecture.blueprints.todoapp.TodoTheme
import com.example.android.architecture.blueprints.todoapp.data.TaskRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

/**
 * Large End-to-End test for the tasks module.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
@HiltAndroidTest
@OptIn(ExperimentalCoroutinesApi::class)
class TasksTest {
    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)
    @get:Rule(order = 1)
    var instantTaskExecutorRule = InstantTaskExecutorRule()
    @get:Rule(order = 2)
    val composeTestRule = createAndroidComposeRule<HiltTestActivity>()
    private val activity get() = composeTestRule.activity
    @Inject lateinit var repository: TaskRepository
    @Before fun init() { hiltRule.inject() }

    @Test
    fun createAndEditTask() = runTest {
        repository.createTask("TITLE1", "DESCRIPTION")
        setContent()
        onTask("TITLE1").performClick()
        composeTestRule.onNodeWithContentDescription(activity.getString(R.string.edit_task)).performClick()
        findTextField("TITLE1").performTextReplacement("NEW TITLE")
        findTextField("DESCRIPTION").performTextReplacement("NEW DESCRIPTION")
        composeTestRule.onNodeWithContentDescription(activity.getString(R.string.cd_save_task)).performClick()
        composeTestRule.onNodeWithText("NEW TITLE").assertIsDisplayed()
        composeTestRule.onNodeWithText("TITLE1").assertDoesNotExist()
    }

    @Test
    fun createAndDeleteTask() = runTest {
        setContent()
        composeTestRule.onNodeWithContentDescription(activity.getString(R.string.add_task)).performClick()
        findTextField(R.string.title_hint).performTextInput("TITLE1")
        findTextField(R.string.description_hint).performTextInput("DESCRIPTION")
        composeTestRule.onNodeWithContentDescription(activity.getString(R.string.cd_save_task)).performClick()
        onTask("TITLE1").performClick()
        composeTestRule.onNodeWithContentDescription(activity.getString(R.string.menu_delete_task)).performClick()
        composeTestRule.onNodeWithText("TITLE1").assertDoesNotExist()
    }

    @Test
    fun toggleTaskCompletion() = runTest {
        repository.createTask("TASK", "DESC")
        setContent()
        onTask("TASK").performClick()
        composeTestRule.onNode(isToggleable()).performClick()
        composeTestRule.onNodeWithContentDescription(activity.getString(R.string.menu_back)).performClick()
        composeTestRule.onNode(isToggleable()).assertIsOn()
    }

    private fun setContent() {
        composeTestRule.setContent { TodoTheme { TodoNavGraph() } }
    }
    private fun findTextField(textId: Int) = composeTestRule.onNode(hasSetTextAction() and hasText(activity.getString(textId)))
    private fun findTextField(text: String) = composeTestRule.onNode(hasSetTextAction() and hasText(text))
    private fun onTask(title: String) = composeTestRule.onNodeWithText(title)
}
