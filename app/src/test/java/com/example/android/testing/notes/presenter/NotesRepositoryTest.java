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
