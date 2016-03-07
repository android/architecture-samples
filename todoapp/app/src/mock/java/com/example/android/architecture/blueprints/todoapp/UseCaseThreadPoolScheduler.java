package com.example.android.architecture.blueprints.todoapp;

public class UseCaseThreadPoolScheduler implements UseCaseScheduler {

    @Override
    public void execute(Runnable runnable) {
        new Thread(runnable).start();
    }
}
