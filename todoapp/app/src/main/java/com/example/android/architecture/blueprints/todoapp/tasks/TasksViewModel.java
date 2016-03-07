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

import static com.example.android.architecture.blueprints.todoapp.tasks.TasksFilterType.ALL_TASKS;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.graphics.drawable.Drawable;

import com.example.android.architecture.blueprints.todoapp.BR;
import com.example.android.architecture.blueprints.todoapp.R;

/**
 * Exposes the data to be used in the {@link TasksContract.View}.
 * <p>
 * {@link BaseObservable} implements a listener registration mechanism which is notified when a
 * property changes. This is done by assigning a {@link Bindable} annotation to the property's
 * getter method.
 */
public class TasksViewModel extends BaseObservable {

    int mTaskListSize = 0;

    private final TasksContract.Presenter mPresenter;

    private Context mContext;

    public TasksViewModel(Context context, TasksContract.Presenter presenter) {
        mContext = context;
        mPresenter = presenter;
    }

    @Bindable
    public String getCurrentFilteringLabel() {
        switch (mPresenter.getFiltering()) {
            case ALL_TASKS:
                return mContext.getResources().getString(R.string.label_all);
            case ACTIVE_TASKS:
                return mContext.getResources().getString(R.string.label_active);
            case COMPLETED_TASKS:
                return mContext.getResources().getString(R.string.label_completed);
        }
        return null;
    }

    @Bindable
    public String getNoTasksLabel() {
        switch (mPresenter.getFiltering()) {
            case ALL_TASKS:
                return mContext.getResources().getString(R.string.no_tasks_all);
            case ACTIVE_TASKS:
                return mContext.getResources().getString(R.string.no_tasks_active);
            case COMPLETED_TASKS:
                return mContext.getResources().getString(R.string.no_tasks_completed);
        }
        return null;
    }

    @Bindable
    public Drawable getNoTaskIconRes() {
        switch (mPresenter.getFiltering()) {
            case ALL_TASKS:
                return mContext.getResources().getDrawable(R.drawable.ic_assignment_turned_in_24dp);
            case ACTIVE_TASKS:
                return mContext.getResources().getDrawable(R.drawable.ic_check_circle_24dp);
            case COMPLETED_TASKS:
                return mContext.getResources().getDrawable(R.drawable.ic_verified_user_24dp);
        }
        return null;
    }

    @Bindable
    public boolean getTasksAddViewVisible() {
        return mPresenter.getFiltering() == ALL_TASKS;
    }

    @Bindable
    public boolean isNotEmpty() {
        return mTaskListSize > 0;
    }

    public void setTaskListSize(int taskListSize) {
        mTaskListSize = taskListSize;
        notifyPropertyChanged(BR.noTaskIconRes);
        notifyPropertyChanged(BR.noTasksLabel);
        notifyPropertyChanged(BR.currentFilteringLabel);
        notifyPropertyChanged(BR.notEmpty);
        notifyPropertyChanged(BR.tasksAddViewVisible);
    }
}
