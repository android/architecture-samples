package com.example.android.architecture.blueprints.todoapp.tasks.domain.filter;

import com.example.android.architecture.blueprints.todoapp.tasks.TasksFilterType;

import java.util.HashMap;
import java.util.Map;

public class FilterFactory {

    private final static Map<TasksFilterType, TaskFilter> mFilters = new HashMap<>();

    public FilterFactory() {
        mFilters.put(TasksFilterType.ALL_TASKS, new FilterAllTaskFilter());
        mFilters.put(TasksFilterType.ACTIVE_TASKS, new ActiveTaskFilter());
        mFilters.put(TasksFilterType.COMPLETED_TASKS, new CompleteTaskFilter());
    }

    public TaskFilter create(TasksFilterType filterType) {
        return mFilters.get(filterType);
    }
}
