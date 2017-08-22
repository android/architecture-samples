/*
 * Copyright 2017, The Android Open Source Project
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
package com.example.android.architecture.blueprints.todoapp.statistics

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.databinding.Bindable
import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository

/**
 * Exposes the data to be used in the statistics screen.
 *
 *
 * This ViewModel uses both [ObservableField]s ([ObservableBoolean]s in this case) and
 * [Bindable] getters. The values in [ObservableField]s are used directly in the layout,
 * whereas the [Bindable] getters allow us to add some logic to it. This is
 * preferable to having logic in the XML layout.
 */
class StatisticsViewModel(
        private val context: Application,
        private val tasksRepository: TasksRepository
) : AndroidViewModel(context) {

    val dataLoading = ObservableBoolean(false)
    val error = ObservableBoolean(false)
    val numberOfActiveTasksString = ObservableField<String>()
    val numberOfCompletedTasksString = ObservableField<String>()
    /**
     * Controls whether the stats are shown or a "No data" message.
     */
    val empty = ObservableBoolean()
    private var numberOfActiveTasks = 0
    private var numberOfCompletedTasks = 0

    fun start() {
        loadStatistics()
    }

    fun loadStatistics() {
        dataLoading.set(true)
        tasksRepository.getTasks(object : TasksDataSource.LoadTasksCallback {
            override fun onTasksLoaded(tasks: List<Task>) {
                error.set(false)
                computeStats(tasks)
            }

            override fun onDataNotAvailable() {
                error.set(true)
                numberOfActiveTasks = 0
                numberOfCompletedTasks = 0
                updateDataBindingObservables()
            }
        })
    }

    /**
     * Called when new data is ready.
     */
    private fun computeStats(tasks: List<Task>) {
        numberOfCompletedTasks = tasks.count { it.isCompleted }
        numberOfActiveTasks = tasks.size - numberOfCompletedTasks
        updateDataBindingObservables()
    }

    private fun updateDataBindingObservables() {
        numberOfCompletedTasksString.set(
                context.getString(R.string.statistics_completed_tasks, numberOfCompletedTasks))
        numberOfActiveTasksString.set(
                context.getString(R.string.statistics_active_tasks, numberOfActiveTasks))
        empty.set(numberOfActiveTasks + numberOfCompletedTasks == 0)
        dataLoading.set(false)
    }
}
