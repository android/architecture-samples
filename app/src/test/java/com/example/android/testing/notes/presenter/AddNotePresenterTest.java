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
import com.example.android.testing.notes.util.ImageFile;
import com.example.android.testing.notes.view.AddNoteView;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.contains;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * TODO: javadoc
 */
public class AddNotePresenterTest {

    @Mock
    private NotesRepository mNotesRepository;

    @Mock
    private ImageFile mImageFile;

    @Mock
    private AddNoteView mAddNoteView;

    private AddNotePresenterImpl mAddNotesPresenter;

    @Before
    public void setupAddNotePresenter() {
        MockitoAnnotations.initMocks(this);
        mAddNotesPresenter = new AddNotePresenterImpl(mNotesRepository, mAddNoteView, mImageFile);
    }

    @Test
    public void saveNoteToRepository_showsSuccessMessageUi() {
        mAddNotesPresenter.saveNote("New Note Title", "Some Note Description");
        verify(mNotesRepository).saveNote(any(Note.class));
        verify(mAddNoteView).showNotesList();
    }

    @Test
    public void saveNote_WithImage_SavesNoteToRepository() {
        // TODO: add captor which checks if images is in Note when image present in file.
    }

    @Test
    public void saveNote_emptyNoteShowsErrorUi() {
        mAddNotesPresenter.saveNote("", "");
        verify(mAddNoteView).showEmptyNoteError();
    }

    @Test
    public void takePicture_CreatesFileAndOpensCamera() throws IOException {
        mAddNotesPresenter.takePicture();
        verify(mImageFile).create(anyString(), anyString());
        verify(mImageFile).getPath();
        verify(mAddNoteView).openCamera(anyString());
    }

    @Test
    public void imageAvailable_SavesImageAndUpdatesUiWithThumbnail() {
        final String imageUrl = "path/to/file";
        when(mImageFile.exists()).thenReturn(true);
        when(mImageFile.getPath()).thenReturn(imageUrl);
        mAddNotesPresenter.imageAvailable();
        verify(mAddNoteView).showImagePreview(contains(imageUrl));
    }

    @Test
    public void imageAvailable_FileDoesNotExistShowsErrorUi() {
        when(mImageFile.exists()).thenReturn(false);
        mAddNotesPresenter.imageAvailable();
        verify(mAddNoteView).showImageError();
        verify(mImageFile).delete();
    }

    @Test
    public void noImageAvailable_ShowsErrorUi() {
        mAddNotesPresenter.imageCaptureFailed();
        verify(mAddNoteView).showImageError();
        verify(mImageFile).delete();
    }

}
