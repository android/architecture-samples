package com.example.android.architecture.blueprints.todoapp.statistics;

import android.content.Context;
import android.support.annotation.NonNull;

import com.example.android.architecture.blueprints.todoapp.Injection;

/**
 * Enables inversion of control of the ViewModel and Navigator classes for statistics.
 */
class StatisticsModule {

    @NonNull
    public static StatisticsViewModel createStatisticsViewModel(@NonNull Context context) {
        return new StatisticsViewModel(Injection.provideTasksRepository(context),
                Injection.createResourceProvider(context));
    }
}
