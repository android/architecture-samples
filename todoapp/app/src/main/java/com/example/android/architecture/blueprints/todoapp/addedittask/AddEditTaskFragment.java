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

import android.arch.lifecycle.LifecycleFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.android.architecture.blueprints.todoapp.R;
import com.example.android.architecture.blueprints.todoapp.SnackbarMessage;
import com.example.android.architecture.blueprints.todoapp.databinding.AddtaskFragBinding;
import com.example.android.architecture.blueprints.todoapp.util.SnackbarUtils;

/**
 * Main UI for the add task screen. Users can enter a task title and description.
 */
public class AddEditTaskFragment extends LifecycleFragment {

    public static final String ARGUMENT_EDIT_TASK_ID = "EDIT_TASK_ID";

    private AddEditTaskViewModel mViewModel;

    private AddtaskFragBinding mViewDataBinding;

    public static AddEditTaskFragment newInstance() {
        return new AddEditTaskFragment();
    }

    public AddEditTaskFragment() {
        // Required empty public constructor
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setupFab();

        setupSnackbar();

        setupActionBar();

        loadData();
    }

    private void loadData() {
        // Add or edit an existing task?
        if (getArguments() != null) {
            mViewModel.start(getArguments().getString(ARGUMENT_EDIT_TASK_ID));
        } else {
            mViewModel.start(null);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.addtask_frag, container, false);
        if (mViewDataBinding == null) {
            mViewDataBinding = AddtaskFragBinding.bind(root);
        }

        mViewModel = AddEditTaskActivity.obtainViewModel(getActivity());

        mViewDataBinding.setViewmodel(mViewModel);

        setHasOptionsMenu(true);
        setRetainInstance(false);

        return mViewDataBinding.getRoot();
    }

    private void setupSnackbar() {
        mViewModel.getSnackbarMessage().observe(this, new SnackbarMessage.SnackbarObserver() {
            @Override
            public void onNewMessage(@StringRes int snackbarMessageResourceId) {
                SnackbarUtils.showSnackbar(getView(), getString(snackbarMessageResourceId));
            }
        });
    }

    private void setupFab() {
        FloatingActionButton fab =
                (FloatingActionButton) getActivity().findViewById(R.id.fab_edit_task_done);
        fab.setImageResource(R.drawable.ic_done);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewModel.saveTask(mViewModel.title.get(), mViewModel.description.get());
            }
        });
    }

    private void setupActionBar() {
        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (actionBar == null) {
            return;
        }
        if (getArguments() != null) {
            actionBar.setTitle(R.string.edit_task);
        } else {
            actionBar.setTitle(R.string.add_task);
        }
    }
}
