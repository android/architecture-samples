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

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;

import com.example.android.architecture.blueprints.todoapp.BR;
import com.example.android.architecture.blueprints.todoapp.R;

/**
 * Created by Wang, Sheng-Yuan on 2016/10/12.
 *
 * Exposes the data to be used in the {@link TasksContract.View}.
 *
 * @{link BaseObservable} implements a listener registration mechanism which is
 * notified when a property changes. This is done by assigning a {@link Bindable}
 * annotation to the property's getter method.
 */

public class TasksViewModel extends BaseObservable{

    int mTasksListSize = 0;

    private final TasksContract.Presenter mPresenter;

    private Context mContext;

    public TasksViewModel(@NonNull Context context, @NonNull TasksContract.Presenter presenter) {
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
                return mContext.getResources().getString(R.string.label_all);
            case ACTIVE_TASKS:
                return mContext.getResources().getString(R.string.label_active);
            case COMPLETED_TASKS:
                return mContext.getResources().getString(R.string.label_completed);
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
    public boolean getTasksAddViewVisiable() {
        return mPresenter.getFiltering() == TasksFilterType.ALL_TASKS;
    }

    @Bindable
    public boolean isNotEmpty() {
        return mTasksListSize > 0;
    }

    public void setTaskListSize(int taskListSize) {
        mTasksListSize = taskListSize;
        notifyPropertyChanged(BR.noTaskIconRes);
        notifyPropertyChanged(BR.noTasksLabel);
        notifyPropertyChanged(BR.currentFilteringLabel);
        notifyPropertyChanged(BR.notEmpty);
        notifyPropertyChanged(BR.tasksAddViewVisiable);
    }

}
