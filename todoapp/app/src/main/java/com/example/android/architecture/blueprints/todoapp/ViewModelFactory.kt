/*
 *  Copyright 2017 Google Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.example.android.architecture.blueprints.todoapp

import android.annotation.SuppressLint
import android.app.Application
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider

import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskViewModel
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository
import com.example.android.architecture.blueprints.todoapp.statistics.StatisticsViewModel
import com.example.android.architecture.blueprints.todoapp.taskdetail.TaskDetailViewModel
import com.example.android.architecture.blueprints.todoapp.tasks.TasksViewModel

/**
 * A creator is used to inject the product ID into the ViewModel
 *
 *
 * This creator is to showcase how to inject dependencies into ViewModels. It's not
 * actually necessary in this case, as the product ID can be passed in a public method.
 */
class ViewModelFactory private constructor(
        private val application: Application,
        private val tasksRepository: TasksRepository
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>) =
            with(modelClass) {
                when {
                    isAssignableFrom(StatisticsViewModel::class.java) ->
                        StatisticsViewModel(application, tasksRepository)
                    isAssignableFrom(TaskDetailViewModel::class.java) ->
                        TaskDetailViewModel(application, tasksRepository)
                    isAssignableFrom(AddEditTaskViewModel::class.java) ->
                        AddEditTaskViewModel(application, tasksRepository)
                    isAssignableFrom(TasksViewModel::class.java) ->
                        TasksViewModel(application, tasksRepository)
                    else ->
                        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
                }
            } as T

    companion object {

        @SuppressLint("StaticFieldLeak")
        @Volatile private var INSTANCE: ViewModelFactory? = null

        fun getInstance(application: Application): ViewModelFactory {
            return INSTANCE ?: synchronized(ViewModelFactory::class.java) {
                INSTANCE ?: ViewModelFactory(application,
                        Injection.provideTasksRepository(application.applicationContext))
                        .also { INSTANCE = it }
            }
        }
    }
}
