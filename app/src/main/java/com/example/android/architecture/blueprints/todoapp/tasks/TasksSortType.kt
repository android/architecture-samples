package com.example.android.architecture.blueprints.todoapp.tasks

enum class TasksSortType(val value: String) {
    /**
     * No sort, data-source default order
     */
    DEFAULT("Default"),

    /**
     * Sort by Task Priority
     */
    TASK_PRIORITY("Task Priority"),

    /**
     * Sort Alphabetically by task name
     */
    ALPHABETICALLY("Alphabetically")
}