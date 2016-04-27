package com.example.android.architecture.blueprints.todoapp.statistics.domain;

public class Statistics {

    private final int completedTasks;
    private final int activeTasks;


    public Statistics(int completedTasks, int activeTasks) {
        this.completedTasks = completedTasks;
        this.activeTasks = activeTasks;
    }

    public int getCompletedTasks() {
        return completedTasks;
    }

    public int getActiveTasks() {
        return activeTasks;
    }
}
