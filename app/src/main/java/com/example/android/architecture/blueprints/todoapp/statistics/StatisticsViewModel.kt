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

package com.example.android.architecture.blueprints.todoapp.statistics

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.architecture.blueprints.todoapp.data.Result.Success
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository
import kotlinx.coroutines.launch

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
    private val tasksRepository: TasksRepository
) : ViewModel() {

    private val _dataLoading = MutableLiveData<Boolean>()
    val dataLoading: LiveData<Boolean> = _dataLoading

    private val _error = MutableLiveData<Boolean>()
    val error: LiveData<Boolean> = _error

    /**
     * Controls whether the stats are shown or a "No data" message.
     */
    private val _empty = MutableLiveData<Boolean>()
    val empty: LiveData<Boolean> = _empty

    private val _numberOfActiveTasks = MutableLiveData<Int>()
    val numberOfActiveTasks: LiveData<Int> = _numberOfActiveTasks

    private val _numberOfCompletedTasks = MutableLiveData<Int>()
    val numberOfCompletedTasks: LiveData<Int> = _numberOfCompletedTasks

    private var activeTasks = 0

    private var completedTasks = 0

    fun start() {
        loadStatistics()
    }

    fun loadStatistics() {
        _dataLoading.value = true

        viewModelScope.launch {
            tasksRepository.getTasks().let { result ->
                if (result is Success) {
                    _error.value = false
                    computeStats(result.data)
                } else {
                    _error.value = true
                    activeTasks = 0
                    completedTasks = 0
                    updateDataBindingObservables()
                }
            }
        }
    }

    /**
     * Called when new data is ready.
     */
    private fun computeStats(tasks: List<Task>) {
        var completed = 0
        var active = 0

        for (task in tasks) {
            if (task.isCompleted) {
                completed += 1
            } else {
                active += 1
            }
        }
        activeTasks = active
        completedTasks = completed

        updateDataBindingObservables()
    }

    private fun updateDataBindingObservables() {
        _numberOfCompletedTasks.value = completedTasks

        _numberOfActiveTasks.value = activeTasks

        _empty.value = activeTasks + completedTasks == 0
        _dataLoading.value = false
    }
}
