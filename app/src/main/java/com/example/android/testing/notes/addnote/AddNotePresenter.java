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

package com.example.android.testing.notes.addnote;

import com.example.android.testing.notes.data.Note;
import com.example.android.testing.notes.data.NotesRepository;
import com.example.android.testing.notes.util.ImageFile;

import android.support.annotation.NonNull;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Listens to user actions from the UI ({@link AddNoteFragment}), retrieves the data and updates
 * the UI as required.
 */
public class AddNotePresenter implements AddNoteContract.UserActionsListener {

    @NonNull
    private final NotesRepository mNotesRepository;
    @NonNull
    private final AddNoteContract.View mAddNoteView;
    @NonNull
    private final ImageFile mImageFile;

    public AddNotePresenter(@NonNull NotesRepository notesRepository,
                            @NonNull AddNoteContract.View addNoteView,
                            @NonNull ImageFile imageFile) {
        mNotesRepository = checkNotNull(notesRepository);
        mAddNoteView = checkNotNull(addNoteView);
        addNoteView.setUserActionListener(this);
        mImageFile = imageFile;
    }

    @Override
    public void saveNote(String title, String description) {
        String imageUrl = null;
        if (mImageFile.exists()) {
            imageUrl = mImageFile.getPath();
        }
        Note newNote = new Note(title, description, imageUrl);
        if (newNote.isEmpty()) {
            mAddNoteView.showEmptyNoteError();
        } else {
            mNotesRepository.saveNote(newNote);
            mAddNoteView.showNotesList();
        }
    }

    @Override
    public void takePicture() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        mImageFile.create(imageFileName, ".jpg");
        mAddNoteView.openCamera(mImageFile.getPath());
    }

    @Override
    public void imageAvailable() {
        if (mImageFile.exists()) {
            mAddNoteView.showImagePreview(mImageFile.getPath());
        } else {
            imageCaptureFailed();
        }
    }

    @Override
    public void imageCaptureFailed() {
        captureFailed();
    }

    private void captureFailed() {
        mImageFile.delete();
        mAddNoteView.showImageError();
    }

}
