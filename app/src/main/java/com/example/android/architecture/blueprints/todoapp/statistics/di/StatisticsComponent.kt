package com.example.android.architecture.blueprints.todoapp.statistics.di

import com.example.android.architecture.blueprints.todoapp.statistics.StatisticsFragment
import dagger.Subcomponent

@Subcomponent(modules = [StatisticsModule::class])
interface StatisticsComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(): StatisticsComponent
    }

    fun inject(fragment: StatisticsFragment)
}
