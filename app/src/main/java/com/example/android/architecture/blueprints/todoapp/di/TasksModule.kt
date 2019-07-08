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

package com.example.android.architecture.blueprints.todoapp.di

import androidx.lifecycle.ViewModel
import com.example.android.architecture.blueprints.todoapp.tasks.TasksFragment
import com.example.android.architecture.blueprints.todoapp.tasks.TasksViewModel
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

/**
 * Dagger module for the tasks list feature.
 */
@Module
abstract class TasksModule {

    @ContributesAndroidInjector(modules = [
        ViewModelBuilder::class
        ])
    internal abstract fun tasksFragment(): TasksFragment

    @Binds
    @IntoMap
    @ViewModelKey(TasksViewModel::class)
    abstract fun bindViewModel(viewmodel: TasksViewModel): ViewModel
}
