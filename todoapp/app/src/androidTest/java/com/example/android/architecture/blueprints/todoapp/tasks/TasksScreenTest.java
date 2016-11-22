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

package com.example.android.architecture.blueprints.todoapp.tasks;

import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SdkSuppress;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.text.TextUtils;
import android.view.View;
import android.widget.ListView;

import com.example.android.architecture.blueprints.todoapp.Injection;
import com.example.android.architecture.blueprints.todoapp.R;
import com.example.android.architecture.blueprints.todoapp.TestUtils;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.isChecked;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.example.android.architecture.blueprints.todoapp.TestUtils.getCurrentActivity;
import static com.google.common.base.Preconditions.checkArgument;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.core.IsNot.not;

/**
 * Tests for the tasks screen, the main screen which contains a list of all tasks.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class TasksScreenTest {

    private final static String TITLE1 = "TITLE1";

    private final static String DESCRIPTION = "DESCR";

    private final static String TITLE2 = "TITLE2";

    /**
     * {@link ActivityTestRule} is a JUnit {@link Rule @Rule} to launch your activity under test.
     * <p>
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
                    Injection.provideTasksRepository(InstrumentationRegistry.getTargetContext())
                        .deleteAllTasks();
                }
            };

    /**
     * A custom {@link Matcher} which matches an item in a {@link ListView} by its text.
     * <p>
     * View constraints:
     * <ul>
     * <li>View must be a child of a {@link ListView}
     * <ul>
     *
     * @param itemText the text to match
     * @return Matcher that matches text in the given view
     */
    private Matcher<View> withItemText(final String itemText) {
        checkArgument(!TextUtils.isEmpty(itemText), "itemText cannot be null or empty");
        return new TypeSafeMatcher<View>() {
            @Override
            public boolean matchesSafely(View item) {
                return allOf(
                        isDescendantOfA(isAssignableFrom(ListView.class)),
                        withText(itemText)).matches(item);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("is isDescendantOfA LV with text " + itemText);
            }
        };
    }

    @Test
    public void clickAddTaskButton_opensAddTaskUi() {
        // Click on the add task button
        onView(withId(R.id.fab_add_task)).perform(click());

        // Check if the add task screen is displayed
        onView(withId(R.id.add_task_title)).check(matches(isDisplayed()));
    }

    @Test
    public void editTask() throws Exception {
        // First add a task
        createTask(TITLE1, DESCRIPTION);

        // Click on the task on the list
        onView(withText(TITLE1)).perform(click());
        startEditTask();


        String editTaskTitle = TITLE2;
        String editTaskDescription = "New Description";

        // Edit task title and description
        onView(withId(R.id.add_task_title))
                .perform(replaceText(editTaskTitle), closeSoftKeyboard()); // Type new task title
        onView(withId(R.id.add_task_description)).perform(replaceText(editTaskDescription),
                closeSoftKeyboard()); // Type new task description and close the keyboard

        // Save the task
        onView(withId(R.id.fab_edit_task_done)).perform(click());

        // Verify task is displayed on screen in the task list.
        onView(withItemText(editTaskTitle)).check(matches(isDisplayed()));

        // Verify previous task is not displayed
        onView(withItemText(TITLE1)).check(doesNotExist());
    }

    @Test
    public void addTaskToTasksList() throws Exception {
        createTask(TITLE1, DESCRIPTION);

        // Verify task is displayed on screen
        onView(withItemText(TITLE1)).check(matches(isDisplayed()));
    }

    @Test
    public void markTaskAsComplete() {
        viewAllTasks();

        // Add active task
        createTask(TITLE1, DESCRIPTION);

        // Mark the task as complete
        clickCheckBoxForTask(TITLE1);

        // Verify task is shown as complete
        viewAllTasks();
        onView(withItemText(TITLE1)).check(matches(isDisplayed()));
        viewActiveTasks();
        onView(withItemText(TITLE1)).check(matches(not(isDisplayed())));
        viewCompletedTasks();
        onView(withItemText(TITLE1)).check(matches(isDisplayed()));
    }

    @Test
    public void markTaskAsActive() {
        viewAllTasks();

        // Add completed task
        createTask(TITLE1, DESCRIPTION);
        clickCheckBoxForTask(TITLE1);

        // Mark the task as active
        clickCheckBoxForTask(TITLE1);

        // Verify task is shown as active
        viewAllTasks();
        onView(withItemText(TITLE1)).check(matches(isDisplayed()));
        viewActiveTasks();
        onView(withItemText(TITLE1)).check(matches(isDisplayed()));
        viewCompletedTasks();
        onView(withItemText(TITLE1)).check(matches(not(isDisplayed())));
    }

    @Test
    public void showAllTasks() {
        // Add 2 active tasks
        createTask(TITLE1, DESCRIPTION);
        createTask(TITLE2, DESCRIPTION);

        //Verify that all our tasks are shown
        viewAllTasks();
        onView(withItemText(TITLE1)).check(matches(isDisplayed()));
        onView(withItemText(TITLE2)).check(matches(isDisplayed()));
    }

    @Test
    public void showActiveTasks() {
        // Add 2 active tasks
        createTask(TITLE1, DESCRIPTION);
        createTask(TITLE2, DESCRIPTION);

        //Verify that all our tasks are shown
        viewActiveTasks();
        onView(withItemText(TITLE1)).check(matches(isDisplayed()));
        onView(withItemText(TITLE2)).check(matches(isDisplayed()));
    }

    @Test
    public void showCompletedTasks() {
        // Add 2 completed tasks
        createTask(TITLE1, DESCRIPTION);
        clickCheckBoxForTask(TITLE1);
        createTask(TITLE2, DESCRIPTION);
        clickCheckBoxForTask(TITLE2);

        // Verify that all our tasks are shown
        viewCompletedTasks();
        onView(withItemText(TITLE1)).check(matches(isDisplayed()));
        onView(withItemText(TITLE2)).check(matches(isDisplayed()));
    }

    @Test
    public void clearCompletedTasks() {
        viewAllTasks();

        // Add 2 complete tasks
        createTask(TITLE1, DESCRIPTION);
        clickCheckBoxForTask(TITLE1);
        createTask(TITLE2, DESCRIPTION);
        clickCheckBoxForTask(TITLE2);

        // Click clear completed in menu
        openActionBarOverflowOrOptionsMenu(getTargetContext());
        onView(withText(R.string.menu_clear)).perform(click());

        //Verify that completed tasks are not shown
        onView(withItemText(TITLE1)).check(matches(not(isDisplayed())));
        onView(withItemText(TITLE2)).check(matches(not(isDisplayed())));
    }

    @Test
    public void createOneTask_deleteTask() {
        viewAllTasks();

        // Add active task
        createTask(TITLE1, DESCRIPTION);

        // Open it in details view
        onView(withText(TITLE1)).perform(click());

        // Click delete task in menu
        onView(withId(R.id.menu_delete)).perform(click());

        // Verify it was deleted
        viewAllTasks();
        onView(withText(TITLE1)).check(matches(not(isDisplayed())));
    }

    @Test
    public void createTwoTasks_deleteOneTask() {
        // Add 2 active tasks
        createTask(TITLE1, DESCRIPTION);
        createTask(TITLE2, DESCRIPTION);

        // Open the second task in details view
        onView(withText(TITLE2)).perform(click());

        // Click delete task in menu
        onView(withId(R.id.menu_delete)).perform(click());

        // Verify only one task was deleted
        viewAllTasks();
        onView(withText(TITLE1)).check(matches(isDisplayed()));
        onView(withText(TITLE2)).check(doesNotExist());
    }

    @Test
    public void markTaskAsCompleteOnDetailScreen_taskIsCompleteInList() {
        viewAllTasks();

        // Add 1 active task
        createTask(TITLE1, DESCRIPTION);

        // Click on the task on the list
        onView(withText(TITLE1)).perform(click());

        // Click on the checkbox in task details screen
        onView(withId(R.id.task_detail_complete)).perform(click());

        // Click on the navigation up button to go back to the list
        onView(withContentDescription(getToolbarNavigationContentDescription())).perform(click());

        // Check that the task is marked as completed
        onView(allOf(withId(R.id.complete),
                hasSibling(withText(TITLE1)))).check(matches(isChecked()));
    }

    @Test
    public void markTaskAsActiveOnDetailScreen_taskIsActiveInList() {
        viewAllTasks();

        // Add 1 completed task
        createTask(TITLE1, DESCRIPTION);
        clickCheckBoxForTask(TITLE1);

        // Click on the task on the list
        onView(withText(TITLE1)).perform(click());

        // Click on the checkbox in task details screen
        onView(withId(R.id.task_detail_complete)).perform(click());

        // Click on the navigation up button to go back to the list
        onView(withContentDescription(getToolbarNavigationContentDescription())).perform(click());

        // Check that the task is marked as active
        onView(allOf(withId(R.id.complete),
                hasSibling(withText(TITLE1)))).check(matches(not(isChecked())));
    }

    @Test
    public void markTaskAsAcompleteAndActiveOnDetailScreen_taskIsActiveInList() {
        viewAllTasks();

        // Add 1 active task
        createTask(TITLE1, DESCRIPTION);

        // Click on the task on the list
        onView(withText(TITLE1)).perform(click());

        // Click on the checkbox in task details screen
        onView(withId(R.id.task_detail_complete)).perform(click());

        // Click again to restore it to original state
        onView(withId(R.id.task_detail_complete)).perform(click());

        // Click on the navigation up button to go back to the list
        onView(withContentDescription(getToolbarNavigationContentDescription())).perform(click());

        // Check that the task is marked as active
        onView(allOf(withId(R.id.complete),
                hasSibling(withText(TITLE1)))).check(matches(not(isChecked())));
    }

    @Test
    public void markTaskAsActiveAndCompleteOnDetailScreen_taskIsCompleteInList() {
        viewAllTasks();

        // Add 1 completed task
        createTask(TITLE1, DESCRIPTION);
        clickCheckBoxForTask(TITLE1);

        // Click on the task on the list
        onView(withText(TITLE1)).perform(click());

        // Click on the checkbox in task details screen
        onView(withId(R.id.task_detail_complete)).perform(click());

        // Click again to restore it to original state
        onView(withId(R.id.task_detail_complete)).perform(click());

        // Click on the navigation up button to go back to the list
        onView(withContentDescription(getToolbarNavigationContentDescription())).perform(click());

        // Check that the task is marked as active
        onView(allOf(withId(R.id.complete),
                hasSibling(withText(TITLE1)))).check(matches(isChecked()));
    }

    @Test
    public void orientationChange_FilterActivePersists() {

        // Add a completed task
        createTask(TITLE1, DESCRIPTION);
        clickCheckBoxForTask(TITLE1);

        // when switching to active tasks
        viewActiveTasks();

        // then no tasks should appear
        onView(withText(TITLE1)).check(matches(not(isDisplayed())));

        // when rotating the screen
        TestUtils.rotateOrientation(mTasksActivityTestRule.getActivity());

        // then nothing changes
        onView(withText(TITLE1)).check(doesNotExist());
    }

    @Test
    public void orientationChange_FilterCompletedPersists() {

        // Add a completed task
        createTask(TITLE1, DESCRIPTION);
        clickCheckBoxForTask(TITLE1);

        // when switching to completed tasks
        viewCompletedTasks();

        // the completed task should be displayed
        onView(withText(TITLE1)).check(matches(isDisplayed()));

        // when rotating the screen
        TestUtils.rotateOrientation(mTasksActivityTestRule.getActivity());

        // then nothing changes
        onView(withText(TITLE1)).check(matches(isDisplayed()));
        onView(withText(R.string.label_completed)).check(matches(isDisplayed()));
    }

    @Test
    @SdkSuppress(minSdkVersion = 21) // Blinking cursor after rotation breaks this in API 19
    public void orientationChange_DuringEdit() throws IllegalStateException {
        // Add a completed task
        createTask(TITLE1, DESCRIPTION);

        // Open the task in details view
        onView(withText(TITLE1)).perform(click());

        // Click on the edit task button
        startEditTask();

        // Rotate the screen
        TestUtils.rotateOrientation(getCurrentActivity());

        // Edit task title and description
        onView(withId(R.id.add_task_title))
                .perform(replaceText(TITLE2), closeSoftKeyboard()); // Type new task title
        onView(withId(R.id.add_task_description)).perform(replaceText(DESCRIPTION),
                closeSoftKeyboard()); // Type new task description and close the keyboard

        // Save the task
        onView(withId(R.id.fab_edit_task_done)).perform(click());

        // Verify task is displayed on screen in the task list.
        onView(withItemText(TITLE2)).check(matches(isDisplayed()));

        // Verify previous task is not displayed
        onView(withItemText(TITLE1)).check(doesNotExist());
    }

    @Test
    @SdkSuppress(minSdkVersion = 21) // Blinking cursor after rotation breaks this in API 19
    public void orientationChange_DuringEdit_NoDuplicate() throws IllegalStateException {
        // Add a completed task
        createTask(TITLE1, DESCRIPTION);

        // Open the task in details view
        onView(withText(TITLE1)).perform(click());

        // Click on the edit task button
        startEditTask();

        // Rotate the screen
        TestUtils.rotateOrientation(getCurrentActivity());

        // Edit task title and description
        onView(withId(R.id.add_task_title))
                .perform(replaceText(TITLE2), closeSoftKeyboard()); // Type new task title
        onView(withId(R.id.add_task_description)).perform(replaceText(DESCRIPTION),
                closeSoftKeyboard()); // Type new task description and close the keyboard

        // Save the task
        onView(withId(R.id.fab_edit_task_done)).perform(click());

        // Verify task is displayed on screen in the task list.
        onView(withItemText(TITLE2)).check(matches(isDisplayed()));

        // Verify previous task is not displayed
        onView(withItemText(TITLE1)).check(doesNotExist());
    }

    private void viewAllTasks() {
        onView(withId(R.id.menu_filter)).perform(click());
        onView(withText(R.string.nav_all)).perform(click());
    }

    private void viewActiveTasks() {
        onView(withId(R.id.menu_filter)).perform(click());
        onView(withText(R.string.nav_active)).perform(click());
    }

    private void viewCompletedTasks() {
        onView(withId(R.id.menu_filter)).perform(click());
        onView(withText(R.string.nav_completed)).perform(click());
    }

    private void createTask(String title, String description) {
        // Click on the add task button
        onView(withId(R.id.fab_add_task)).perform(click());

        // Add task title and description
        onView(withId(R.id.add_task_title)).perform(typeText(title),
                closeSoftKeyboard()); // Type new task title
        onView(withId(R.id.add_task_description)).perform(typeText(description),
                closeSoftKeyboard()); // Type new task description and close the keyboard

        // Save the task
        onView(withId(R.id.fab_edit_task_done)).perform(click());
    }

    private void clickCheckBoxForTask(String title) {
        onView(allOf(withId(R.id.complete), hasSibling(withText(title)))).perform(click());
    }

    private String getText(int stringId) {
        return mTasksActivityTestRule.getActivity().getResources().getString(stringId);
    }

    private String getToolbarNavigationContentDescription() {
        return TestUtils.getToolbarNavigationContentDescription(
                mTasksActivityTestRule.getActivity(), R.id.toolbar);
    }

    private void startEditTask() {
        // Click on the edit task button, which are different things on phone and tablet.
        if (mTasksActivityTestRule.getActivity()
                .getSupportFragmentManager().findFragmentById(R.id.contentFrame_detail) == null) {
            // On phone click on FAB
            onView(withId(R.id.fab_edit_task)).perform(click());
        } else {
            // On tablet, click on menu item
            onView(withId(R.id.menu_edit)).perform(click());
        }
    }
}
