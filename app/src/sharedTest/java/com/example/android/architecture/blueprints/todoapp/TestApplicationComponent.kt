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

package com.example.android.architecture.blueprints.todoapp

import android.content.Context
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository
import com.example.android.architecture.blueprints.todoapp.data.usecases.INetworkTasksUseCases
import com.example.android.architecture.blueprints.todoapp.di.AddEditTaskModule
import com.example.android.architecture.blueprints.todoapp.di.StatisticsModule
import com.example.android.architecture.blueprints.todoapp.di.TaskDetailModule
import com.example.android.architecture.blueprints.todoapp.di.TasksModule
import com.example.android.architecture.blueprints.todoapp.di.ViewModelBuilder
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
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
    val networkTasksUseCases: INetworkTasksUseCases
}
