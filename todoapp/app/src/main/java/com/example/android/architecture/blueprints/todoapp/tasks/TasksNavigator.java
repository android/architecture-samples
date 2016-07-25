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

package com.example.android.architecture.blueprints.todoapp.tasks;

import static com.example.android.architecture.blueprints.todoapp.taskdetail.TaskDetailActivity.EXTRA_TASK_ID;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

import com.example.android.architecture.blueprints.todoapp.R;
import com.example.android.architecture.blueprints.todoapp.taskdetail.TaskDetailActivity;
import com.example.android.architecture.blueprints.todoapp.taskdetail.TaskDetailFragment;

/**
 * TODO: javadoc
 */

public class TasksNavigator {

    private final FragmentActivity mFragmentActivity;

    private final TasksMvpController mTasksMvpBuilder;

    public TasksNavigator(FragmentActivity fragmentActivity, TasksMvpController tasksMvpBuilder) {
        mFragmentActivity = fragmentActivity;
        mTasksMvpBuilder = tasksMvpBuilder;
    }

    public void onTaskDeleted() {
        if (isTablet()) {
            removeDetailPane();
        } else {
            mFragmentActivity.finish();
        }
    }

    public void startTaskDetail(String taskId) {
        if (isTablet()) {
            TaskDetailFragment detailFragment = mTasksMvpBuilder.createDetailViewForTablet(taskId);

            // Show fragment
            if (getDetailFragment() == null) { //TODO: better alternative?
                mFragmentActivity.getSupportFragmentManager().beginTransaction()
                        .add(R.id.contentFrame_detail, detailFragment)
                        .addToBackStack(null)
                        .commit();
            } else {
                mFragmentActivity.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.contentFrame_detail, detailFragment)
                        .commit();
            }

        } else {
            Intent intent = new Intent(mFragmentActivity, TaskDetailActivity.class);
            intent.putExtra(EXTRA_TASK_ID, taskId);
            mFragmentActivity.startActivity(intent);
        }
    }

    public boolean isTablet() {
        return mFragmentActivity.getResources().getBoolean(R.bool.isTablet);
    }

    public void removeDetailPane() {
        Fragment detailFragment = getDetailFragment();
        FragmentTransaction transaction = mFragmentActivity.getSupportFragmentManager().beginTransaction();
        transaction.remove(detailFragment);
        transaction.commit();
    }

    private Fragment getDetailFragment() {
        return mFragmentActivity.getSupportFragmentManager().findFragmentById(R.id.contentFrame_detail);
    }
}
