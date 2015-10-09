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

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.example.android.testing.notes.NotesActivity;
import com.example.android.testing.notes.R;
import com.example.android.testing.notes.model.Note;
import com.example.android.testing.notes.presenter.AddNotePresenter;
import com.example.android.testing.notes.presenter.AddNotePresenterImpl;
import com.example.android.testing.notes.util.ActivityUtils;

/**
 * TODO javadoc
 */
public class AddNoteFragment extends BaseFragment implements AddNoteView {

    private AddNotePresenter mAddNotePresenter;

    private TextView mTitle;

    private TextView mDescription;

    public static AddNoteFragment newInstance() {
        return new AddNoteFragment();
    }

    public AddNoteFragment() {
        // Required empty public constructor
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mAddNotePresenter = new AddNotePresenterImpl(getNotesRepository(), this);

        // Set the focus on the title field and open keyboard.
        mTitle.setFocusableInTouchMode(true);
        mTitle.requestFocus();
        InputMethodManager imm =
                (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mTitle, InputMethodManager.SHOW_IMPLICIT);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_addnote, container, false);
        mTitle = (TextView) root.findViewById(R.id.add_note_title);
        mDescription = (TextView) root.findViewById(R.id.add_note_description);

        FloatingActionButton fab =
                (FloatingActionButton) getActivity().findViewById(R.id.fab_notes);
        fab.setImageResource(R.drawable.ic_done);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: add image parameter to this.
                mAddNotePresenter.saveNote(new Note(mTitle.getText().toString(),
                        mDescription.getText().toString()));
            }
        });

        return root;
    }

    @Override
    public void showEmptyNoteError() {
        Snackbar.make(mTitle, getString(R.string.empty_note_message), Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void showNotesList() {
        Snackbar.make(mTitle, getString(R.string.successfully_saved_note_message),
                Snackbar.LENGTH_SHORT).show();
        ActivityUtils.<NotesActivity>cast(getActivity()).showNotesFragment();
    }
}
