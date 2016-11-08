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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.example.android.architecture.blueprints.todoapp.R;
import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskActivity;
import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskFragment;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Main UI for the task detail screen.
 */
public class TaskDetailFragment extends Fragment implements TaskDetailContract.View {

    public static final int REQUEST_EDIT_TASK = 1;

    private TaskDetailContract.Presenter mPresenter;

    private TextView mDetailTitle;

    private TextView mDetailDescription;

    private CheckBox mDetailCompleteStatus;

    private View mDetailLayout;

    public static TaskDetailFragment newInstance() {
        return new TaskDetailFragment();
    }

    @Override
    public void setPresenter(@NonNull TaskDetailContract.Presenter presenter) {
        mPresenter = checkNotNull(presenter);
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.loadTask();
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
        mDetailLayout = root.findViewById(R.id.task_detail_layout);

        // Set up floating action button
        FloatingActionButton fab =
                (FloatingActionButton) getActivity().findViewById(R.id.fab_edit_task);
        if (fab != null) { // Fab is null in tablet mode
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mPresenter.editTask();
                }
            });
        }

        return root;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_delete:
                mPresenter.deleteTask();
                return true;
            case R.id.menu_edit: // tablet mode only
                mPresenter.editTask();
                return true;
        }
        return false;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.taskdetail_fragment_menu, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        boolean showMenuOptions = mPresenter.getDetailTaskId() != null;
        menu.findItem(R.id.menu_delete).setVisible(showMenuOptions);
        // "Edit" is only visible on tablet
        MenuItem menuEditItem = menu.findItem(R.id.menu_edit);
        if (menuEditItem != null) {
            menuEditItem.setVisible(showMenuOptions);
        }

        super.onPrepareOptionsMenu(menu);
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
    public void showCompletionStatus(final boolean complete) {
        mDetailCompleteStatus.setChecked(complete);
        mDetailCompleteStatus.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            mPresenter.completeTask();
                        } else {
                            mPresenter.activateTask();
                        }
                    }
                });
    }

    @Override
    public void showEditTask(String taskId) {
        Intent intent = new Intent(getContext(), AddEditTaskActivity.class);
        intent.putExtra(AddEditTaskFragment.ARGUMENT_EDIT_TASK_ID, taskId);
        startActivityForResult(intent, REQUEST_EDIT_TASK);
    }

    @Override
    public void showTaskDeleted() {
        // Close the activity if not in tablet mode.
        if (getFragmentManager().findFragmentById(R.id.contentFrame_detail) == null) {
            getActivity().finish();
        }
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
    public void updateMenuOptions() {
        getActivity().supportInvalidateOptionsMenu();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_EDIT_TASK) {
            // If the task was edited successfully, go back to the list.
            if (resultCode == Activity.RESULT_OK) {
                if (getFragmentManager().findFragmentById(R.id.contentFrame_detail) == null) {
                    getActivity().finish();
                }
            }
        }
    }

    @Override
    public void showTitle(String title) {
        mDetailTitle.setText(title);
        mDetailTitle.setVisibility(View.VISIBLE);
        mDetailLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void showDescription(String description) {
        mDetailDescription.setText(description);
        mDetailDescription.setVisibility(View.VISIBLE);
        mDetailLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void showMissingTask() {
        mDetailTitle.setText("");
        mDetailDescription.setText("");
        mDetailLayout.setVisibility(View.GONE);
        getActivity().supportInvalidateOptionsMenu();
    }

    @Override
    public boolean isActive() {
        return isAdded();
    }

    public TaskDetailContract.Presenter getPresenter() {
        return mPresenter;
    }
}
