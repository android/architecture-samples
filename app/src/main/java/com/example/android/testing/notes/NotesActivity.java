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

import com.example.android.testing.notes.util.ActivityUtils;
import com.example.android.testing.notes.view.AddNoteFragment;
import com.example.android.testing.notes.view.NotesFragment;

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

public class NotesActivity extends AppCompatActivity {

    private static final String ADD_NOTE_FRAGMENT_TAG = "ADD_NOTE_FRAGMENT_TAG";

    private static final String NOTES_FRAGMENT_TAG = "NOTES_FRAGMENT_TAG";

    private DrawerLayout mDrawerLayout;

    private NotesFragment mNotesFragment;

    private AddNoteFragment mAddNotesFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);


        FragmentManager fm = getSupportFragmentManager();
        mNotesFragment = (NotesFragment) fm.findFragmentByTag(NOTES_FRAGMENT_TAG);
        mAddNotesFragment = (AddNoteFragment) fm.findFragmentByTag(ADD_NOTE_FRAGMENT_TAG);

        if (mNotesFragment == null) {
            mNotesFragment = NotesFragment.newInstance();
            initFragment(mNotesFragment);
        }

        // TODO create a presenter and view for this logic and wire it up (Toolbar + Drawer)
        // + write unit and UI tests.
        // Set up the toolbar.
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.ic_menu);
        ab.setDisplayHomeAsUpEnabled(true);

        // Set up the navigation drawer.
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setStatusBarBackground(R.color.colorPrimaryDark);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {
            setupDrawerContent(navigationView);
        }

    }

    private void initFragment(Fragment notesFragment) {
        // Add the NotesView to the layout
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.contentFrame, notesFragment);
        transaction.commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Open the navigation drawer when the home icon is selected from the toolbar.
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            // TODO figure out what we want to do for the drawer
                            case R.id.drawer_home:
                                Toast.makeText(NotesActivity.this, "Notes", Toast.LENGTH_SHORT)
                                        .show();
                                break;
                            case R.id.drawer_statistics:
                                Toast.makeText(NotesActivity.this, "Statistics", Toast.LENGTH_SHORT)
                                        .show();
                                break;
                        }
                        // Close the navigation drawer when an item is selected.
                        menuItem.setChecked(true);
                        mDrawerLayout.closeDrawers();
                        return true;
                    }
                });
    }

    public void showAddNoteFragment() {
        mAddNotesFragment = AddNoteFragment.newInstance();
        ActivityUtils.showFragment(getSupportFragmentManager(), R.id.contentFrame,
                mAddNotesFragment, ADD_NOTE_FRAGMENT_TAG, true);
    }

    public void showNotesFragment() {
        mAddNotesFragment = null;
        ActivityUtils.showFragment(getSupportFragmentManager(), R.id.contentFrame,
                mNotesFragment, NOTES_FRAGMENT_TAG, false);
    }

}
