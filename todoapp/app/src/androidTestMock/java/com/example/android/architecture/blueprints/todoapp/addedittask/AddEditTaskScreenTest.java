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

package com.example.android.architecture.blueprints.todoapp.addedittask;

import static com.example.android.architecture.blueprints.todoapp.R.id.toolbar;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Intent;
import android.content.res.Resources;
import android.view.View;

import com.example.android.architecture.blueprints.todoapp.R;
import com.example.android.architecture.blueprints.todoapp.TestUtils;
import com.example.android.architecture.blueprints.todoapp.data.FakeTasksRemoteDataSource;
import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;
import com.example.android.architecture.blueprints.todoapp.util.EspressoIdlingResource;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.espresso.matcher.BoundedMatcher;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

/**
 * Tests for the add task screen.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class AddEditTaskScreenTest {

    private static final String TASK_ID = "1";

    /**
     * {@link IntentsTestRule} is an {@link ActivityTestRule} which inits and releases Espresso
     * Intents before and after each test run.
     *
     * <p>
     * Rules are interceptors which are executed for each test method and are important building
     * blocks of Junit tests.
     */
    @Rule
    public ActivityTestRule<AddEditTaskActivity> mActivityTestRule =
            new ActivityTestRule<>(AddEditTaskActivity.class, false, false);

    /**
     * Prepare your test fixture for this test. In this case we register an IdlingResources with
     * Espresso. IdlingResource resource is a great way to tell Espresso when your app is in an
     * idle state. This helps Espresso to synchronize your test actions, which makes tests
     * significantly more reliable.
     */
    @Before
    public void registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.getIdlingResource());
    }

    /**
     * Unregister your Idling Resource so it can be garbage collected and does not leak any memory.
     */
    @After
    public void unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.getIdlingResource());
    }

    @Test
    public void emptyTask_isNotSaved() {
        // Launch activity to add a new task
        launchNewTaskActivity(null);

        // Add invalid title and description combination
        onView(withId(R.id.add_task_title)).perform(clearText());
        onView(withId(R.id.add_task_description)).perform(clearText());
        // Try to save the task
        onView(withId(R.id.fab_edit_task_done)).perform(click());

        // Verify that the activity is still displayed (a correct task would close it).
        onView(withId(R.id.add_task_title)).check(matches(isDisplayed()));
    }

    @Test
    public void toolbarTitle_newTask_persistsRotation() {
        // Launch activity to add a new task
        launchNewTaskActivity(null);

        // Check that the toolbar shows the correct title
        onView(withId(toolbar)).check(matches(withToolbarTitle(R.string.add_task)));

        // Rotate activity
        TestUtils.rotateOrientation(mActivityTestRule.getActivity());

        // Check that the toolbar title is persisted
        onView(withId(toolbar)).check(matches(withToolbarTitle(R.string.add_task)));
    }

    @Test
    public void toolbarTitle_editTask_persistsRotation() throws Throwable {
        mActivityTestRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TasksRepository.destroyInstance();
                FakeTasksRemoteDataSource.getInstance().addTasks(
                        new Task("AddTitle", "", TASK_ID, false)
                );
            }
        });
        launchNewTaskActivity(TASK_ID);


        // Check that the toolbar shows the correct title
        onView(withId(toolbar)).check(matches(withToolbarTitle(R.string.edit_task)));

        // Rotate activity
        TestUtils.rotateOrientation(mActivityTestRule.getActivity());

        // check that the toolbar title is persisted
        onView(withId(toolbar)).check(matches(withToolbarTitle(R.string.edit_task)));
    }

    /**
     * @param taskId is null if used to add a new task, otherwise it edits the task.
     */
    private void launchNewTaskActivity(@Nullable String taskId) {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(),
                AddEditTaskActivity.class);

        intent.putExtra(AddEditTaskFragment.ARGUMENT_EDIT_TASK_ID, taskId);
        mActivityTestRule.launchActivity(intent);
    }

    /**
     * Matches the toolbar title with a specific string resource.
     *
     * @param resourceId the ID of the string resource to match
     */
    public static Matcher<View> withToolbarTitle(final int resourceId) {
        return new BoundedMatcher<View, Toolbar>(Toolbar.class) {

            @Override
            public void describeTo(Description description) {
                description.appendText("with toolbar title from resource id: ");
                description.appendValue(resourceId);
            }

            @Override
            protected boolean matchesSafely(Toolbar toolbar) {
                CharSequence expectedText = "";
                try {
                    expectedText = toolbar.getResources().getString(resourceId);
                } catch (Resources.NotFoundException ignored) {
                    /* view could be from a context unaware of the resource id. */
                }
                CharSequence actualText = toolbar.getTitle();
                return expectedText.equals(actualText);
            }
        };
    }
}
