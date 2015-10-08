/*
 * Copyright 2015, The Android Open Source Project
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

package com.example.android.testing.notes;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.text.TextUtils;
import android.view.View;

import com.example.android.testing.notes.model.NoteRepositories;
import com.example.android.testing.notes.stub.FakeNotesServiceApi;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.contrib.RecyclerViewActions.scrollTo;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.google.common.base.Preconditions.checkArgument;
import static org.hamcrest.Matchers.allOf;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class NotesActivityTest {

    private Matcher<View> withItemText(final String itemText) {
        checkArgument(!TextUtils.isEmpty(itemText), "itemText cannot be null or empty");
        return new TypeSafeMatcher<View>() {
            @Override
            public boolean matchesSafely(View item) {
                return allOf(isDescendantOfA(withId(R.id.notes_list)), withText(itemText)).matches(item);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("is isDescendantOfA RV with text " + itemText);
            }
        };
    }

    @Rule
    public ActivityTestRule<NotesActivity> mNotesActivityTestRule =
            new ActivityTestRule<NotesActivity>(NotesActivity.class) {

                @Override
                protected void beforeActivityLaunched() {
                    // TODO this is kinda messed up refactor code to make for a cleaner Service API
                    // injection or use dagger
                    NoteRepositories.getInMemoryRepoInstance(new FakeNotesServiceApi());
                }
            };

    @Test
    public void clickAddNoteButton_opensAddNoteUi() throws Exception {
        onView(withId(R.id.fab_add_note)).perform(click());
        onView(withId(R.id.fab_done)).check(matches(isDisplayed()));
    }

    @Test
    public void addNoteToNotesList() throws Exception {
        final String newNoteTitle = "Espresso";
        final String newNoteDescription = "Ui testing for Android";

        onView(withId(R.id.fab_add_note)).perform(click());
        // Add note title and description
        onView(withId(R.id.add_note_title)).perform(typeText(newNoteTitle));
        onView(withId(R.id.add_note_description)).perform(typeText(newNoteDescription),
                closeSoftKeyboard());
        // Save the note
        onView(withId(R.id.fab_done)).perform(click());

        // Verify successful save snackbar is shown
        String successfullySavedNoteMessage = getTargetContext()
                .getString(R.string.successfully_saved_note_message);
        onView(withText(successfullySavedNoteMessage)).check(matches(isDisplayed()));

        // Verify note is displayed on screen
        onView(withId(R.id.notes_list)).perform(scrollTo(hasDescendant(withText(newNoteDescription))));
        onView(withItemText(newNoteDescription)).check(matches(isDisplayed()));
    }

    @Test
    public void errorShownOnEmptyMessage() throws InterruptedException {
        onView(withId(R.id.fab_add_note)).perform(click());
        // Add note title and description
        onView(withId(R.id.add_note_title)).perform(typeText(""));
        onView(withId(R.id.add_note_description)).perform(typeText(""),
                closeSoftKeyboard());
        // Save the note
        onView(withId(R.id.fab_done)).perform(click());

        // Verify empty notes snackbar is shown
        String emptyNoteMessageText = getTargetContext().getString(R.string.empty_note_message);
        onView(withText(emptyNoteMessageText)).check(matches(isDisplayed()));
    }

}