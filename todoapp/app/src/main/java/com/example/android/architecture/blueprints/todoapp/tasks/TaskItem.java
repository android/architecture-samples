package com.example.android.architecture.blueprints.todoapp.tasks;

import android.support.annotation.DrawableRes;

import com.example.android.architecture.blueprints.todoapp.data.Task;

import rx.functions.Action0;
import rx.functions.Action1;

/**
 * A task that should be displayed as an item in a list of tasks.
 * Contains the task, the action that should be triggered when taping on the task, the action that
 * should be triggered when checking or unchecking a task and the background that should be used for
 * this task.
 */
final class TaskItem {

    private Task mTask;

    @DrawableRes
    private int mBackground;

    private Action0 mOnClickAction;

    private Action1<Boolean> mOnCheckAction;

    public TaskItem(Task task, @DrawableRes int background,
                    Action0 onClickAction, Action1<Boolean> onCheckAction) {
        mTask = task;
        mBackground = background;
        mOnClickAction = onClickAction;
        mOnCheckAction = onCheckAction;
    }

    public Task getTask() {
        return mTask;
    }

    public int getBackground() {
        return mBackground;
    }

    public Action0 getOnClickAction() {
        return mOnClickAction;
    }

    public Action1<Boolean> getOnCheckAction() {
        return mOnCheckAction;
    }
}
