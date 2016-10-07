package com.example.android.architecture.blueprints.todoapp.util.schedulers;

import android.support.annotation.NonNull;

import rx.Scheduler;

/**
 * Created by Wang, Sheng-Yuan on 2016/10/7.
 *
 * Allow providing different types to {@link Scheduler}s.
 */

public interface BaseSchedulerProvider {

    @NonNull
    Scheduler computation();

    @NonNull
    Scheduler io();

    @NonNull
    Scheduler ui();

}
