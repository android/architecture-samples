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
import com.example.android.testing.notes.view.AddNoteView;
import com.example.android.testing.notes.view.NotesView;
import com.google.common.collect.Lists;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.verify;

/**
 * TODO: javadoc
 */
public class AddNotePresenterTest {

    @Mock
    private NotesRepository mNotesRepository;

    @Mock
    private AddNoteView mAddNoteView;

    private AddNotePresenterImpl mAddNotesPresenter;

    @Before
    public void setupAddNotePresenter() {
        MockitoAnnotations.initMocks(this);
        mAddNotesPresenter = new AddNotePresenterImpl(mNotesRepository, mAddNoteView);
    }

    @Test
    public void savesNoteToRepository_showsSuccessMessage() {
        final Note newNote = new Note("New Note Title", "Some Note Description");
        mAddNotesPresenter.saveNote(newNote);
        verify(mNotesRepository).saveNote(newNote);
        verify(mAddNoteView).showNotesList();
    }

    @Test
    public void saveNote_emptyNoteShowError() {
        final Note emptyNote = new Note("", "");
        mAddNotesPresenter.saveNote(emptyNote);
        verify(mAddNoteView).showEmptyNoteError();
    }

}
