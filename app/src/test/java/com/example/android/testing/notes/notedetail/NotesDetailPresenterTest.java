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

package com.example.android.testing.notes.notedetail;

import com.example.android.testing.notes.data.Note;
import com.example.android.testing.notes.data.NotesRepository;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for the implementation of {@link NoteDetailPresenter}
 */
public class NotesDetailPresenterTest {

    public static final String INVALID_ID = "INVALID_ID";

    public static final String TITLE_TEST = "title";

    public static final String DESCRIPTION_TEST = "description";

    @Mock
    private NotesRepository mNotesRepository;

    @Mock
    private NoteDetailContract.View mNoteDetailView;

    /**
     * {@link ArgumentCaptor} is a powerful Mockito API to capture argument values and use them to
     * perform further actions or assertions on them.
     */
    @Captor
    private ArgumentCaptor<NotesRepository.GetNoteCallback> mGetNoteCallbackCaptor;

    private NoteDetailPresenter mNotesDetailsPresenter;

    @Before
    public void setupNotesPresenter() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);

        // Get a reference to the class under test
        mNotesDetailsPresenter = new NoteDetailPresenter(mNotesRepository, mNoteDetailView);
    }

    @Test
    public void getNoteFromRepositoryAndLoadIntoView() {
        // Given an initialized NoteDetailPresenter with stubbed note
        Note note = new Note(TITLE_TEST, DESCRIPTION_TEST);

        // When notes presenter is asked to open a note
        mNotesDetailsPresenter.openNote(note.getId());

        // Then note is loaded from model, callback is captured and progress indicator is shown
        verify(mNotesRepository).getNote(eq(note.getId()), mGetNoteCallbackCaptor.capture());
        verify(mNoteDetailView).setProgressIndicator(true);

        // When note is finally loaded
        mGetNoteCallbackCaptor.getValue().onNoteLoaded(note); // Trigger callback

        // Then progress indicator is hidden and title and description are shown in UI
        verify(mNoteDetailView).setProgressIndicator(false);
        verify(mNoteDetailView).showTitle(TITLE_TEST);
        verify(mNoteDetailView).showDescription(DESCRIPTION_TEST);
    }

    @Test
    public void getUnknownNoteFromRepositoryAndLoadIntoView() {
        // When loading of a note is requested with an invalid note ID.
        mNotesDetailsPresenter.openNote(INVALID_ID);

        // Then note with invalid id is attempted to load from model, callback is captured and
        // progress indicator is shown.
        verify(mNoteDetailView).setProgressIndicator(true);
        verify(mNotesRepository).getNote(eq(INVALID_ID), mGetNoteCallbackCaptor.capture());

        // When note is finally loaded
        mGetNoteCallbackCaptor.getValue().onNoteLoaded(null); // Trigger callback

        // Then progress indicator is hidden and missing note UI is shown
        verify(mNoteDetailView).setProgressIndicator(false);
        verify(mNoteDetailView).showMissingNote();
    }
}
