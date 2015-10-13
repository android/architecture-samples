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

import android.support.annotation.NonNull;

import java.io.File;

/**
 * TODO: javadoc
 */
public class NoteDetailPresenterImpl implements NoteDetailPresenter {

    private final NotesRepository mNotesRepository;

    private final NoteDetailsView mNotesDetailView;

    public NoteDetailPresenterImpl(NotesRepository notesRepository,
                                   NoteDetailsView noteDetailsView) {
        mNotesRepository = notesRepository;
        mNotesDetailView = noteDetailsView;
    }

    @Override
    public void openNote(String noteId) {
        if (noteId == null || noteId.isEmpty()) {
            mNotesDetailView.showMissingNote();
            return;
        }

        mNotesDetailView.setProgressIndicator(true);
        mNotesRepository.getNote(noteId, new NotesRepository.GetNoteCallback() {
            @Override
            public void onNoteLoaded(Note note) {
                mNotesDetailView.setProgressIndicator(false);
                if (note == null) {
                    mNotesDetailView.showMissingNote();
                } else {
                    showNote(note);
                }
            }
        });
    }

    private void showNote(@NonNull Note note) {
        final String title = note.getTitle();
        final String description = note.getDescription();

        if (title.isEmpty()) {
            mNotesDetailView.hideTitle();
        } else {
            mNotesDetailView.showTitle(title);
        }

        if (description.isEmpty()) {
            mNotesDetailView.hideDescription();
        } else {
            mNotesDetailView.showDescription(description);
        }

        if (note.getImageUrl() != null) {
            File imageFile = new File(note.getImageUrl());
            if (imageFile.exists()) {
                mNotesDetailView.showImage(imageFile);
            } else {
                mNotesDetailView.hideImage();
            }
        } else {
            mNotesDetailView.hideImage();
        }

    }
}
