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

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

import com.example.android.architecture.blueprints.todoapp.taskdetail.TaskDetailFragment;

/**
 * Manages screens in tablet mode.
 */

public class TasksTabletNavigator {

    private final FragmentActivity mFragmentActivity;

    private final TasksMvpController mTasksMvpBuilder;

    public TasksTabletNavigator(
            FragmentActivity fragmentActivity, TasksMvpController tasksMvpBuilder) {
        mFragmentActivity = fragmentActivity;
        mTasksMvpBuilder = tasksMvpBuilder;
    }

    public void startTaskDetailForTablet(String taskId) {
        TaskDetailFragment detailFragment = mTasksMvpBuilder.createDetailViewForTablet(taskId);

        mFragmentActivity.getSupportFragmentManager().beginTransaction()
                .replace(R.id.contentFrame_detail, detailFragment)
                .commit();
    }

    public void removeDetailPane() {
        Fragment detailFragment = getDetailFragment();
        FragmentTransaction transaction =
                mFragmentActivity.getSupportFragmentManager().beginTransaction();
        transaction.remove(detailFragment);
        transaction.commit();
    }

    private Fragment getDetailFragment() {
        return mFragmentActivity.getSupportFragmentManager().findFragmentById(
                R.id.contentFrame_detail);
    }
}
