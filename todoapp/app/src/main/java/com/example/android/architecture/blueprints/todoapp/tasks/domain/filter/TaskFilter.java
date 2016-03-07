package com.example.android.architecture.blueprints.todoapp.tasks.domain.filter;

import com.example.android.architecture.blueprints.todoapp.data.Task;

import java.util.List;

public interface TaskFilter {
    List<Task> filter(List<Task> tasks);
}
