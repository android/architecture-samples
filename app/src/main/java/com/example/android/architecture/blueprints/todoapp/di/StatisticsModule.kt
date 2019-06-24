package com.example.android.architecture.blueprints.todoapp.di

import androidx.lifecycle.ViewModel
import com.example.android.architecture.blueprints.todoapp.statistics.StatisticsFragment
import com.example.android.architecture.blueprints.todoapp.statistics.StatisticsUtils
import com.example.android.architecture.blueprints.todoapp.statistics.StatisticsViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

/**
 * Dagger module for the Statistics feature.
 *
 * It shows how to include a module that contains @Provides methods.
 */
@Module(includes = [StatsUtilsModule::class])
abstract class StatisticsModule {

    @ContributesAndroidInjector(modules = [
        ViewModelBuilder::class
    ])
    internal abstract fun statisticsFragment(): StatisticsFragment

    @Binds
    @IntoMap
    @ViewModelKey(StatisticsViewModel::class)
    abstract fun bindViewModel(viewmodel: StatisticsViewModel): ViewModel

}

@Module
class StatsUtilsModule {

    @Provides
    fun provideStatsUtil() = StatisticsUtils()
}