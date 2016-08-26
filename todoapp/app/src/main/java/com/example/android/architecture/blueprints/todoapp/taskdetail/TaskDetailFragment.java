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

package com.example.android.architecture.blueprints.todoapp.taskdetail;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.example.android.architecture.blueprints.todoapp.BasePresenter;
import com.example.android.architecture.blueprints.todoapp.R;
import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskActivity;
import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskFragment;
import com.jakewharton.rxbinding.view.RxMenuItem;
import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxCompoundButton;
import com.jakewharton.rxrelay.PublishRelay;

import rx.Observable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Main UI for the task detail screen.
 */
public class TaskDetailFragment extends Fragment implements TaskDetailContract.View {

    public static final String ARGUMENT_TASK_ID = "TASK_ID";

    public static final int REQUEST_EDIT_TASK = 1;

    private BasePresenter mPresenter;

    private TextView mDetailTitle;

    private TextView mDetailDescription;

    private CheckBox mDetailCompleteStatus;

    private PublishRelay<Void> mDeleteTaskClick = PublishRelay.create();

    private Observable<Void> mEditTaskClick;

    private Observable<Boolean> mCheckedChanges;

    // When mUpdating is true, mCheckedChanges won't emit.
    // Stops model changes events from causing loop-backs.
    private boolean mUpdating = false;

    public static TaskDetailFragment newInstance(String taskId) {
        Bundle arguments = new Bundle();
        arguments.putString(ARGUMENT_TASK_ID, taskId);
        TaskDetailFragment fragment = new TaskDetailFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.subscribe();
    }

    @Override
    public void onPause() {
        super.onPause();

        mPresenter.unsubscribe();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.taskdetail_frag, container, false);
        setHasOptionsMenu(true);
        mDetailTitle = (TextView) root.findViewById(R.id.task_detail_title);
        mDetailDescription = (TextView) root.findViewById(R.id.task_detail_description);
        mDetailCompleteStatus = (CheckBox) root.findViewById(R.id.task_detail_complete);

        // Set up floating action button
        FloatingActionButton fab =
                (FloatingActionButton) getActivity().findViewById(R.id.fab_edit_task);

        // RxBindings
        mEditTaskClick = RxView.clicks(fab);
        mCheckedChanges = RxCompoundButton
                .checkedChanges(mDetailCompleteStatus)
                .doOnNext(checked -> Log.i("TEST", "Pre-UpdatingFilter " + checked))
                .filter(checked -> !mUpdating)
                .share();

        return root;
    }

    @Override
    public void setPresenter(@NonNull BasePresenter presenter) {
        mPresenter = checkNotNull(presenter);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.taskdetail_fragment_menu, menu);

        // By using RxRelay, we can abstract Fragment's lifecycle implementation details.
        RxMenuItem
                .clicks(menu.findItem(R.id.menu_delete))
                .subscribe(mDeleteTaskClick);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void setLoadingIndicator(boolean active) {
        if (active) {
            mDetailTitle.setText("");
            mDetailDescription.setText(getString(R.string.loading));
        }
    }

    @Override
    public void hideDescription() {
        mDetailDescription.setVisibility(View.GONE);
    }

    @Override
    public void hideTitle() {
        mDetailTitle.setVisibility(View.GONE);
    }

    @Override
    public void showDescription(String description) {
        mDetailDescription.setVisibility(View.VISIBLE);
        mDetailDescription.setText(description);
    }

    @Override
    public void showCompletionStatus(final boolean complete) {
        mUpdating = true;
        mDetailCompleteStatus.setChecked(complete);
        mUpdating = false;
    }

    @Override
    public void showEditTask(String taskId) {
        Intent intent = new Intent(getContext(), AddEditTaskActivity.class);
        intent.putExtra(AddEditTaskFragment.ARGUMENT_EDIT_TASK_ID, taskId);
        startActivityForResult(intent, REQUEST_EDIT_TASK);
    }

    @Override
    public void showTaskDeleted() {
        getActivity().finish();
    }

    public void showTaskMarkedComplete() {
        Snackbar.make(getView(), getString(R.string.task_marked_complete), Snackbar.LENGTH_LONG)
                .show();
    }

    @Override
    public void showTaskMarkedActive() {
        Snackbar.make(getView(), getString(R.string.task_marked_active), Snackbar.LENGTH_LONG)
                .show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_EDIT_TASK) {
            // If the task was edited successfully, go back to the list.
            if (resultCode == Activity.RESULT_OK) {
                getActivity().finish();
                return;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void showTitle(String title) {
        mDetailTitle.setVisibility(View.VISIBLE);
        mDetailTitle.setText(title);
    }

    @Override
    public void showMissingTask() {
        mDetailTitle.setText("");
        mDetailDescription.setText(getString(R.string.no_data));
    }

    @Override
    public boolean isActive() {
        return isAdded();
    }

    @Override
    public Observable<Void> editTask() {
        return mEditTaskClick;
    }

    @Override
    public Observable<Void> deleteTask() {
        return mDeleteTaskClick;
    }

    @Override
    public Observable<Void> completeTask() {
        return mCheckedChanges
                .doOnNext(checked -> Log.i("TEST", "" + checked))
                .filter(checked -> checked)
                .map(checked -> (Void) null);
    }

    @Override
    public Observable<Void> activateTask() {
        return mCheckedChanges
                .doOnNext(checked -> Log.i("TEST", "" + checked))
                .filter(checked -> !checked)
                .map(checked -> (Void) null);
    }
}
