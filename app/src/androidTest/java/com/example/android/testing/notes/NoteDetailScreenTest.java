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

import com.example.android.testing.notes.data.FakeNotesServiceApiImpl;
import com.example.android.testing.notes.data.Note;
import com.example.android.testing.notes.notedetails.NoteDetailActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.content.Intent;
import android.support.test.espresso.Espresso;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.example.android.testing.notes.custom.matcher.ImageViewHasDrawableMatcher.hasDrawable;
import static org.hamcrest.Matchers.allOf;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class NoteDetailScreenTest {

    private static String NOTE_TITLE = "ATSL";

    private static String NOTE_DESCRIPTION = "Rocks";

    private static String NOTE_IMAGE = "file:///android_asset/atsl-logo.png";

    private static Note NOTE = new Note(NOTE_TITLE, NOTE_DESCRIPTION, NOTE_IMAGE);

    @Rule
    public ActivityTestRule<NoteDetailActivity> mNoteDetailActivityTestRule =
            new ActivityTestRule<>(NoteDetailActivity.class, true /* Initial touch mode  */,
                    false /* Lazily launch activity */);

    @Before
    public void intentWithStubbedNoteId() {
        FakeNotesServiceApiImpl.addNotes(NOTE);
        final Intent startIntent = new Intent();
        startIntent.putExtra(NoteDetailActivity.EXTRA_NOTE_ID, NOTE.getId());
        mNoteDetailActivityTestRule.launchActivity(startIntent);
        registerIdlingResource();
    }

    @Test
    public void noteIsDetailsDisplayedInUi() throws Exception {
        onView(withId(R.id.note_detail_title)).check(matches(withText(NOTE_TITLE)));
        onView(withId(R.id.note_detail_description)).check(matches(withText(NOTE_DESCRIPTION)));
        onView(withId(R.id.note_detail_image)).check(matches(allOf(
                hasDrawable(),
                isDisplayed())));
    }

    @After
    public void unregisterIdlingResource() {
        Espresso.unregisterIdlingResources(
                mNoteDetailActivityTestRule.getActivity().getCountingIdlingResource());
    }

    private void registerIdlingResource() {
        Espresso.registerIdlingResources(
                mNoteDetailActivityTestRule.getActivity().getCountingIdlingResource());
    }
}