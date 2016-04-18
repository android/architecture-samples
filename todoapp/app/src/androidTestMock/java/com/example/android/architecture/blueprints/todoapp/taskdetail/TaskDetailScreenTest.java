/*
 * Copyright 2016, The Android Open Source Project
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

package com.example.android.architecture.blueprints.todoapp.taskdetail;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import com.example.android.architecture.blueprints.todoapp.R;
import com.example.android.architecture.blueprints.todoapp.TestUtils;
import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource;
import com.example.android.architecture.blueprints.todoapp.tasks.TasksActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.*;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.core.IsNot.not;

/**
 * Tests for the tasks screen, the main screen which contains a list of all tasks.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class TaskDetailScreenTest {

    private static String TASK_TITLE = "ATSL";

    private static String TASK_DESCRIPTION = "Rocks";

    /**
     * {@link Task} stub that is added to the fake service API layer.
     */
    private static Task ACTIVE_TASK = new Task(TASK_TITLE, TASK_DESCRIPTION, false);

    /**
     * {@link Task} stub that is added to the fake service API layer.
     */
    private static Task COMPLETED_TASK = new Task(TASK_TITLE, TASK_DESCRIPTION, true);

    /**
     * {@link ActivityTestRule} is a JUnit {@link Rule @Rule} to launch your activity under test.
     * <p/>
     * Rules are interceptors which are executed for each test method and are important building
     * blocks of Junit tests.
     */
    @Rule
    public ActivityTestRule<TasksActivity> mTasksActivityTestRule =
            new ActivityTestRule<TasksActivity>(TasksActivity.class) {

                /**
                 * To avoid a long list of tasks and the need to scroll through the list to find a
                 * task, we call {@link TasksDataSource#deleteAllTasks()} before each test.
                 */
                @Override
                protected void beforeActivityLaunched() {
                    super.beforeActivityLaunched();
                    // Doing this in @Before generates a race condition.

                }
            };

    @Test
    public void activeTaskDetails_DisplayedInUi() throws Exception {

        // Add active task
        createTask(TASK_TITLE, TASK_DESCRIPTION);

        // Click on the task on the list
        onView(withText(TASK_TITLE)).perform(click());

        // Check that the task title and description are displayed
        onView(withId(R.id.task_detail_title)).check(matches(withText(TASK_TITLE)));
        onView(withId(R.id.task_detail_description)).check(matches(withText(TASK_DESCRIPTION)));
        onView(withId(R.id.task_detail_complete)).check(matches(not(isChecked())));
    }

    @Test
    public void completedTaskDetails_DisplayedInUi() throws Exception {

        // Add active task
        createTask(TASK_TITLE, TASK_DESCRIPTION);

        // Mark the task as complete
        clickCheckBoxForTask(TASK_TITLE);

        // Click on the task on the list
        onView(withText(TASK_TITLE)).perform(click());

        // Check that the task title and description are displayed
        onView(withId(R.id.task_detail_title)).check(matches(withText(TASK_TITLE)));
        onView(withId(R.id.task_detail_description)).check(matches(withText(TASK_DESCRIPTION)));
        onView(withId(R.id.task_detail_complete)).check(matches(isChecked()));
    }

    @Test
    public void orientationChange_menuAndTaskPersist() {
        // Add active task
        createTask(TASK_TITLE, TASK_DESCRIPTION);

        // Click on the task on the list
        onView(withText(TASK_TITLE)).perform(click());

        TestUtils.rotateOrientation(mTasksActivityTestRule);

        // Check that the task is shown
        onView(withId(R.id.task_detail_title)).check(matches(withText(TASK_TITLE)));
        onView(withId(R.id.task_detail_description)).check(matches(withText(TASK_DESCRIPTION)));

        // Check delete menu item is displayed and is unique
        onView(withId(R.id.menu_delete)).check(matches(isDisplayed()));
    }

    private void createTask(String title, String description) {
        // Click on the add task button
        onView(withId(R.id.fab_add_task)).perform(click());

        // Add task title and description
        onView(withId(R.id.add_task_title)).perform(typeText(title)); // Type new task title
        onView(withId(R.id.add_task_description)).perform(
                typeText(description),
                closeSoftKeyboard()
        ); // Type new task description and close the keyboard

        // Save the task
        onView(withId(R.id.fab_edit_task_done)).perform(click());
    }

    private void clickCheckBoxForTask(String title) {
        onView(allOf(withId(R.id.complete), hasSibling(withText(title)))).perform(click());
    }

}
