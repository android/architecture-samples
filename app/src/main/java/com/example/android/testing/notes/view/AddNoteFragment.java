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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.android.testing.notes.Injection;
import com.example.android.testing.notes.NotesActivity;
import com.example.android.testing.notes.R;
import com.example.android.testing.notes.presenter.AddNotePresenter;
import com.example.android.testing.notes.presenter.AddNotePresenterImpl;
import com.example.android.testing.notes.util.ActivityUtils;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;

import static com.google.common.base.Preconditions.checkState;

/**
 * TODO javadoc
 */
public class AddNoteFragment extends Fragment implements AddNoteView {

    public static final int REQUEST_CODE_IMAGE_CAPTURE = 1;
    public static final int ADD_PHOTO_MENU_ITEM_ID = 0x1001;

    private AddNotePresenter mAddNotePresenter;

    private TextView mTitle;

    private TextView mDescription;

    private ImageView mImageThumbnail;

    public static AddNoteFragment newInstance() {
        return new AddNoteFragment();
    }

    public AddNoteFragment() {
        // Required empty public constructor
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mAddNotePresenter = new AddNotePresenterImpl(Injection.provideNotesRepository(), this,
                Injection.provideImageFile());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_addnote, container, false);
        mTitle = (TextView) root.findViewById(R.id.add_note_title);
        mDescription = (TextView) root.findViewById(R.id.add_note_description);
        mImageThumbnail = (ImageView) root.findViewById(R.id.add_note_image_thumbnail);

        FloatingActionButton fab =
                (FloatingActionButton) getActivity().findViewById(R.id.fab_notes);
        fab.setImageResource(R.drawable.ic_done);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAddNotePresenter.saveNote(mTitle.getText().toString(),
                        mDescription.getText().toString());
            }
        });
        setHasOptionsMenu(true);
        return root;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case ADD_PHOTO_MENU_ITEM_ID:
                try {
                    mAddNotePresenter.takePicture();
                } catch (IOException ioe) {
                    if (getView() != null) {
                        Snackbar.make(getView(), getString(R.string.take_picture_error),
                                Snackbar.LENGTH_LONG).show();
                    }
                }
                return true;
        }
        return false;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(Menu.NONE, ADD_PHOTO_MENU_ITEM_ID, Menu.NONE, R.string.add_picture);
        super.onCreateOptionsMenu(menu, inflater);
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

    @Override
    public void openCamera(String saveTo) {
        // Open the camera to take a picture.
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getContext().getPackageManager()) != null) {
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.parse(saveTo));
            startActivityForResult(takePictureIntent, REQUEST_CODE_IMAGE_CAPTURE);
        } else {
            Snackbar.make(mTitle, getString(R.string.cannot_connect_to_camera_message),
                    Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    public void showImagePreview(@NonNull String imageUrl) {
        checkState(!TextUtils.isEmpty(imageUrl), "imageUrl cannot be null or empty!");
        mImageThumbnail.setVisibility(View.VISIBLE);
        Glide.with(this)
                .load(imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .into(mImageThumbnail);
    }

    @Override
    public void showImageError() {
        Snackbar.make(mTitle, getString(R.string.cannot_connect_to_camera_message),
                Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // If an image is received, display it on the ImageView.
        if (REQUEST_CODE_IMAGE_CAPTURE == requestCode && Activity.RESULT_OK == resultCode) {
            mAddNotePresenter.imageAvailable();
        } else {
            mAddNotePresenter.imageCaptureFailed();
        }
    }
}
