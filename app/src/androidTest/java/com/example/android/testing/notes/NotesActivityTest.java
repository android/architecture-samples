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

import com.example.android.testing.notes.model.NoteRepositories;
import com.example.android.testing.notes.stub.FakeNotesServiceApi;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class NotesActivityTest {

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
    public void clickAddNoteButton_opensAddNoteFragment() throws Exception {
        onView(withId(R.id.fab_add_note)).perform(click());
        onView(withId(R.id.fab_done)).check(matches(isDisplayed()));
    }

}