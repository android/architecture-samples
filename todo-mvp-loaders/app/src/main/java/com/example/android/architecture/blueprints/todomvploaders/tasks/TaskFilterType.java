package com.example.android.architecture.blueprints.todomvploaders.tasks;

public enum TaskFilterType {
    ALL_TASKS,
    ACTIVE_TASKS,
    COMPLETED_TASKS;

    public static TaskFilterType from(int filter) {
        for (TaskFilterType taskFilter : values()) {
            if (taskFilter.ordinal() == filter) {
                return taskFilter;
            }
        }
        return ALL_TASKS;
    }
}
