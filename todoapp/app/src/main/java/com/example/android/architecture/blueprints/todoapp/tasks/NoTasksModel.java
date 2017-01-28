package com.example.android.architecture.blueprints.todoapp.tasks;

import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;

/**
 * The string and image that should be displayed when there are no tasks.
 */
public class NoTasksModel {

    @StringRes
    private int mText;

    @DrawableRes
    private int mIcon;

    private boolean mShowAdd;

    public NoTasksModel(int text, int icon, boolean showAdd) {
        mText = text;
        mIcon = icon;
        mShowAdd = showAdd;
    }

    @StringRes
    public int getText() {
        return mText;
    }

    @DrawableRes
    public int getIcon() {
        return mIcon;
    }

    public boolean isShowAdd() {
        return mShowAdd;
    }
}
