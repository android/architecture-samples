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

package com.example.android.testing.notes.model;

import com.example.android.testing.notes.model.NotesRepository.LoadNotesCallback;
import com.example.android.testing.notes.model.NotesServiceApi.NotesServiceCallback;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

/**
 * TODO: javadoc, add tests for caching notes behaviour
 */
public class NotesRepositoryTest {

    @Mock
    private NotesServiceApiImpl mServiceApi;

    @Mock
    private LoadNotesCallback mLoadNotesCallback;

    private InMemoryNotesRepository mNotesRepository;

    @Before
    public void setupNotesRepository() {
        MockitoAnnotations.initMocks(this);
        mNotesRepository = new InMemoryNotesRepository(mServiceApi);
    }

    @Test
    public void getNotes_RequestsAllNotesFromServiceApi() {
        mNotesRepository.getNotes(mLoadNotesCallback);
        verify(mServiceApi).getAllNotes(any(NotesServiceCallback.class));
    }

    @Test
    public void saveNote_savesNoteToServiceAPIAndInvalidatesCache() {
        final Note newNote = new Note("New Note Title", "Some Note Description");
        mNotesRepository.saveNote(newNote);
        assertThat(mNotesRepository.mCachedNotes, is(nullValue()));
    }
    
    @Test
    public void getNote_RequestsSingleNoteFromServiceApi() {
        mNotesRepository.getNote(any(String.class), any(NotesRepository.GetNoteCallback.class));
        verify(mServiceApi).getNote(any(String.class), any(NotesServiceCallback.class));
    }
}
