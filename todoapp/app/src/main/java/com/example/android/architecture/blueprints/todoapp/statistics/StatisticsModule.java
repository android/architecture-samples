package com.example.android.architecture.blueprints.todoapp.statistics;

import com.example.android.architecture.blueprints.todoapp.di.PerActivity;
import com.example.android.architecture.blueprints.todoapp.di.PerFragment;

import dagger.Binds;
import dagger.Module;
import dagger.android.ContributesAndroidInjector;

/**
 * This is a Dagger module. We use this to pass in the View dependency to the
 * {@link StatisticsPresenter}.
 */
@Module
public abstract class StatisticsModule {

    @PerFragment
    @ContributesAndroidInjector
    abstract StatisticsFragment statisticsFragment();

    @PerActivity
    @Binds
    abstract StatisticsContract.Presenter statitsticsPresenter(StatisticsPresenter presenter);
}
