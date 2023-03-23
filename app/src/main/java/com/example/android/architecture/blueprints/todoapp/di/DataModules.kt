/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.architecture.blueprints.todoapp.di

import android.content.Context
import androidx.room.Room
import com.example.android.architecture.blueprints.todoapp.data.DefaultTaskRepository
import com.example.android.architecture.blueprints.todoapp.data.TaskRepository
import com.example.android.architecture.blueprints.todoapp.data.source.local.ToDoDatabase
import com.example.android.architecture.blueprints.todoapp.data.source.network.NetworkDataSource
import com.example.android.architecture.blueprints.todoapp.data.source.network.TaskNetworkDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.plus

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class NetworkTaskDataSource

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Singleton
    @Provides
    fun provideTaskRepository(
        @NetworkTaskDataSource remoteDataSource: NetworkDataSource,
        database: ToDoDatabase,
        @DefaultDispatcher dispatcher: CoroutineDispatcher,
        @ApplicationScope scope: CoroutineScope,
    ): TaskRepository {
        return DefaultTaskRepository(
            tasksNetworkDataSource = remoteDataSource,
            taskDao = database.taskDao(),
            expensiveWorkDispatcher = dispatcher,
            fireAndForgetScope = scope,
        )
    }
}

@Module
@InstallIn(SingletonComponent::class)
object DataSourceModule {

    @Singleton
    @NetworkTaskDataSource
    @Provides
    fun provideTaskRemoteDataSource(): NetworkDataSource = TaskNetworkDataSource
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Singleton
    @Provides
    fun provideDataBase(@ApplicationContext context: Context): ToDoDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            ToDoDatabase::class.java,
            "Tasks.db"
        ).build()
    }
}
