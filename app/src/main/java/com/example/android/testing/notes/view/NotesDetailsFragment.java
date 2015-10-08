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

package com.example.android.testing.notes.view;

import com.example.android.testing.notes.R;
import com.example.android.testing.notes.presenter.NoteDetailPresenter;
import com.example.android.testing.notes.presenter.NoteDetailPresenterImpl;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class NotesDetailsFragment extends BaseFragment implements NoteDetailsView {

    public static final String ARGUMENT_NOTE_ID = "NOTE_ID";

    private NoteDetailPresenter mNoteDetailPresenter;

    private TextView mTitle;

    private TextView mDescription;

    public static NotesDetailsFragment newInstance(String noteId) {
        Bundle arguments = new Bundle();
        arguments.putString(ARGUMENT_NOTE_ID, noteId);
        NotesDetailsFragment fragment = new NotesDetailsFragment();
        fragment.setArguments(arguments);

        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mNoteDetailPresenter = new NoteDetailPresenterImpl(getNotesRepository(), this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_detail, container, false);
        mTitle = (TextView) root.findViewById(R.id.note_title);
        mDescription = (TextView) root.findViewById(R.id.note_description);
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        final String noteId = getArguments().getString(ARGUMENT_NOTE_ID);
        mNoteDetailPresenter.openNote(noteId);
    }

    @Override
    public void setProgressIndicator(boolean active) {
        // TODO
        if (active) {
            mTitle.setText("");
            mDescription.setText("LOADING");
        }

    }

    @Override
    public void hideDescription() {
        mDescription.setVisibility(View.GONE);
    }

    @Override
    public void hideTitle() {
        mTitle.setVisibility(View.GONE);
    }

    @Override
    public void showDescription(String description) {
        mDescription.setVisibility(View.VISIBLE);
        mDescription.setText(description);
    }

    @Override
    public void showTitle(String title) {
        mTitle.setVisibility(View.VISIBLE);
        mTitle.setText(title);
    }

    @Override
    public void showMissingNote() {
        // TODO
        mTitle.setText("");
        mDescription.setText("No data");
    }
}
