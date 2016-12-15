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

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.android.architecture.blueprints.todoapp.R;
import com.example.android.architecture.blueprints.todoapp.SnackBarChangedCallback;
import com.example.android.architecture.blueprints.todoapp.databinding.AddtaskFragBinding;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Main UI for the add task screen. Users can enter a task title and description.
 */
public class AddEditTaskFragment extends Fragment {

    public static final String ARGUMENT_EDIT_TASK_ID = "EDIT_TASK_ID";

    private AddEditTaskViewModel mViewModel;

    private SnackBarChangedCallback mSnackBarChangedCallback;

    public static AddEditTaskFragment newInstance() {
        return new AddEditTaskFragment();
    }

    public AddEditTaskFragment() {
        // Required empty public constructor
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getArguments() != null) {
            mViewModel.start(getArguments().getString(ARGUMENT_EDIT_TASK_ID));
        } else {
            mViewModel.start(null);
        }
    }

    public void setViewModel(@NonNull AddEditTaskViewModel viewModel) {
        mViewModel = checkNotNull(viewModel);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // FAB setup
        FloatingActionButton fab =
                (FloatingActionButton) getActivity().findViewById(R.id.fab_edit_task_done);
        fab.setImageResource(R.drawable.ic_done);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewModel.saveTask(mViewModel.title.get(), mViewModel.description.get());
            }
        });

        // SnackBar setup
        mSnackBarChangedCallback = new SnackBarChangedCallback(getView(), mViewModel);
        mViewModel.snackBarText.addOnPropertyChangedCallback(mSnackBarChangedCallback);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.addtask_frag, container, false);
        AddtaskFragBinding mViewDataBinding = AddtaskFragBinding.bind(root);

        mViewDataBinding.setViewmodel(mViewModel);

        setHasOptionsMenu(true);
        setRetainInstance(true);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mViewModel.snackBarText.removeOnPropertyChangedCallback(mSnackBarChangedCallback);
    }
}

