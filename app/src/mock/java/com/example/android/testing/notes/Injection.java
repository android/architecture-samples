/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.example.android.testing.notes;

import com.example.android.testing.notes.data.FakeNotesServiceApiImpl;
import com.example.android.testing.notes.data.NoteRepositories;
import com.example.android.testing.notes.data.NotesRepository;
import com.example.android.testing.notes.util.FakeImageFileImpl;
import com.example.android.testing.notes.util.ImageFile;

/**
 * Enables to inject a different implementation of {@link ImageFile} and
 * {@link NotesRepository} at runtime. This is used when testing, as this allows us to use
 * a fake implementation of the class to isolate the dependencies.
 */
public class Injection {

    public static ImageFile provideImageFile() {
        return new FakeImageFileImpl();
    }

    public static NotesRepository provideNotesRepository() {
        return NoteRepositories.getInMemoryRepoInstance(new FakeNotesServiceApiImpl());
    }
}
