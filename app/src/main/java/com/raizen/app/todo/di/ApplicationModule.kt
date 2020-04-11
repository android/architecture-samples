/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.raizen.app.todo.di

import android.content.Context
import androidx.room.Room
import com.raizen.app.todo.data.source.DefaultTasksRepository
import com.raizen.app.todo.data.source.TasksDataSource
import com.raizen.app.todo.data.source.TasksRepository
import com.raizen.app.todo.data.source.local.TasksLocalDataSource
import com.raizen.app.todo.data.source.local.ToDoDatabase
import com.raizen.app.todo.data.source.network.ApiService
import com.raizen.app.todo.data.source.network.INetworkTasksRepository
import com.raizen.app.todo.data.source.network.NetworkTasksRepository
import com.raizen.app.todo.data.source.remote.TasksRemoteDataSource
import com.raizen.app.todo.data.usecases.INetworkTasksUseCases
import com.raizen.app.todo.data.usecases.NetworkTasksUseCases
import dagger.Binds
import dagger.Module
import dagger.Provides
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Qualifier
import javax.inject.Singleton
import kotlin.annotation.AnnotationRetention.RUNTIME


@Module(includes = [ApplicationModuleBinds::class])
object ApplicationModule {

    @Qualifier
    @Retention(RUNTIME)
    annotation class TasksRemoteDataSource

    @Qualifier
    @Retention(RUNTIME)
    annotation class TasksLocalDataSource


    @JvmStatic
    @Singleton
    @TasksRemoteDataSource
    @Provides
    fun provideTasksRemoteDataSource(): TasksDataSource {
        return TasksRemoteDataSource
    }

    @JvmStatic
    @Singleton
    @TasksLocalDataSource
    @Provides
    fun provideTasksLocalDataSource(
        database: ToDoDatabase,
        ioDispatcher: CoroutineDispatcher
    ): TasksDataSource {
        return TasksLocalDataSource(
            database.taskDao(), ioDispatcher
        )
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideDataBase(context: Context): ToDoDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            ToDoDatabase::class.java,
            "Tasks.db"
        ).build()
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideIoDispatcher() = Dispatchers.IO


    @JvmStatic
    @Singleton
    @Provides
    fun provideApiService(retrofit: Retrofit): ApiService = retrofit.create(ApiService::class.java)

    @JvmStatic
    @Singleton
    @Provides
    fun provideRetrofitInterface(): Retrofit {
        val endpoint = "https://jsonplaceholder.typicode.com"
        return Retrofit.Builder()
                .baseUrl(endpoint)
                .addConverterFactory(MoshiConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                .build()
    }

}

@Module
abstract class ApplicationModuleBinds {

    @Singleton
    @Binds
    abstract fun bindRepository(repo: DefaultTasksRepository): TasksRepository

    @Singleton
    @Binds
    abstract fun bindNetworkRepository(repo: NetworkTasksRepository): INetworkTasksRepository

    @Singleton
    @Binds
    abstract fun bindNetworkUseCase(repo: NetworkTasksUseCases): INetworkTasksUseCases
}
