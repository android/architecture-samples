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

import com.google.common.collect.Lists;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Tests for the implementation of the in-memory repository with cache.
 */
public class InMemoryNotesRepositoryTest {

    private static List<Note> NOTES = Lists.newArrayList(new Note("Title1", "Description1"),
            new Note("Title2", "Description2"));

    private NotesRepository mNotesRepository;

    @Mock
    private NotesServiceApiImpl mServiceApi;

    @Captor
    private ArgumentCaptor<NotesServiceApi.NotesServiceCallback> mNotesServiceCallbackCaptor;

    @Before
    public void setupNotesRepository() {
        MockitoAnnotations.initMocks(this);
        mNotesRepository = new InMemoryNotesRepository(mServiceApi);
    }

    @Test
    public void getNotes_Cache() {
        NotesRepository.LoadNotesCallback callback = mock(NotesRepository.LoadNotesCallback.class);
        makeTwoCallsToRepository(callback);

        // The API should be called only once
        verify(mServiceApi, times(1)).getAllNotes(any(NotesServiceApi.NotesServiceCallback.class));
    }

    @Test
    public void invalidateCache_DoesntCallTheServiceApi() {
        NotesRepository.LoadNotesCallback callback = mock(NotesRepository.LoadNotesCallback.class);
        makeTwoCallsToRepository(callback);

        mNotesRepository.refreshData();
        mNotesRepository.getNotes(callback); // Second call to API
        verify(mServiceApi, times(2)).getAllNotes(any(NotesServiceApi.NotesServiceCallback.class));
    }

    private void makeTwoCallsToRepository(NotesRepository.LoadNotesCallback callback) {
        mNotesRepository.getNotes(callback); // First call to API
        verify(mServiceApi).getAllNotes(mNotesServiceCallbackCaptor.capture());

        // Trigger callback so notes are cached
        mNotesServiceCallbackCaptor.getValue().onLoaded(NOTES);

        mNotesRepository.getNotes(callback); // No call to API
    }
}