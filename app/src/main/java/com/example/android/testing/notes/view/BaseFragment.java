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

package com.example.android.testing.notes.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.example.android.testing.notes.model.NoteRepositories;
import com.example.android.testing.notes.model.NotesRepository;
import com.example.android.testing.notes.model.NotesServiceApiImpl;

/**
 * TODO javadoc
 */
public class BaseFragment extends Fragment {

    private NotesRepository mNotesRepository;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mNotesRepository = NoteRepositories
                .getInMemoryRepoInstance(new NotesServiceApiImpl());
    }

    protected NotesRepository getNotesRepository() {
        return mNotesRepository;
    }
}
