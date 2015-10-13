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

import org.hamcrest.Description;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.app.Activity;
import android.app.Instrumentation.ActivityResult;
import android.provider.MediaStore;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.espresso.matcher.BoundedMatcher;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;
import android.widget.ImageView;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intending;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class AddNoteScreenTest {

    private BoundedMatcher<View, ImageView> hasDrawable() {
        return new BoundedMatcher<View, ImageView>(ImageView.class) {
            @Override
            public void describeTo(Description description) {
                description.appendText("has drawable");
            }

            @Override
            public boolean matchesSafely(ImageView imageView) {
                return imageView.getDrawable() != null;
            }
        };
    }

    @Rule
    public IntentsTestRule<NotesActivity> mNotesIntentsTestRule =
            new IntentsTestRule<>(NotesActivity.class);

    @Test
    public void addImageToNoteShowsThumbnailInUi() {
        // Stub take image Intent.
        ActivityResult result = createImageCaptureActivityResultStub();
        intending(hasAction(MediaStore.ACTION_IMAGE_CAPTURE)).respondWith(result);

        // Open add notes screen
        onView(withId(R.id.fab_notes)).perform(click());

        // Check thumbnail view is not shown
        onView(withId(R.id.add_note_image_thumbnail)).check(matches(not(isDisplayed())));
        selectTakeImageFromMenu();

        onView(withId(R.id.add_note_image_thumbnail))
                .check(matches(allOf(
                        hasDrawable(),
                        isDisplayed())));
    }

    @Test
    public void errorShownOnEmptyMessage() throws InterruptedException {
        onView(withId(R.id.fab_notes)).perform(click());
        // Add note title and description
        onView(withId(R.id.add_note_title)).perform(typeText(""));
        onView(withId(R.id.add_note_description)).perform(typeText(""),
                closeSoftKeyboard());
        // Save the note
        onView(withId(R.id.fab_notes)).perform(click());

        // Verify empty notes snackbar is shown
        String emptyNoteMessageText = getTargetContext().getString(R.string.empty_note_message);
        onView(withText(emptyNoteMessageText)).check(matches(isDisplayed()));
    }

    private void selectTakeImageFromMenu() {
        openActionBarOverflowOrOptionsMenu(getTargetContext());
        onView(withText(R.string.add_picture)).perform(click());
    }

    private ActivityResult createImageCaptureActivityResultStub() {
        // Create the ActivityResult with a null Intent.
        return new ActivityResult(Activity.RESULT_OK, null);
    }

}