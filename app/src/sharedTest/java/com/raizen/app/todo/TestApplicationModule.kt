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

package com.raizen.app.todo

import com.raizen.app.todo.data.source.FakeNetworkTasksUseCases
import com.raizen.app.todo.data.source.FakeRepository
import com.raizen.app.todo.data.source.TasksRepository
import com.raizen.app.todo.data.usecases.INetworkTasksUseCases
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

    @Singleton
    @Provides
    fun provideNetworkTaskUseCases(): INetworkTasksUseCases = FakeNetworkTasksUseCases()

}
