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

package com.example.android.testing.notes.addnote;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.example.android.testing.notes.Injection;
import com.example.android.testing.notes.R;
import com.example.android.testing.notes.util.EspressoIdlingResource;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
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
 * Main UI for the add note screen. Users can enter a note title and description. Images can be
 * added to notes by clicking on the options menu.
 */
public class AddNoteFragment extends Fragment implements AddNoteContract.View {

    public static final int REQUEST_CODE_IMAGE_CAPTURE = 0;
    public static final int ADD_PHOTO_MENU_ITEM_ID = 1;
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 2;

    private AddNoteContract.UserActionsListener mActionListener;

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
        mActionListener = new AddNotePresenter(Injection.provideNotesRepository(), this,
                Injection.provideImageFile());

        FloatingActionButton fab =
                (FloatingActionButton) getActivity().findViewById(R.id.fab_add_notes);
        fab.setImageResource(R.drawable.ic_done);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActionListener.saveNote(mTitle.getText().toString(),
                        mDescription.getText().toString());
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_addnote, container, false);
        mTitle = (TextView) root.findViewById(R.id.add_note_title);
        mDescription = (TextView) root.findViewById(R.id.add_note_description);
        mImageThumbnail = (ImageView) root.findViewById(R.id.add_note_image_thumbnail);

        setHasOptionsMenu(true);
        setRetainInstance(true);
        return root;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case ADD_PHOTO_MENU_ITEM_ID:
                if (ContextCompat.checkSelfPermission(getContext(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {
                    takePicture();
                    return true;
                } else {
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            REQUEST_WRITE_EXTERNAL_STORAGE);
                    return false;
                }
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
        getActivity().setResult(Activity.RESULT_OK);
        getActivity().finish();
    }

    @Override
    public void openCamera(String saveTo) {
        // Open the camera to take a picture.
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Check if there is a camera app installed to handle our Intent
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

        // The image is loaded in a different thread so in order to UI-test this, an idling resource
        // is used to specify when the app is idle.
        EspressoIdlingResource.increment(); // App is busy until further notice.

        // This app uses Glide for image loading
        Glide.with(this)
                .load(imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .into(new GlideDrawableImageViewTarget(mImageThumbnail) {
                    @Override
                    public void onResourceReady(GlideDrawable resource,
                                                GlideAnimation<? super GlideDrawable> animation) {
                        super.onResourceReady(resource, animation);
                        EspressoIdlingResource.decrement(); // Set app as idle.
                    }
                });
    }

    @Override
    public void showImageError() {
        Snackbar.make(mTitle, getString(R.string.cannot_connect_to_camera_message),
                Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void setUserActionListener(AddNoteContract.UserActionsListener listener) {
        mActionListener = listener;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // If an image is received, display it on the ImageView.
        if (REQUEST_CODE_IMAGE_CAPTURE == requestCode && Activity.RESULT_OK == resultCode) {
            mActionListener.imageAvailable();
        } else {
            mActionListener.imageCaptureFailed();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        if (REQUEST_WRITE_EXTERNAL_STORAGE == requestCode) {
            if (1 == grantResults.length && PackageManager.PERMISSION_GRANTED == grantResults[0]) {
                // Write external storage permission has been granted, preview can be displayed
                Snackbar.make(mTitle, R.string.permissions_granted, Snackbar.LENGTH_LONG)
                        .setAction(getString(R.string.add_picture), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                takePicture();
                            }
                        })
                        .show();
            } else {
                Snackbar.make(mTitle, getString(R.string.take_picture_error),
                        Snackbar.LENGTH_LONG).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void takePicture() {
        try {
            mActionListener.takePicture();
        } catch (IOException ioe) {
            if (getView() != null) {
                Snackbar.make(getView(), getString(R.string.take_picture_error),
                        Snackbar.LENGTH_LONG).show();
            }
        }
    }
}
