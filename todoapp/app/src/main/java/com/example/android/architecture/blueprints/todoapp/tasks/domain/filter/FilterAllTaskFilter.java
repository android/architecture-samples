package com.example.android.architecture.blueprints.todoapp.tasks.domain.filter;

import com.example.android.architecture.blueprints.todoapp.data.Task;

import java.util.List;

class FilterAllTaskFilter implements TaskFilter {
    @Override
    public List<Task> filter(List<Task> tasks) {
        return tasks;
    }
}
