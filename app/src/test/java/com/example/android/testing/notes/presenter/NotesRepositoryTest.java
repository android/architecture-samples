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

import com.example.android.testing.notes.model.NotesServiceApiImpl;
import com.example.android.testing.notes.model.NotesRepository;
import com.example.android.testing.notes.model.InMemoryNotesRepository;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.verify;

public class NotesRepositoryTest {

    @Mock
    private NotesServiceApiImpl mServiceApi;

    private NotesRepository mNotesRepository;

    @Before
    public void setupNotesRepository() {
        MockitoAnnotations.initMocks(this);
        mNotesRepository = new InMemoryNotesRepository(mServiceApi);
    }

    @Test
    public void getNotes_RequestsAllNotesFromServiceApi() {
        mNotesRepository.getNotes();
        verify(mServiceApi).getAllNotes();
    }
}
