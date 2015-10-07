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

import android.support.v4.util.ArrayMap;

import java.util.ArrayList;
import java.util.List;

// TODO ideally we would get this on a background thread, use a listener and simulate network latency
public class NotesServiceApiImpl implements NotesServiceApi {

    private static final ArrayMap NOTES_SERVICE_DATA = NotesServiceApiEndpoint.DATA;

    public List<Note> getAllNotes() {
        return new ArrayList<>(NOTES_SERVICE_DATA.values());
    }
}
