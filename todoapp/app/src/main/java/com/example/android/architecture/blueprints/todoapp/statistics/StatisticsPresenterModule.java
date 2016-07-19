package com.example.android.architecture.blueprints.todoapp.statistics;

import dagger.Module;
import dagger.Provides;

/**
 * This is a Dagger module. We use this to pass in the View dependency to the
 * {@link StatisticsPresenter}.
 */
@Module
public class StatisticsPresenterModule {

    private final StatisticsContract.View mView;

    public StatisticsPresenterModule(StatisticsContract.View view) {
        mView = view;
    }

    @Provides
    StatisticsContract.View provideStatisticsContractView() {
        return mView;
    }
}
