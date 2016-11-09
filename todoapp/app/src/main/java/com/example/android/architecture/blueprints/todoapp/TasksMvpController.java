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

package com.example.android.architecture.blueprints.todoapp;

import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskFragment;
import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskPresenter;
import com.example.android.architecture.blueprints.todoapp.taskdetail.TaskDetailFragment;
import com.example.android.architecture.blueprints.todoapp.taskdetail.TaskDetailPresenter;
import com.example.android.architecture.blueprints.todoapp.tasks.TasksFilterType;
import com.example.android.architecture.blueprints.todoapp.tasks.TasksFragment;
import com.example.android.architecture.blueprints.todoapp.tasks.TasksPresenter;
import com.example.android.architecture.blueprints.todoapp.util.ActivityUtils;

import static com.example.android.architecture.blueprints.todoapp.util.ActivityUtils.isTablet;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Class that creates fragments (MVP views) and makes the necessary connections between them.
 */
public class TasksMvpController {

    public static final String ADD_EDIT_DIALOG_TAG = "ADD_EDIT_DIALOG_TAG";

    private final FragmentActivity mFragmentActivity;

    @Nullable
    private TasksTabletPresenter mTasksTabletPresenter;

    private TasksPresenter mTasksPresenter;

    //@Nullable
    //private AddEditTaskFragment mAddEditTaskFragment;

    // Force factory method, prevent direct instantiation:
    private TasksMvpController(@NonNull FragmentActivity fragmentActivity) {
        mFragmentActivity = fragmentActivity;
    }

    /**
     * Creates a controller for a task view for phones or tablets.
     *
     * @param fragmentActivity the context activity
     * @return a TasksMvpController
     */
    public static TasksMvpController createTasksView(@NonNull FragmentActivity fragmentActivity) {
        checkNotNull(fragmentActivity);

        TasksMvpController tasksMvpController = new TasksMvpController(fragmentActivity);

        tasksMvpController.initTasksView();
        return tasksMvpController;
    }

    /**
     * Shows the edit view for a new task.
     */
    public void addNewTask() {
        showAddNewTaskDialogForTablet(null);
    }

    /**
     * Shows the edit view for an existing task.
     * @param taskId The task ID or null if
     */
    public void editTask(@NonNull String taskId) {
        showAddNewTaskDialogForTablet(taskId);
    }

    /**
     * Sets the current task ID for detail, used in a configuration change (rotation, etc.)
     */
    public void restoreDetailTaskId(@Nullable String taskId) {
        if (isTablet(mFragmentActivity)) {
            assert mTasksTabletPresenter != null; // In tablet mode it's never null
            // Set the task ID on the detail presenter
            mTasksTabletPresenter.setDetailTaskId(taskId);
        }
    }

    /**
     * Sets the current task ID for add/edit, used in a configuration change (rotation, etc.)
     */
    public void restoreAddEditTaskId(@Nullable String taskId) {
        if (isTablet(mFragmentActivity)) {
            // If the add/edit dialog is shown, recreate presenter
            if (isAddEditDialogShown()) {
                createAddEditDialogElements(taskId, false);
            }
        }
    }

    public TasksFilterType getFiltering() {
        return mTasksPresenter.getFiltering();
    }

    public void setFiltering(@Nullable TasksFilterType filtering) {
        if (filtering != null) {
            mTasksPresenter.setFiltering(filtering);
        }
    }

    @Nullable
    public String getDetailTaskId() {
        if (mTasksTabletPresenter != null) {
            return mTasksTabletPresenter.getDetailTaskId();
        }
        return null;
    }

    @Nullable
    public String getAddEditTaskId() {
        if (mTasksTabletPresenter != null) {
            return mTasksTabletPresenter.getAddEditTaskId();
        }
        return null;
    }

    private void showAddNewTaskDialogForTablet(@Nullable String taskId) {
        AddEditTaskFragment addEditTaskFragment = createAddEditDialogElements(taskId, true);
        addEditTaskFragment.show(getSupportFragmentManager(), ADD_EDIT_DIALOG_TAG);
    }

    private AddEditTaskFragment createAddEditDialogElements(@Nullable String taskId, boolean shouldLoadDataFromRepo) {
        assert mTasksTabletPresenter != null; // In tablet mode it's never null
        AddEditTaskFragment addEditTaskFragment = findOrCreateAddEditTaskFragmentForTablet();
        AddEditTaskPresenter presenter = createAddEditTaskPresenter(taskId, addEditTaskFragment, shouldLoadDataFromRepo);
        mTasksTabletPresenter.setAddEditPresenter(presenter);
        addEditTaskFragment.setPresenter(mTasksTabletPresenter);
        return addEditTaskFragment;
    }

