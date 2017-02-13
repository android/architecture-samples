package com.example.android.architecture.blueprints.todoapp.tasks;

import android.support.annotation.Nullable;

import java.util.List;

/**
 * Model for the list of tasks screen.
 */
class TasksModel {

    private final boolean mIsTasksListVisible;

    private final List<TaskItem> mItemList;

    private final boolean mIsNoTasksViewVisible;

    @Nullable
    private final NoTasksModel mNoTasksModel;

    public TasksModel(boolean isTasksListVisible, List<TaskItem> itemList,
                      boolean isNoTasksViewVisible, NoTasksModel noTasksModel) {
        mIsTasksListVisible = isTasksListVisible;
        mItemList = itemList;
        mIsNoTasksViewVisible = isNoTasksViewVisible;
        mNoTasksModel = noTasksModel;
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
