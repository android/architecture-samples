package com.example.android.architecture.blueprints.todoapp.taskdetail;

/**
 * Model for the task displayed in task detail screen.
 */
final class TaskModel {

    private boolean mShowTitle;

    private String mTitle;

    private boolean mShowDescription;

    private String mDescription;

    private boolean mIsChecked;

    public TaskModel(boolean showTitle, String title, boolean showDescription, String description,
                     boolean isChecked) {
        mShowTitle = showTitle;
        mTitle = title;
        mShowDescription = showDescription;
        mDescription = description;
        mIsChecked = isChecked;
    }

    public boolean isShowTitle() {
        return mShowTitle;
    }

    public String getTitle() {
        return mTitle;
    }

    public boolean isShowDescription() {
        return mShowDescription;
    }

    public String getDescription() {
        return mDescription;
    }

    public boolean isChecked() {
        return mIsChecked;
    }
}
