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

    private final FragmentActivity mFragmentActivity;

    // Null task ID means there's no task selected (or in phone mode)
    @Nullable private final String mTaskId;

    private TasksTabletPresenter mTasksTabletPresenter;

    private TasksPresenter mTasksPresenter;

    // Force factory method, prevent direct instantiation:
    private TasksMvpController(
            @NonNull FragmentActivity fragmentActivity, @Nullable String taskId) {
        mFragmentActivity = fragmentActivity;
        mTaskId = taskId;
    }

    /**
     * Creates a controller for a task view for phones or tablets.
     * @param fragmentActivity the context activity
     * @return a TasksMvpController
     */
    public static TasksMvpController createTasksView(
            @NonNull FragmentActivity fragmentActivity, @Nullable String taskId) {
        checkNotNull(fragmentActivity);

        TasksMvpController tasksMvpController =
                new TasksMvpController(fragmentActivity, taskId);

        tasksMvpController.initTasksView();
        return tasksMvpController;
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
                mTasksPresenter);

        tasksFragment.setPresenter(mTasksTabletPresenter);
        taskDetailFragment.setPresenter(mTasksTabletPresenter);
        mTasksTabletPresenter.setTaskDetailPresenter(taskDetailPresenter);
    }

    @NonNull
    private TaskDetailPresenter createTaskDetailPresenter(TaskDetailFragment taskDetailFragment) {
        return new TaskDetailPresenter(
                mTaskId,
                Injection.provideTasksRepository(mFragmentActivity.getApplicationContext()),
                taskDetailFragment);
    }

    private TasksPresenter createListPresenter(TasksFragment tasksFragment) {
        mTasksPresenter = new TasksPresenter(
                Injection.provideTasksRepository(mFragmentActivity.getApplicationContext()),
                tasksFragment);

        return mTasksPresenter;
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

    private Fragment getFragmentById(int contentFrame_detail) {
        return getSupportFragmentManager().findFragmentById(contentFrame_detail);
    }

    public void setFiltering(TasksFilterType filtering) {
        mTasksPresenter.setFiltering(filtering);
    }

    public TasksFilterType getFiltering() {
        return mTasksPresenter.getFiltering();
    }

    public String getTaskId() {
        return mTaskId;
    }

    private FragmentManager getSupportFragmentManager() {
        return mFragmentActivity.getSupportFragmentManager();
    }
}
