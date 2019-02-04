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
package com.example.android.architecture.blueprints.todoapp.tasks

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import android.content.Context
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.databinding.ObservableList
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.SingleLiveEvent
import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskActivity
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository
import com.example.android.architecture.blueprints.todoapp.util.ADD_EDIT_RESULT_OK
import com.example.android.architecture.blueprints.todoapp.util.DELETE_RESULT_OK
import com.example.android.architecture.blueprints.todoapp.util.EDIT_RESULT_OK


/**
 * Exposes the data to be used in the task list screen.
 *
 *
 * [BaseObservable] implements a listener registration mechanism which is notified when a
 * property changes. This is done by assigning a [Bindable] annotation to the property's
 * getter method.
 */
class TasksViewModel(
        context: Application,
        private val tasksRepository: TasksRepository
) : AndroidViewModel(context) {

    private val isDataLoadingError = ObservableBoolean(false)
    private val context: Context = context.applicationContext //Application Context to avoid leaks.

    internal val openTaskEvent = SingleLiveEvent<String>()

    // These observable fields will update Views automatically
    val items: ObservableList<Task> = ObservableArrayList()
    val dataLoading = ObservableBoolean(false)
    val currentFilteringLabel = ObservableField<String>()
    val noTasksLabel = ObservableField<String>()
    val noTaskIconRes = ObservableField<Drawable>()
    val empty = ObservableBoolean(false)
    val tasksAddViewVisible = ObservableBoolean()
    val snackbarMessage = SingleLiveEvent<Int>()
    val newTaskEvent = SingleLiveEvent<Void>()

    var currentFiltering = TasksFilterType.ALL_TASKS
        set(value) {
            field = value
            // Depending on the filter type, set the filtering label, icon drawables, etc.
            updateFiltering()
        }

    fun start() {
        loadTasks(false)
    }

    fun loadTasks(forceUpdate: Boolean) {
        loadTasks(forceUpdate, true)
    }

    /**
     * Sets the current task filtering type.

     * @param requestType Can be [TasksFilterType.ALL_TASKS],
     * *                    [TasksFilterType.COMPLETED_TASKS], or
     * *                    [TasksFilterType.ACTIVE_TASKS]
     */
    fun updateFiltering() {
        // Depending on the filter type, set the filtering label, icon drawables, etc.
        when (currentFiltering) {
            TasksFilterType.ALL_TASKS -> {
                setFilter(R.string.label_all, R.string.no_tasks_all,
                        R.drawable.ic_assignment_turned_in_24dp, true)
            }
            TasksFilterType.ACTIVE_TASKS -> {
                setFilter(R.string.label_active, R.string.no_tasks_active,
                        R.drawable.ic_check_circle_24dp, false)
            }
            TasksFilterType.COMPLETED_TASKS -> {
                setFilter(R.string.label_completed, R.string.no_tasks_completed,
                        R.drawable.ic_verified_user_24dp, false)
            }
        }
    }

    private fun setFilter(@StringRes filteringLabelString: Int, @StringRes noTasksLabelString: Int,
            @DrawableRes noTaskIconDrawable: Int, tasksAddVisible: Boolean) {
        with(context.resources) {
            currentFilteringLabel.set(context.getString(filteringLabelString))
            noTasksLabel.set(getString(noTasksLabelString))
            noTaskIconRes.set(getDrawable(noTaskIconDrawable))
            tasksAddViewVisible.set(tasksAddVisible)
        }
    }

    fun clearCompletedTasks() {
        tasksRepository.clearCompletedTasks()
        snackbarMessage.value = R.string.completed_tasks_cleared
        loadTasks(false, false)
    }

    fun completeTask(task: Task, completed: Boolean) {
        // Update the entity
        task.isCompleted = completed

        // Notify repository
        if (completed) {
            tasksRepository.completeTask(task)
            showSnackbarMessage(R.string.task_marked_complete)
        } else {
            tasksRepository.activateTask(task)
            showSnackbarMessage(R.string.task_marked_active)
        }
    }

    private fun showSnackbarMessage(message: Int) {
        snackbarMessage.value = message
    }

    /**
     * Called by the Data Binding library and the FAB's click listener.
     */
    fun addNewTask() {
        newTaskEvent.call()
    }

    fun handleActivityResult(requestCode: Int, resultCode: Int) {
        if (AddEditTaskActivity.REQUEST_CODE == requestCode) {
            snackbarMessage.value =
                    when (resultCode) {
                        EDIT_RESULT_OK ->
                            R.string.successfully_saved_task_message
                        ADD_EDIT_RESULT_OK ->
                            R.string.successfully_added_task_message
                        DELETE_RESULT_OK ->
                            R.string.successfully_deleted_task_message
                        else -> return
                    }
        }
    }

    /**
     * @param forceUpdate   Pass in true to refresh the data in the [TasksDataSource]
     * *
     * @param showLoadingUI Pass in true to display a loading icon in the UI
     */
    private fun loadTasks(forceUpdate: Boolean, showLoadingUI: Boolean) {
        if (showLoadingUI) {
            dataLoading.set(true)
        }
        if (forceUpdate) {
            tasksRepository.refreshTasks()
        }

        tasksRepository.getTasks(object : TasksDataSource.LoadTasksCallback {
            override fun onTasksLoaded(tasks: List<Task>) {
                val tasksToShow: List<Task>

                // We filter the tasks based on the requestType
                when (currentFiltering) {
                    TasksFilterType.ALL_TASKS ->
                        tasksToShow = tasks
                    TasksFilterType.ACTIVE_TASKS ->
                        tasksToShow = tasks.filter { it.isActive }
                    TasksFilterType.COMPLETED_TASKS ->
                        tasksToShow = tasks.filter { it.isCompleted }
                }

                if (showLoadingUI) {
                    dataLoading.set(false)
                }
                isDataLoadingError.set(false)

                with(items) {
                    clear()
                    addAll(tasksToShow)
                    empty.set(isEmpty())
                }
            }

            override fun onDataNotAvailable() {
                isDataLoadingError.set(true)
            }
        })
    }
}
