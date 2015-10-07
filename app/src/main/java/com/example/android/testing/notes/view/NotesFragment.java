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

import com.example.android.testing.notes.NotesActivity;
import com.example.android.testing.notes.R;
import com.example.android.testing.notes.model.Note;
import com.example.android.testing.notes.presenter.NotesPresenter;
import com.example.android.testing.notes.presenter.NotesPresenterImpl;
import com.example.android.testing.notes.util.ActivityUtils;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Display a grid of {@link Note}s
 */
public class NotesFragment extends BaseFragment implements NotesView {

    private NotesPresenter mNotesPresenter;

    private RecyclerView mRecyclerView;

    private NotesAdapter mListAdapter;

    public NotesFragment() {
        // Requires empty public constructor
    }

    public static NotesFragment newInstance() {
        return new NotesFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mListAdapter = new NotesAdapter(new ArrayList<Note>(0));
    }

    @Override
    public void onResume() {
        super.onResume();
        mNotesPresenter.loadNotes();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mNotesPresenter = new NotesPresenterImpl(getNotesRepository(), this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_notes, container, false);
        mRecyclerView = (RecyclerView) root.findViewById(R.id.notes_list);
        mRecyclerView.setAdapter(mListAdapter);

        int numColumns = getContext().getResources().getInteger(R.integer.num_notes_columns);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), numColumns));

        // Set up floating action button
        FloatingActionButton fab = (FloatingActionButton) root.findViewById(R.id.fab_add_note);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNotesPresenter.addNewNote();
            }
        });

        return root;
    }

    @Override
    public void setProgressIndicator() {
        // TODO add a progress indicator
    }

    public void showNotes(List<Note> notes) {
        mListAdapter.replaceData(notes);
    }

    @Override
    public void showAddNote() {
        NotesActivity notesActivity = ActivityUtils.cast(getActivity());
        notesActivity.showAddNoteFragment();
    }

    @Override
    public void showNotesEmptyPlaceholder() {
        // TODO add placeholder asset when no data is available
    }

    @Override
    public void showNoteDetailUi() {
        // TODO implement show detail note feature, please implement this in a fragment but hosted
        // in it's own Activity, since it makes more sense that way and it gives us the flexibility
        // to show some Intent stubbing.
    }

    //TODO wire up a listener to make items clickable
    private static class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.ViewHolder> {

        private List<Note> mNotes;

        public NotesAdapter(List<Note> notes) {
            mNotes = checkNotNull(notes);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);
            View noteView = inflater.inflate(R.layout.item_note, parent, false);

            ViewHolder holder = new ViewHolder(noteView);
            return holder;
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, int position) {
            Note note = mNotes.get(position);

            viewHolder.title.setText(note.getTitle());
            viewHolder.description.setText(note.getDescription());
        }

        public void replaceData(List<Note> notes) {
            mNotes = notes;
            notifyDataSetChanged();
        }

        @Override
        public int getItemCount() {
            return mNotes.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            public TextView title;

            public TextView description;

            public ViewHolder(View itemView) {
                super(itemView);
                title = (TextView) itemView.findViewById(R.id.note_title);
                description = (TextView) itemView.findViewById(R.id.note_description);
            }
        }
    }

}