    private void initTasksView() {
        if (isTablet(mFragmentActivity)) {
            createTabletElements();
        } else {
            createPhoneElements();
        }
    }

    private void createPhoneElements() {
        TasksFragment tasksFragment = findOrCreateTasksFragment(R.id.contentFrame);
        mTasksPresenter = createListPresenter(tasksFragment);
        tasksFragment.setPresenter(mTasksPresenter);
    }

    private void createTabletElements() {
        // Fragment 1: List
        TasksFragment tasksFragment = findOrCreateTasksFragment(R.id.contentFrame_list);
        mTasksPresenter = createListPresenter(tasksFragment);

        // Fragment 2: Detail
        TaskDetailFragment taskDetailFragment = findOrCreateTaskDetailFragmentForTablet();
        TaskDetailPresenter taskDetailPresenter = createTaskDetailPresenter(taskDetailFragment);

        // Fragments connect to their presenters through a tablet presenter:
        mTasksTabletPresenter = new TasksTabletPresenter(
                Injection.provideTasksRepository(mFragmentActivity),
                mTasksPresenter,
                this);

        tasksFragment.setPresenter(mTasksTabletPresenter);
        taskDetailFragment.setPresenter(mTasksTabletPresenter);
        mTasksTabletPresenter.setTaskDetailPresenter(taskDetailPresenter);
    }

    @NonNull
    private TaskDetailPresenter createTaskDetailPresenter(TaskDetailFragment taskDetailFragment) {
        return new TaskDetailPresenter(
                null,
                Injection.provideTasksRepository(mFragmentActivity.getApplicationContext()),
                taskDetailFragment);
    }

    @NonNull
    private AddEditTaskPresenter createAddEditTaskPresenter(@Nullable String taskId,
            AddEditTaskFragment addEditTaskFragment, boolean shouldLoadDataFromRepo) {
        assert mTasksTabletPresenter != null; // In tablet mode it's never null
        return new AddEditTaskPresenter(
                taskId,
                Injection.provideTasksRepository(mFragmentActivity.getApplicationContext()),
                addEditTaskFragment, shouldLoadDataFromRepo);
    }

    @NonNull
    private TasksPresenter createListPresenter(TasksFragment tasksFragment) {
        return new TasksPresenter(
                Injection.provideTasksRepository(mFragmentActivity.getApplicationContext()),
                tasksFragment);
    }

    @NonNull
    private TasksFragment findOrCreateTasksFragment(@IdRes int fragmentId) {
        TasksFragment tasksFragment =
                (TasksFragment) getFragmentById(fragmentId);
        if (tasksFragment == null) {
            // Create the fragment
            tasksFragment = TasksFragment.newInstance();
            ActivityUtils.addFragmentToActivity(
                    getSupportFragmentManager(), tasksFragment, fragmentId);
        }
        return tasksFragment;
    }

    @NonNull
    private TaskDetailFragment findOrCreateTaskDetailFragmentForTablet() {
        TaskDetailFragment taskDetailFragment =
                (TaskDetailFragment) getFragmentById(R.id.contentFrame_detail);
        if (taskDetailFragment == null) {
            // Create the fragment
            taskDetailFragment = TaskDetailFragment.newInstance();
            ActivityUtils.addFragmentToActivity(
                    getSupportFragmentManager(), taskDetailFragment, R.id.contentFrame_detail);
        }
        return taskDetailFragment;
    }

    @NonNull
    private AddEditTaskFragment findOrCreateAddEditTaskFragmentForTablet() {
        AddEditTaskFragment addEditTaskFragment = (AddEditTaskFragment) getSupportFragmentManager()
                .findFragmentByTag(ADD_EDIT_DIALOG_TAG);
        if (addEditTaskFragment == null) {
            addEditTaskFragment = AddEditTaskFragment.newInstance();
        }
        return addEditTaskFragment;
    }

    private Fragment getFragmentById(int contentFrame_detail) {
        return getSupportFragmentManager().findFragmentById(contentFrame_detail);
    }

    private FragmentManager getSupportFragmentManager() {
        return mFragmentActivity.getSupportFragmentManager();
    }

    private boolean isAddEditDialogShown() {
        return getSupportFragmentManager().findFragmentByTag(ADD_EDIT_DIALOG_TAG) != null;
    }
}
