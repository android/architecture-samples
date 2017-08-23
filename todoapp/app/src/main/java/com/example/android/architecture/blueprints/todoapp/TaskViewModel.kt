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
package com.example.android.architecture.blueprints.todoapp

import android.content.Context
import android.databinding.BaseObservable
import android.databinding.Bindable
import android.databinding.Observable
import android.databinding.ObservableField

import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository


/**
 * Abstract class for View Models that expose a single [Task].
 */
abstract class TaskViewModel(
        context: Context,
        private val tasksRepository: TasksRepository
) : BaseObservable(), TasksDataSource.GetTaskCallback {

    val snackbarText = ObservableField<String>()
    val title = ObservableField<String>()
    val description = ObservableField<String>()

    private val taskObservable = ObservableField<Task>().apply {
        // Exposed observables depend on the taskObservable observable:
        addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(observable: Observable, i: Int) {
                val task = get()
                if (task != null) {
                    title.set(task.title)
                    description.set(task.description)
                } else {
                    title.set(this@TaskViewModel.context.getString(R.string.no_data))
                    description.set(this@TaskViewModel.context.getString(R.string.no_data_description))
                }
            }
        })
    }

    private val context: Context = context.applicationContext // Force use of Application Context.

    @get:Bindable
    var isDataLoading: Boolean = false
        private set

    // "completed" is two-way bound, so in order to intercept the new value, use a @Bindable
    // annotation and process it in the setter.
    var completed: Boolean
        @Bindable
        get() = taskObservable.get().isCompleted
        set(completed) {
            if (isDataLoading) {
                return
            }
            taskObservable.get().let {
                // Update the entity
                it.isCompleted = completed

                // Notify repository and user
                if (completed) {
                    tasksRepository.completeTask(it)
                    snackbarText.set(context.getString(R.string.task_marked_complete))
                } else {
                    tasksRepository.activateTask(it)
                    snackbarText.set(context.getString(R.string.task_marked_active))
                }
            }
        }

    val isDataAvailable: Boolean
        @Bindable
        get() = taskObservable.get() != null

    // This could be an observable, but we save a call to Task.getTitleForList() if not needed.
    val titleForList: String
        @Bindable
        get() {
            if (taskObservable.get() == null) {
                return "No data"
            }
            return taskObservable.get().titleForList
        }

    fun start(taskId: String?) {
        taskId?.let {
            isDataLoading = true
            tasksRepository.getTask(it, this)
        }
    }

    fun setTask(task: Task) {
        taskObservable.set(task)
    }

    override fun onTaskLoaded(task: Task) {
        taskObservable.set(task)
        isDataLoading = false
        notifyChange() // For the @Bindable properties
    }

    override fun onDataNotAvailable() {
        taskObservable.set(null)
        isDataLoading = false
    }

    fun deleteTask() {
        if (taskObservable.get() != null) {
            tasksRepository.deleteTask(taskObservable.get().id)
        }
    }

    fun onRefresh() {
        if (taskObservable.get() != null) {
            start(taskObservable.get().id)
        }
    }

    protected val taskId: String?
        get() = taskObservable.get().id
}
