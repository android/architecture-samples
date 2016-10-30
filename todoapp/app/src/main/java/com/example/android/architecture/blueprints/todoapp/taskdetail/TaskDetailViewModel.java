/*
 * Copyright 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.architecture.blueprints.todoapp.taskdetail;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.Bindable;

import com.example.android.architecture.blueprints.todoapp.BR;
import com.example.android.architecture.blueprints.todoapp.R;
import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.google.common.base.Strings;

/**
 * Created by sheng on 2016/10/24.
 */

public class TaskDetailViewModel extends BaseObservable {

    private final TaskDetailContract.Presenter mPresenter;

    private Context mContext;

    private String mTitle;
    private boolean mHidTitle = false;
    private boolean mHidDescription = false;
    private String mDescription;
    private boolean mIsCompleted = false;

    public TaskDetailViewModel(Context context, TaskDetailContract.Presenter presenter) {
        mContext = context;
        mPresenter = presenter;
    }

    @Bindable
    public String getTitle() {
        return mTitle;
    }

    @Bindable
    public boolean isHidTitle() {
        return mHidTitle;
    }

    @Bindable
    public String getDescription() {
        return mDescription;
    }

    @Bindable
    public boolean isHidDescription() {
        return mHidDescription;
    }

    @Bindable
    public boolean isCompleted() {
        return mIsCompleted;
    }


    /**
     * Called by the Data Binding library when the checkbox is toggled.
     */
    public void completeChanged(boolean isChecked) {
        if (isChecked) {
            mPresenter.completeTask();
        } else {
            mPresenter.activateTask();
        }
    }

    public void missingTask() {
        mTitle = "";
        mDescription = mContext.getString(R.string.no_data);
        mHidTitle = true;
        mHidDescription = true;
        notifyPropertyChanged(BR.title);
        notifyPropertyChanged(BR.description);
        notifyPropertyChanged(BR.hidTitle);
        notifyPropertyChanged(BR.hidDescription);
    }


    public void setTask(Task task) {
        String title = task.getTitle();
        String description = task.getDescription();
        mIsCompleted = task.isCompleted();
        if (Strings.isNullOrEmpty(title)) {
            mHidTitle = true;
        } else {
            mTitle = title;
        }

        if (Strings.isNullOrEmpty(description)) {
            mHidDescription = true;
        } else {
            mDescription = description;
        }
        notifyPropertyChanged(BR.title);
        notifyPropertyChanged(BR.description);
        notifyPropertyChanged(BR.hidTitle);
        notifyPropertyChanged(BR.hidDescription);
    }

}
