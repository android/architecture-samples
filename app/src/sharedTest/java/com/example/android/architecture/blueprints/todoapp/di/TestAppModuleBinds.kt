package com.example.android.architecture.blueprints.todoapp.di

import com.example.android.architecture.blueprints.todoapp.data.source.FakeRepository
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

/**
 * A replacement for [ApplicationModule] to be used in tests. It simply provides a [FakeRepository].
 */
@Module
abstract class TestAppModuleBinds {

    @Singleton
    @Binds
    abstract fun bindRepository(repo: FakeRepository): TasksRepository
}
