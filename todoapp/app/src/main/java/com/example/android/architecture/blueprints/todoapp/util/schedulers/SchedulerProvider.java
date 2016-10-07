package com.example.android.architecture.blueprints.todoapp.util.schedulers;

import android.support.annotation.NonNull;

import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Wang, Sheng-Yuan on 2016/10/7.
 *
 * Provides different types of Schedulers.
 */

public class SchedulerProvider implements BaseSchedulerProvider {

    private static final SchedulerProvider INSTANCE = new SchedulerProvider();

    // Prevent direct instantiation.
    private SchedulerProvider() {}

    public static SchedulerProvider getInstance() {
        return INSTANCE;
    }

    @NonNull
    @Override
    public Scheduler computation() {
        return Schedulers.computation();
    }

    @NonNull
    @Override
    public Scheduler io() {
        return Schedulers.io();
    }

    @NonNull
    @Override
    public Scheduler ui() {
        return AndroidSchedulers.mainThread();
    }

}
