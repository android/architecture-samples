package com.example.android.architecture.blueprints.todoapp

import com.example.android.architecture.blueprints.todoapp.data.source.FakeRepository
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * A replacement for [ApplicationModule] to be used in tests. It simply creates a [FakeRepository].
 */
@Module
class TestApplicationModule {

    @Singleton
    @Provides
    fun provideRepository(): TasksRepository = FakeRepository()
}
