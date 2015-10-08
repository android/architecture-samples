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

import android.support.annotation.NonNull;

import com.example.android.testing.notes.model.Note;
import com.example.android.testing.notes.model.NotesRepository;
import com.example.android.testing.notes.view.AddNoteView;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * TODO: javadoc
 */
public class AddNotePresenterImpl implements AddNotePresenter {

    private final NotesRepository mNotesRepository;
    private final AddNoteView mAddNoteView;

    public AddNotePresenterImpl(@NonNull NotesRepository notesRepository,
                                @NonNull AddNoteView addNoteView) {
        mNotesRepository = checkNotNull(notesRepository);
        mAddNoteView = checkNotNull(addNoteView);
    }

    @Override
    public void saveNote(@NonNull Note note) {
        checkNotNull(note);
        if(note.isEmpty()) {
            mAddNoteView.showEmptyNoteError();
        } else {
            mNotesRepository.saveNote(note);
            mAddNoteView.showNotesList();
        }
    }

}
