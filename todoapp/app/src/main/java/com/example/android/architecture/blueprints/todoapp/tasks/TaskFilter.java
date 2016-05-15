package com.example.android.architecture.blueprints.todoapp.tasks;

import android.os.Bundle;

import com.example.android.architecture.blueprints.todoapp.BuildConfig;

public class TaskFilter {

    public final static String KEY_TASK_FILTER = BuildConfig.APPLICATION_ID + "TASK_FILTER";
    private TasksFilterType tasksFilterType = TasksFilterType.ALL_TASKS;
    private Bundle filterExtras;

    protected TaskFilter(Bundle extras) {
        this.filterExtras = extras;
        this.tasksFilterType = (TasksFilterType) extras.getSerializable(KEY_TASK_FILTER);
    }

    public static TaskFilter from(TasksFilterType tasksFilterType){
        Bundle bundle = new Bundle();
        bundle.putSerializable(KEY_TASK_FILTER, tasksFilterType);
        return new TaskFilter(bundle);
    }

    public TasksFilterType getTasksFilterType() {
        return tasksFilterType;
    }

    public Bundle getFilterExtras() {
        return filterExtras;
    }
}
