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
import com.example.android.architecture.blueprints.todoapp.data.TaskRepository
import com.google.accompanist.appcompattheme.AppCompatTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

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

    // Executes tasks in the Architecture Components in the same thread
    @get:Rule(order = 1)
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule(order = 2)
    val composeTestRule = createAndroidComposeRule<HiltTestActivity>()
    private val activity get() = composeTestRule.activity

    @Inject
    lateinit var repository: TaskRepository

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun editTask() = runTest {
        val originalTaskTitle = "TITLE1"
        repository.create(originalTaskTitle, "DESCRIPTION")

        setContent()

        // Click on the task on the list and verify that all the data is correct
        composeTestRule.onNodeWithText(activity.getString(R.string.label_all)).assertIsDisplayed()
        composeTestRule.onNodeWithText(originalTaskTitle).assertIsDisplayed()
        composeTestRule.onNodeWithText(originalTaskTitle).performClick()

        // Task detail screen
        composeTestRule.onNodeWithText(activity.getString(R.string.task_details))
            .assertIsDisplayed()
        composeTestRule.onNodeWithText(originalTaskTitle).assertIsDisplayed()
        composeTestRule.onNodeWithText("DESCRIPTION").assertIsDisplayed()
        composeTestRule.onNode(isToggleable()).assertIsOff()

        // Click on the edit button, edit, and save
        composeTestRule.onNodeWithContentDescription(activity.getString(R.string.edit_task))
            .performClick()
        composeTestRule.onNodeWithText(activity.getString(R.string.edit_task)).assertIsDisplayed()
        findTextField(originalTaskTitle).performTextReplacement("NEW TITLE")
        findTextField("DESCRIPTION").performTextReplacement("NEW DESCRIPTION")
        composeTestRule.onNodeWithContentDescription(activity.getString(R.string.cd_save_task))
            .performClick()

        // Verify task is displayed on screen in the task list.
        composeTestRule.onNodeWithText(activity.getString(R.string.label_all)).assertIsDisplayed()
        composeTestRule.onNodeWithText("NEW TITLE").assertIsDisplayed()
        // Verify previous task is not displayed
        composeTestRule.onNodeWithText(originalTaskTitle).assertDoesNotExist()
    }

    @Test
    fun createOneTask_deleteTask() {
        setContent()

        val taskTitle = "TITLE1"
        // Add active task
        composeTestRule.onNodeWithContentDescription(activity.getString(R.string.add_task))
            .performClick()
        findTextField(R.string.title_hint).performTextInput(taskTitle)
        findTextField(R.string.description_hint).performTextInput("DESCRIPTION")
        composeTestRule.onNodeWithContentDescription(activity.getString(R.string.cd_save_task))
            .performClick()
        composeTestRule.onNodeWithText(activity.getString(R.string.label_all)).assertIsDisplayed()
        composeTestRule.onNodeWithText(taskTitle).assertIsDisplayed()

        // Open the task detail screen
        composeTestRule.onNodeWithText(taskTitle).performClick()
        composeTestRule.onNodeWithText(activity.getString(R.string.task_details))
            .assertIsDisplayed()
        // Click delete task in menu
        composeTestRule.onNodeWithContentDescription(activity.getString(R.string.menu_delete_task))
            .performClick()

        // Verify it was deleted
        composeTestRule.onNodeWithContentDescription(activity.getString(R.string.menu_filter))
            .performClick()
        composeTestRule.onNodeWithText(activity.getString(R.string.nav_all)).assertIsDisplayed()
        composeTestRule.onNodeWithText(taskTitle).assertDoesNotExist()
    }

    @Test
    fun createTwoTasks_deleteOneTask() = runTest {
        repository.apply {
            create("TITLE1", "DESCRIPTION")
            create("TITLE2", "DESCRIPTION")
        }

        setContent()

        // Open the second task in details view
        composeTestRule.onNodeWithText(activity.getString(R.string.label_all)).assertIsDisplayed()
        composeTestRule.onNodeWithText("TITLE2").assertIsDisplayed()
        composeTestRule.onNodeWithText("TITLE2").performClick()
        // Click delete task in menu
        composeTestRule.onNodeWithContentDescription(activity.getString(R.string.menu_delete_task))
            .performClick()

        // Verify only one task was deleted
        composeTestRule.onNodeWithContentDescription(activity.getString(R.string.menu_filter))
            .performClick()
        composeTestRule.onNodeWithText(activity.getString(R.string.label_all)).performClick()
        composeTestRule.onNodeWithText("TITLE1").assertIsDisplayed()
        composeTestRule.onNodeWithText("TITLE2").assertDoesNotExist()
    }

    @Test
    fun markTaskAsCompleteOnDetailScreen_taskIsCompleteInList() = runTest {
        // Add 1 active task
        val taskTitle = "COMPLETED"
        repository.create(taskTitle, "DESCRIPTION")

        setContent()

        // Click on the task on the list
        composeTestRule.onNodeWithText(activity.getString(R.string.label_all)).assertIsDisplayed()
        composeTestRule.onNodeWithText(taskTitle).assertIsDisplayed()
        composeTestRule.onNodeWithText(taskTitle).performClick()

        // Click on the checkbox in task details screen
        composeTestRule.onNode(isToggleable()).performClick()

        // Click on the navigation up button to go back to the list
        composeTestRule.onNodeWithContentDescription(activity.getString(R.string.menu_back))
            .performClick()

        // Check that the task is marked as completed
        composeTestRule.onNodeWithText(activity.getString(R.string.label_all)).assertIsDisplayed()
        composeTestRule.onNode(isToggleable()).assertIsOn()
    }

    @Test
    fun markTaskAsActiveOnDetailScreen_taskIsActiveInList() = runTest {
        // Add 1 completed task
        val taskTitle = "ACTIVE"
        repository.apply {
            create(taskTitle, "DESCRIPTION").also { complete(it) }
        }

        setContent()

        // Click on the task on the list
        composeTestRule.onNodeWithText(activity.getString(R.string.label_all)).assertIsDisplayed()
        composeTestRule.onNodeWithText(taskTitle).assertIsDisplayed()
        composeTestRule.onNodeWithText(taskTitle).performClick()

        // Click on the checkbox in task details screen
        composeTestRule.onNode(isToggleable()).performClick()

        // Click on the navigation up button to go back to the list
        composeTestRule.onNodeWithContentDescription(activity.getString(R.string.menu_back))
            .performClick()

        // Check that the task is marked as active
        composeTestRule.onNodeWithText(activity.getString(R.string.label_all)).assertIsDisplayed()
        composeTestRule.onNode(isToggleable()).assertIsOff()
    }

    @Test
    fun markTaskAsCompleteAndActiveOnDetailScreen_taskIsActiveInList() = runTest {
        // Add 1 active task
        val taskTitle = "ACT-COMP"
        repository.create(taskTitle, "DESCRIPTION")

        setContent()

        // Click on the task on the list
        composeTestRule.onNodeWithText(activity.getString(R.string.label_all)).assertIsDisplayed()
        composeTestRule.onNodeWithText(taskTitle).assertIsDisplayed()
        composeTestRule.onNodeWithText(taskTitle).performClick()

        // Click on the checkbox in task details screen
        composeTestRule.onNode(isToggleable()).performClick()
        // Click again to restore it to original state
        composeTestRule.onNode(isToggleable()).performClick()

        // Click on the navigation up button to go back to the list
        composeTestRule.onNodeWithContentDescription(activity.getString(R.string.menu_back))
            .performClick()

        // Check that the task is marked as active
        composeTestRule.onNodeWithText(activity.getString(R.string.label_all)).assertIsDisplayed()
        composeTestRule.onNode(isToggleable()).assertIsOff()
    }

    @Test
    fun markTaskAsActiveAndCompleteOnDetailScreen_taskIsCompleteInList() = runTest {
        // Add 1 completed task
        val taskTitle = "COMP-ACT"
        repository.apply {
            create(taskTitle, "DESCRIPTION").also { complete(it) }
        }

        setContent()

        // Click on the task on the list
        composeTestRule.onNodeWithText(activity.getString(R.string.label_all)).assertIsDisplayed()
        composeTestRule.onNodeWithText(taskTitle).assertIsDisplayed()
        composeTestRule.onNodeWithText(taskTitle).performClick()
        // Click on the checkbox in task details screen
        composeTestRule.onNode(isToggleable()).performClick()
        // Click again to restore it to original state
        composeTestRule.onNode(isToggleable()).performClick()

        // Click on the navigation up button to go back to the list
        composeTestRule.onNodeWithContentDescription(activity.getString(R.string.menu_back))
            .performClick()

        // Check that the task is marked as active
        composeTestRule.onNodeWithText(activity.getString(R.string.label_all)).assertIsDisplayed()
        composeTestRule.onNode(isToggleable()).assertIsOn()
    }

    @Test
    fun createTask() {
        setContent()

        // Click on the "+" button, add details, and save
        composeTestRule.onNodeWithContentDescription(activity.getString(R.string.add_task))
            .performClick()
        findTextField(R.string.title_hint).performTextInput("title")
        findTextField(R.string.description_hint).performTextInput("description")
        composeTestRule.onNodeWithContentDescription(activity.getString(R.string.cd_save_task))
            .performClick()

        // Then verify task is displayed on screen
        composeTestRule.onNodeWithText(activity.getString(R.string.label_all)).assertIsDisplayed()
        composeTestRule.onNodeWithText("title").assertIsDisplayed()
    }

    private fun setContent() {
        composeTestRule.setContent {
            AppCompatTheme {
                TodoNavGraph()
            }
        }
    }

    private fun findTextField(textId: Int): SemanticsNodeInteraction {
        return composeTestRule.onNode(
            hasSetTextAction() and hasText(activity.getString(textId))
        )
    }

    private fun findTextField(text: String): SemanticsNodeInteraction {
        return composeTestRule.onNode(
            hasSetTextAction() and hasText(text)
        )
    }
}
