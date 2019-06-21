package com.example.android.architecture.blueprints.todoapp.di

import android.content.Context
import androidx.room.Room
import com.example.android.architecture.blueprints.todoapp.data.source.DefaultTasksRepository
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository
import com.example.android.architecture.blueprints.todoapp.data.source.local.TasksLocalDataSource
import com.example.android.architecture.blueprints.todoapp.data.source.local.ToDoDatabase
import com.example.android.architecture.blueprints.todoapp.data.source.remote.TasksRemoteDataSource
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Named
import javax.inject.Singleton

@Module
class ApplicationModule {

    @Singleton
    @Provides
    fun provideRepository(
        @Named("TasksRemoteDataSource") tasksRemoteDataSource: TasksDataSource,
        @Named("TasksLocalDataSource") tasksLocalDataSource: TasksDataSource,
        ioDispatcher: CoroutineDispatcher
    ): TasksRepository {
        return DefaultTasksRepository(
            tasksRemoteDataSource, tasksLocalDataSource, ioDispatcher
        )
    }

    @Singleton
    @Named("TasksRemoteDataSource")
    @Provides
    fun provideTasksRemoteDataSource(): TasksDataSource {
        return TasksRemoteDataSource
    }

    @Singleton
    @Named("TasksLocalDataSource")
    @Provides
    fun provideTasksLocalDataSource(
        database: ToDoDatabase,
        ioDispatcher: CoroutineDispatcher
    ): TasksDataSource {
        return TasksLocalDataSource(
            database.taskDao(), ioDispatcher
        )
    }

    @Singleton
    @Provides
    fun provideDataBase(context: Context): ToDoDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            ToDoDatabase::class.java,
            "Tasks.db"
        )
            .build()
    }

    @Singleton
    @Provides
    fun provideIoDispatcher() = Dispatchers.IO
}