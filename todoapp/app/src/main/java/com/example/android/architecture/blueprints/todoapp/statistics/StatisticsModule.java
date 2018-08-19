package com.example.android.architecture.blueprints.todoapp.statistics;

import com.example.android.architecture.blueprints.todoapp.di.ActivityScoped;
import com.example.android.architecture.blueprints.todoapp.di.FragmentScoped;

import dagger.Binds;
import dagger.Module;
import dagger.android.ContributesAndroidInjector;

/**
 * This is a Dagger module. We use this to pass in the View dependency to the
 * {@link StatisticsPresenter}.
 */
@Module
public abstract class StatisticsModule {

    @FragmentScoped
    @ContributesAndroidInjector
    abstract StatisticsFragment statisticsFragment();

    @ActivityScoped
    @Binds
    abstract StatisticsContract.Presenter statisticsPresenter(StatisticsPresenter presenter);
}
