package com.example.android.architecture.blueprints.todoapp

import android.content.Context
import androidx.room.Room
import com.example.android.architecture.blueprints.todoapp.data.source.DefaultTasksRepository
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository
import com.example.android.architecture.blueprints.todoapp.data.source.local.ToDoDatabase
import com.example.android.architecture.blueprints.todoapp.di.AddEditTaskModule
import com.example.android.architecture.blueprints.todoapp.di.StatisticsModule
import com.example.android.architecture.blueprints.todoapp.di.TaskDetailModule
import com.example.android.architecture.blueprints.todoapp.di.TasksModule
import com.example.android.architecture.blueprints.todoapp.di.ViewModelBuilder
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Named
import javax.inject.Singleton

@Singleton
@Component(modules = [
    TestApplicationModule::class,
    AndroidSupportInjectionModule::class,
    ViewModelBuilder::class,
    TasksModule::class,
    TaskDetailModule::class,
    AddEditTaskModule::class,
    StatisticsModule::class])
interface TestApplicationComponent : AndroidInjector<TestTodoApplication> {

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance applicationContext: Context): TestApplicationComponent
    }


    val tasksRepository: TasksRepository
}

@Module
class TestApplicationModule {

    @Singleton
    @Provides
    fun provideRepository(
        @Named("TasksRemoteDataSource") tasksRemoteDataSource: TasksDataSource,
        @Named("TasksLocalDataSource") tasksLocalDataSource: TasksDataSource,
        ioDispatcher: CoroutineDispatcher
    ): TasksRepository {
        return DefaultTasksRepository(tasksRemoteDataSource, tasksLocalDataSource, ioDispatcher)
    }

    @Singleton
    @Named("TasksRemoteDataSource")
    @Provides
    fun provideTasksRemoteDataSource(): TasksDataSource {
        return FakeTasksRemoteDataSource
    }

    @Singleton
    @Named("TasksLocalDataSource")
    @Provides
    fun provideTasksLocalDataSource(): TasksDataSource {
        return FakeTasksRemoteDataSource
    }

    @Singleton
    @Provides
    fun provideDataBase(context: Context): ToDoDatabase {
        return Room.databaseBuilder(context.applicationContext,
            ToDoDatabase::class.java, "Tasks.db")
            .build()
    }

    @Singleton
    @Provides
    fun provideIoDispatcher() : CoroutineDispatcher = Dispatchers.Main
}