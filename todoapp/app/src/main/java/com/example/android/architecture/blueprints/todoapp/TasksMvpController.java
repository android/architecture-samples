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

import static com.google.common.base.Preconditions.checkNotNull;

import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import com.example.android.architecture.blueprints.todoapp.taskdetail.TaskDetailFragment;
import com.example.android.architecture.blueprints.todoapp.taskdetail.TaskDetailPresenter;
import com.example.android.architecture.blueprints.todoapp.tasks.TasksFragment;
import com.example.android.architecture.blueprints.todoapp.tasks.TasksPresenter;
import com.example.android.architecture.blueprints.todoapp.util.ActivityUtils;

/**
 * Class that creates fragments (MVP views) and makes the necessary connections between them.
 */
public class TasksMvpController {

    private final FragmentActivity mFragmentActivity;

    private TasksTabletPresenter mTasksTabletPresenter;

    private TasksPresenter mTasksPresenter;

    private TaskDetailPresenter mTaskDetailPresenter;

    /**
     * Creates a controller for a task view for phones or tablets.
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
     * Creates a controller for a task detail view for phones or tablets.
     * @param fragmentActivity the context activity
     * @return a TasksMvpController
     */
    public static TasksMvpController createTaskDetailView(
            @NonNull FragmentActivity fragmentActivity, String taskId) {
        checkNotNull(fragmentActivity);
        TasksMvpController tasksMvpController = new TasksMvpController(fragmentActivity);
        tasksMvpController.initTaskDetailView(taskId);
        return tasksMvpController;
    }

    // Force factory method, prevent direct instantiation:
    private TasksMvpController(@NonNull FragmentActivity fragmentActivity) {
        mFragmentActivity = fragmentActivity;
    }

    private void initTasksView() {
        if (isTablet()) {
            createTabletElements();
        } else {
            TasksFragment tasksFragment = findOrCreateTasksFragment();
            if (tasksFragment.getPresenter() != null) {
                mTasksPresenter = (TasksPresenter) tasksFragment.getPresenter();
            } else {
                mTasksPresenter = createListPresenter(tasksFragment);
            }
            tasksFragment.setPresenter(mTasksPresenter);
        }
    }

    private void initTaskDetailView(String taskId) {
        TaskDetailFragment taskDetailFragment = findOrCreateTaskDetailFragment(taskId);
        mTaskDetailPresenter = createTaskDetailPresenter(taskId, taskDetailFragment);
        taskDetailFragment.setPresenter(mTaskDetailPresenter);
    }

    public TasksPresenter getTasksPresenter() {
        return mTasksPresenter;
    }

    public TaskDetailPresenter getTaskDetailPresenter() {
        return mTaskDetailPresenter;
    }

    /**
     * To be called in tablet mode when a new detail view is requested.
     * @param taskId the ID of the task
     * @return the fragment to be added to the UI
     */
    public TaskDetailFragment createDetailViewForTablet(String taskId) {
        // Create the View
        TaskDetailFragment taskDetailFragment = TaskDetailFragment.newInstance(taskId);

        // Create the Presenter
        TaskDetailPresenter taskDetailPresenter = createTaskDetailPresenter(taskId,
                taskDetailFragment);

        // Wire presenters
        mTasksTabletPresenter.setTaskDetailPresenter(taskDetailPresenter);
        taskDetailFragment.setPresenter(mTasksTabletPresenter);
        return taskDetailFragment;
    }

    private void createTabletElements() {
        TasksFragment tasksFragment = findOrCreateTasksFragment();

        // TasksFragment is retained so let's reuse its presenter.
        if (tasksFragment.getPresenter() != null) {
            TasksTabletPresenter retainedPresenter =
                    (TasksTabletPresenter) tasksFragment.getPresenter();
            mTasksPresenter = retainedPresenter.getTasksPresenter();
        } else {
            mTasksPresenter = createListPresenter(tasksFragment);
        }

        mTasksTabletPresenter = new TasksTabletPresenter(
                Injection.provideTasksRepository(mFragmentActivity),
                new TasksTabletNavigator(mFragmentActivity, this),
                mTasksPresenter);

        tasksFragment.setPresenter(mTasksTabletPresenter);

        // TaskDetailFragment is retained, so let's reuse its presenter.
        TaskDetailFragment taskDetailFragment = getDetailFragment();
        if (taskDetailFragment != null && taskDetailFragment.isAdded()) {

            if (taskDetailFragment.getPresenter() instanceof TasksTabletPresenter) {
                TasksTabletPresenter retainedPresenter =
                        (TasksTabletPresenter) taskDetailFragment.getPresenter();

                TaskDetailPresenter retainedTaskDetailPresenter =
                        retainedPresenter.getTaskDetailPresenter();

                mTasksTabletPresenter.setTaskDetailPresenter(retainedTaskDetailPresenter);

                // Replace retained presenter with new one.
                taskDetailFragment.setPresenter(mTasksTabletPresenter);
            }
        }
    }

    @NonNull
    private TaskDetailPresenter createTaskDetailPresenter(
            String taskId, TaskDetailFragment taskDetailFragment) {
        return new TaskDetailPresenter(
                taskId,
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
    private TasksFragment findOrCreateTasksFragment() {
        TasksFragment tasksFragment =
                (TasksFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        if (tasksFragment == null) {
            // Create the fragment
            tasksFragment = TasksFragment.newInstance();
            ActivityUtils.addFragmentToActivity(
                    getSupportFragmentManager(), tasksFragment, R.id.contentFrame);
        }
        return tasksFragment;
    }

    @NonNull
    private TaskDetailFragment findOrCreateTaskDetailFragment(String taskId) {
        TaskDetailFragment taskDetailFragment =
                (TaskDetailFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        if (taskDetailFragment == null) {
            // Create the fragment
            taskDetailFragment = TaskDetailFragment.newInstance(taskId);
            ActivityUtils.addFragmentToActivity(
                    getSupportFragmentManager(), taskDetailFragment, R.id.contentFrame);
        }
        return taskDetailFragment;
    }

    private TaskDetailFragment getDetailFragment() {
        return (TaskDetailFragment) mFragmentActivity.getSupportFragmentManager().findFragmentById(
                R.id.contentFrame_detail);
    }

    private FragmentManager getSupportFragmentManager() {
        return mFragmentActivity.getSupportFragmentManager();
    }

    private boolean isTablet() {
        return mFragmentActivity.getResources().getBoolean(R.bool.isTablet);
    }
}
