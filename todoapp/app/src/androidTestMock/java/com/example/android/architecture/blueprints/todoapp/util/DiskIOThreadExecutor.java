package com.example.android.architecture.blueprints.todoapp.util;

import android.support.annotation.NonNull;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Executor that executes a task on a new single thread.
 * This implementation is used by the Android instrumentation tests.
 */
public class DiskIOThreadExecutor implements Executor {

    private final Executor mDiskIO;

    public DiskIOThreadExecutor() {
        mDiskIO = Executors.newSingleThreadExecutor();
    }

    @Override
    public void execute(@NonNull Runnable command) {
        // increment the idling resources before executing the long running command
        EspressoIdlingResource.increment();
        mDiskIO.execute(command);
        // decrement the idling resources once executing the command has been finished
        EspressoIdlingResource.decrement();
    }
}
