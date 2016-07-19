package com.example.android.architecture.blueprints.todoapp;

import android.content.Context;

import dagger.Module;
import dagger.Provides;

/**
 * This is a Dagger module. We use this to pass in the Context dependency to the
 * {@link
 * com.example.android.architecture.blueprints.todoapp.data.source.TasksRepositoryComponent}.
 */
@Module
public final class ApplicationModule {

    private final Context mContext;

    ApplicationModule(Context context) {
        mContext = context;
    }

    @Provides
    Context provideContext() {
        return mContext;
    }

    @Provides
    UseCaseScheduler provideUseCaseScheduler() {
        return new UseCaseThreadPoolScheduler();
    }
}