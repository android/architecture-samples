package com.example.android.architecture.blueprints.todoapp.statistics.di

import androidx.lifecycle.ViewModel
import com.example.android.architecture.blueprints.todoapp.di.ViewModelKey
import com.example.android.architecture.blueprints.todoapp.statistics.StatisticsViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class StatisticsModule {

    @Binds
    @IntoMap
    @ViewModelKey(StatisticsViewModel::class)
    abstract fun bindViewModel(viewmodel: StatisticsViewModel): ViewModel
}
