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

import com.google.common.collect.Lists;

import com.example.android.testing.notes.model.Note;
import com.example.android.testing.notes.model.NotesRepository;
import com.example.android.testing.notes.model.NotesRepository.LoadNotesCallback;
import com.example.android.testing.notes.view.NotesView;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

/**
 * TODO javadoc, add tests for invaldating cache logic
 */
public class NotesPresenterTest {

    private static List<Note> NOTES = Lists.newArrayList(new Note("Title1", "Description1"),
            new Note("Title2", "Description2"));

    private static List<Note> EMPTY_NOTES = new ArrayList<>(0);

    @Mock
    private NotesRepository mNotesRepository;

    @Mock
    private NotesView mNotesView;

    @Captor
    private ArgumentCaptor<LoadNotesCallback> mLoadNotesCallbackCaptor;

    private NotesPresenter mNotesPresenter;

    @Before
    public void setupNotesPresenter() {
        MockitoAnnotations.initMocks(this);
        mNotesPresenter = new NotesPresenterImpl(mNotesRepository, mNotesView);
    }

    @Test
    public void loadNotesFromRepositoryAndLoadIntoView() {
        // Given an initialized NotesPresenterImpl with initialized notes
        // When loading of Notes is requested
        mNotesPresenter.loadNotes(true);
        verify(mNotesRepository).getNotes(mLoadNotesCallbackCaptor.capture());
        // Invoke the callback with stub notes
        mLoadNotesCallbackCaptor.getValue().onNotesLoaded(NOTES);
        verify(mNotesView).setProgressIndicator(false);
        verify(mNotesView).showNotes(NOTES);
    }

    @Test
    public void emptyNotes_showsEmptyNotesPlaceholder() {
        // Given an initialized NotesPresenterImpl with empty notes
        // When loading of Notes is requested
        mNotesPresenter.loadNotes(true);
        verify(mNotesRepository).getNotes(mLoadNotesCallbackCaptor.capture());
        // Invoke the callback with empty notes
        mLoadNotesCallbackCaptor.getValue().onNotesLoaded(EMPTY_NOTES);
        // Then verify that empty placeholder is shown
        verify(mNotesView).showNotesEmptyPlaceholder();
    }

    @Test
    public void clickOnFab_ShowsAddsNoteUi() {
        mNotesPresenter.addNewNote();
        verify(mNotesView).showAddNote();
    }

    @Test
    public void clickOnNote_ShowsDetailUi() {
        final Note requestedNote = new Note("Details Requested", "For this note");
        mNotesPresenter.openNoteDetails(requestedNote);
        verify(mNotesView).showNoteDetailUi(any(String.class));
    }
}
