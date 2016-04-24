package com.example.android.architecture.blueprints.todoapp.tasks;

import android.os.Bundle;

import com.example.android.architecture.blueprints.todoapp.data.source.TasksInteractor;

public class TaskFilter {

    private TasksFilterType tasksFilterType = TasksFilterType.ALL_TASKS;
    private Bundle filterExtras;

    protected TaskFilter(Bundle extras) {
        this.filterExtras = extras;
        this.tasksFilterType = (TasksFilterType) extras.getSerializable(TasksInteractor.KEY_TASK_FILTER);
    }

    public static TaskFilter from(TasksFilterType tasksFilterType){
        Bundle bundle = new Bundle();
        bundle.putSerializable(TasksInteractor.KEY_TASK_FILTER, tasksFilterType);
        return new TaskFilter(bundle);
    }

    public TasksFilterType getTasksFilterType() {
        return tasksFilterType;
    }

    public Bundle getFilterExtras() {
        return filterExtras;
    }
}
