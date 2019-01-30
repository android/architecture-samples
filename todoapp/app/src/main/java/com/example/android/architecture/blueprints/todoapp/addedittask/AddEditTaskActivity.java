/*
 * Copyright 2016, The Android Open Source Project
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

package com.example.android.architecture.blueprints.todoapp.addedittask;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.android.architecture.blueprints.todoapp.R;
import com.example.android.architecture.blueprints.todoapp.ViewModelFactory;
import com.example.android.architecture.blueprints.todoapp.util.ActivityUtils;

/**
 * Displays an add or edit task screen.
 */
public class AddEditTaskActivity extends AppCompatActivity implements AddEditTaskNavigator {

    public static final int REQUEST_CODE = 1;

    public static final int ADD_EDIT_RESULT_OK = RESULT_FIRST_USER + 1;

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onTaskSaved() {
        setResult(ADD_EDIT_RESULT_OK);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addtask_act);

        // Set up the toolbar.
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        AddEditTaskFragment addEditTaskFragment = obtainViewFragment();

        ActivityUtils.replaceFragmentInActivity(getSupportFragmentManager(),
                addEditTaskFragment, R.id.contentFrame);

        subscribeToNavigationChanges();
    }

    private void subscribeToNavigationChanges() {
        AddEditTaskViewModel viewModel = obtainViewModel(this);

        // The activity observes the navigation events in the ViewModel
        viewModel.getTaskUpdatedEvent().observe(this, new Observer<Void>() {
            @Override
            public void onChanged(@Nullable Void _) {
                AddEditTaskActivity.this.onTaskSaved();
            }
        });
    }

    public static AddEditTaskViewModel obtainViewModel(FragmentActivity activity) {
        // Use a Factory to inject dependencies into the ViewModel
        ViewModelFactory factory = ViewModelFactory.getInstance(activity.getApplication());

        return ViewModelProviders.of(activity, factory).get(AddEditTaskViewModel.class);
    }

    @NonNull
    private AddEditTaskFragment obtainViewFragment() {
        // View Fragment
        AddEditTaskFragment addEditTaskFragment = (AddEditTaskFragment) getSupportFragmentManager()
                .findFragmentById(R.id.contentFrame);

        if (addEditTaskFragment == null) {
            addEditTaskFragment = AddEditTaskFragment.newInstance();

            // Send the task ID to the fragment
            Bundle bundle = new Bundle();
            bundle.putString(AddEditTaskFragment.ARGUMENT_EDIT_TASK_ID,
                    getIntent().getStringExtra(AddEditTaskFragment.ARGUMENT_EDIT_TASK_ID));
            addEditTaskFragment.setArguments(bundle);
        }
        return addEditTaskFragment;
    }
}
