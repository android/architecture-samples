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
import com.google.common.base.Objects;
import com.google.common.base.Strings;

/**
 * Created by sheng on 2016/10/24.
 */

public class TaskDetailViewModel extends BaseObservable {

    private final TaskDetailContract.Presenter mPresenter;

    private Context mContext;

    private String mTitle;
    private String mDescription;
    private boolean mNoData = false;

    public TaskDetailViewModel(Context context, TaskDetailContract.Presenter presenter) {
        mContext = context;
        mPresenter = presenter;
    }

    @Bindable
    public String getTitle() {
        return mTitle;
    }

    @Bindable
    public String getDescription() {
        return mDescription;
    }

    @Bindable
    public boolean isNoData() {
        return mNoData;
    }

    public void setTask(Task task) {
        if (task == null) {
            mNoData = true;
            mTitle = mContext.getString(R.string.no_data);
            mDescription = mContext.getString(R.string.no_data);
        } else {
            mNoData = false;
            mTitle = task.getTitle();
            mDescription = task.getDescription();
        }
        notifyPropertyChanged(BR.title);
        notifyPropertyChanged(BR.description);
        notifyPropertyChanged(BR.noData);
        // mTaskDetailView.showCompletionStatus(task.isCompleted());
    }

}
