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

import android.support.annotation.NonNull;

import static com.google.common.base.Preconditions.checkNotNull;

public class NoteRepositories {

    private NoteRepositories() {
        // no instance
    }

    private static NotesRepository repository = null;

    public synchronized static NotesRepository getInMemoryRepoInstance(@NonNull NotesServiceApi notesServiceApi) {
        checkNotNull(notesServiceApi);
        if (null == repository) {
            repository = new InMemoryNotesRepository(notesServiceApi);
        }
        return repository;
    }
}