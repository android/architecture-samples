package com.example.android.architecture.blueprints.todoapp.tasks;

import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import java.util.List;

/**
 * Model for the list of tasks screen.
 */
class TasksUiModel {

    @StringRes
    private final int mFilterResId;

    private final boolean mIsTasksListVisible;

    private final List<TaskItem> mItemList;

    private final boolean mIsNoTasksViewVisible;

    @Nullable
    private final NoTasksModel mNoTasksModel;

    public TasksUiModel(@StringRes int filterResId, boolean isTasksListVisible, List<TaskItem> itemList,
                        boolean isNoTasksViewVisible, NoTasksModel noTasksModel) {
        mFilterResId = filterResId;
        mIsTasksListVisible = isTasksListVisible;
        mItemList = itemList;
        mIsNoTasksViewVisible = isNoTasksViewVisible;
        mNoTasksModel = noTasksModel;
    }

    @StringRes
    public int getFilterResId() {
        return mFilterResId;
    }

    public boolean isTasksListVisible() {
        return mIsTasksListVisible;
    }

    public List<TaskItem> getItemList() {
        return mItemList;
    }

    public boolean isNoTasksViewVisible() {
        return mIsNoTasksViewVisible;
    }

    @Nullable
    public NoTasksModel getNoTasksModel() {
        return mNoTasksModel;
    }
}
