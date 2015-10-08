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

package com.example.android.testing.notes.presenter;

import com.example.android.testing.notes.model.Note;
import com.example.android.testing.notes.model.NotesRepository;
import com.example.android.testing.notes.view.NoteDetailsView;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

/**
 * TODO javadoc, add tests for invaldating cache logic
 */
public class NotesDetailPresenterTest {

    public static final String INVALID_ID = "INVALID_ID";

    public static final String TITLE_TEST = "title";

    public static final String DESCRIPTION_TEST = "description";

    @Mock
    private NotesRepository mNotesRepository;

    @Mock
    private NoteDetailsView mNoteDetailsView;

    @Captor
    private ArgumentCaptor<NotesRepository.GetNoteCallback> mGetNoteCallbackCaptor;

    private NoteDetailPresenter mNotesDetailsPresenter;

    @Before
    public void setupNotesPresenter() {
        MockitoAnnotations.initMocks(this);
        mNotesDetailsPresenter = new NoteDetailPresenterImpl(mNotesRepository, mNoteDetailsView);
    }

    @Test
    public void getNoteFromRepositoryAndLoadIntoView() {
        // Given an initialized NoteDetailPresenterImpl with initialized notes
        // When loading of a note is requested

        Note note = new Note(TITLE_TEST, DESCRIPTION_TEST);

        mNotesDetailsPresenter.openNote(note.getId());
        verify(mNoteDetailsView).setProgressIndicator(true);
        verify(mNotesRepository).getNote(eq(note.getId()), mGetNoteCallbackCaptor.capture());

        mGetNoteCallbackCaptor.getValue().onNoteLoaded(note); // Trigger callback

        verify(mNoteDetailsView).setProgressIndicator(false);
        verify(mNoteDetailsView).showNote(TITLE_TEST, DESCRIPTION_TEST);
    }

    @Test
    public void getUnknownNoteFromRepositoryAndLoadIntoView() {
        // Given an initialized NoteDetailPresenterImpl with initialized notes
        // When loading of a note is requested

        mNotesDetailsPresenter.openNote(INVALID_ID);
        verify(mNoteDetailsView).setProgressIndicator(true);
        verify(mNotesRepository).getNote(eq(INVALID_ID), mGetNoteCallbackCaptor.capture());

        mGetNoteCallbackCaptor.getValue().onNoteLoaded(null); // Trigger callback

        verify(mNoteDetailsView).setProgressIndicator(false);
        verify(mNoteDetailsView).showMissingNote();
    }
}
