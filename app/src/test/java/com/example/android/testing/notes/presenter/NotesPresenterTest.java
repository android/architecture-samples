package com.example.android.testing.notes.presenter;

import com.example.android.testing.notes.model.Note;
import com.example.android.testing.notes.model.NotesRepository;
import com.example.android.testing.notes.view.NotesView;
import com.google.common.collect.Lists;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class NotesPresenterTest {

    private static List<Note> NOTES = Lists.newArrayList(new Note("Title1", "Description1"),
            new Note("Title2", "Description2"));

    private static List<Note> EMPTY_NOTES = Lists.newArrayList();

    @Mock
    private NotesRepository mNotesRepository;

    @Mock
    private NotesView mNotesView;

    private NotesPresenter mNotesPresenter;

    @Before
    public void setupNotesPresenter() {
        MockitoAnnotations.initMocks(this);
        mNotesPresenter = new NotesPresenterImpl(mNotesRepository, mNotesView);
    }

    @Test
    public void loadNotesFromRepositoryAndLoadIntoView() {
        // Given an initialized NotesPresenterImpl with initialized notes
        when(mNotesRepository.getNotes()).thenReturn(NOTES);
        // When loading of Notes is requested
        mNotesPresenter.loadNotes();
        // Then verify that Notes List View was updated
        verify(mNotesRepository).getNotes();
        verify(mNotesView).setProgressIndicator();
        verify(mNotesView).showNotes(NOTES);
    }

    @Test
    public void emptyNotes_showsEmptyNotesPlaceholder() {
        // Given an initialized NotesPresenterImpl with empty notes
        when(mNotesRepository.getNotes()).thenReturn(EMPTY_NOTES);
        // When loading of Notes is requested
        mNotesPresenter.loadNotes();
        // Then verify that empty placeholder is shown
        verify(mNotesRepository).getNotes();
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
        verify(mNotesView).showNoteDetailUi();
    }
}
