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
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.architecture.blueprints.todoapp.Injection;
import com.example.android.architecture.blueprints.todoapp.R;
import com.example.android.architecture.blueprints.todoapp.data.Task;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Main UI for the add task screen. Users can enter a task title and description.
 */
public class AddEditTaskFragment extends Fragment {

    private static final String TAG = AddEditTaskFragment.class.getSimpleName();

    public static final String ARGUMENT_EDIT_TASK_ID = "EDIT_TASK_ID";

    private TextView mTitle;

    private TextView mDescription;

    private AddEditTaskViewModel mViewModel;

    private CompositeSubscription mSubscription = new CompositeSubscription();

    public static AddEditTaskFragment newInstance(String taskId) {
        Bundle arguments = new Bundle();
        arguments.putString(ARGUMENT_EDIT_TASK_ID, taskId);
        AddEditTaskFragment fragment = new AddEditTaskFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        bindViewModel();
    }

    @Override
    public void onPause() {
        unbindViewModel();
        super.onPause();
    }

    private void bindViewModel() {
        mSubscription = new CompositeSubscription();

        mSubscription.add(mViewModel.getSnackbarText()
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        // onNext
                        this::showSnackbar,
                        // onError
                        __ -> Log.e(TAG, "Error retrieving snackbar text")));

        mSubscription.add(mViewModel.getTask()
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        // onNext
                        this::setTask,
                        // onError
                        __ -> Log.e(TAG, "Error retrieving the task")));

    }

    private void unbindViewModel() {
        mSubscription.unsubscribe();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.addtask_frag, container, false);
        mTitle = (TextView) root.findViewById(R.id.add_task_title);
        mDescription = (TextView) root.findViewById(R.id.add_task_description);
        setHasOptionsMenu(true);

        setupFab();

        mViewModel = Injection.provideAddEditTaskViewModel(getTaskId(), getActivity());

        return root;
    }

    private void setupFab() {
        FloatingActionButton fab =
                (FloatingActionButton) getActivity().findViewById(R.id.fab_edit_task_done);
        fab.setImageResource(R.drawable.ic_done);
        fab.setOnClickListener(__ -> mViewModel.saveTask(mTitle.getText().toString(),
                mDescription.getText().toString()));
    }

    private void showSnackbar(@StringRes Integer textId) {
        Snackbar.make(mTitle, textId, Snackbar.LENGTH_LONG).show();
    }

    private void setTask(Task task) {
        setTitle(task.getTitle());
        setDescription(task.getDescription());
    }

    private void setTitle(String title) {
        mTitle.setText(title);
    }

    private void setDescription(String description) {
        mDescription.setText(description);
    }

    private String getTaskId() {
        if (getArguments() != null) {
            return getArguments().getString(ARGUMENT_EDIT_TASK_ID);
        }
        return null;

    }
}
