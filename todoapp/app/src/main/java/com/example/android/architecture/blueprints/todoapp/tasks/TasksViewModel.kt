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
import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.example.android.architecture.blueprints.todoapp.Event
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskActivity
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository
import com.example.android.architecture.blueprints.todoapp.util.ADD_EDIT_RESULT_OK
import com.example.android.architecture.blueprints.todoapp.util.DELETE_RESULT_OK
import com.example.android.architecture.blueprints.todoapp.util.EDIT_RESULT_OK
import java.util.ArrayList

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

    private val _items = MutableLiveData<List<Task>>().apply { value = emptyList() }
    val items: LiveData<List<Task>>
        get() = _items

    private val _dataLoading = MutableLiveData<Boolean>()
    val dataLoading: LiveData<Boolean>
        get() = _dataLoading

    private val _currentFilteringLabel = MutableLiveData<String>()
    val currentFilteringLabel: LiveData<String>
        get() = _currentFilteringLabel

    private val _noTasksLabel = MutableLiveData<String>()
    val noTasksLabel: LiveData<String>
        get() = _noTasksLabel

    private val _noTaskIconRes = MutableLiveData<Drawable>()
    val noTaskIconRes: LiveData<Drawable>
        get() = _noTaskIconRes

    private val _tasksAddViewVisible = MutableLiveData<Boolean>()
    val tasksAddViewVisible: LiveData<Boolean>
        get() = _tasksAddViewVisible

    private val _snackbarText = MutableLiveData<Event<Int>>()
    val snackbarMessage: LiveData<Event<Int>>
        get() = _snackbarText

    private var _currentFiltering = TasksFilterType.ALL_TASKS

    // Not used at the moment
    private val isDataLoadingError = MutableLiveData<Boolean>()

    private val _openTaskEvent = MutableLiveData<Event<String>>()
    val openTaskEvent: LiveData<Event<String>>
        get() = _openTaskEvent

    private val _newTaskEvent = MutableLiveData<Event<Unit>>()
    val newTaskEvent: LiveData<Event<Unit>>
        get() = _newTaskEvent

    // To prevent leaks, this must be an Application Context.
    private val context: Context = context.applicationContext

    // This LiveData depends on another so we can use a transformation.
    val empty: LiveData<Boolean> = Transformations.map(_items) {
        it.isEmpty()
    }

    init {
        // Set initial state
        setFiltering(TasksFilterType.ALL_TASKS)
    }

    fun start() {
        loadTasks(false)
    }

    fun loadTasks(forceUpdate: Boolean) {
        loadTasks(forceUpdate, true)
    }

    /**
     * Sets the current task filtering type.
     *
     * @param requestType Can be [TasksFilterType.ALL_TASKS],
     * [TasksFilterType.COMPLETED_TASKS], or
     * [TasksFilterType.ACTIVE_TASKS]
     */
    fun setFiltering(requestType: TasksFilterType) {
        _currentFiltering = requestType

        // Depending on the filter type, set the filtering label, icon drawables, etc.
        when (requestType) {
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
        _currentFilteringLabel.value = context.getString(filteringLabelString)
        _noTasksLabel.value = context.getString(noTasksLabelString)
        _noTaskIconRes.value = ContextCompat.getDrawable(context, noTaskIconDrawable)
        _tasksAddViewVisible.value = tasksAddVisible
    }

    fun clearCompletedTasks() {
        tasksRepository.clearCompletedTasks()
        _snackbarText.value = Event(R.string.completed_tasks_cleared)
        loadTasks(false, false)
    }

    fun completeTask(task: Task, completed: Boolean) {
        // Notify repository
        if (completed) {
            tasksRepository.completeTask(task)
            showSnackbarMessage(R.string.task_marked_complete)
        } else {
            tasksRepository.activateTask(task)
            showSnackbarMessage(R.string.task_marked_active)
        }
    }

    /**
     * Called by the Data Binding library and the FAB's click listener.
     */
    fun addNewTask() {
        _newTaskEvent.value = Event(Unit)
    }

    /**
     * Called by the [TasksAdapter].
     */
    internal fun openTask(taskId: String) {
        _openTaskEvent.value = Event(taskId)
    }

    fun handleActivityResult(requestCode: Int, resultCode: Int) {
        if (AddEditTaskActivity.REQUEST_CODE == requestCode) {
            when (resultCode) {
                EDIT_RESULT_OK -> _snackbarText.setValue(
                    Event(R.string.successfully_saved_task_message)
                )
                ADD_EDIT_RESULT_OK -> _snackbarText.setValue(
                    Event(R.string.successfully_added_task_message)
                )
                DELETE_RESULT_OK -> _snackbarText.setValue(
                    Event(R.string.successfully_deleted_task_message)
                )
            }
        }
    }

    private fun showSnackbarMessage(message: Int) {
        _snackbarText.value = Event(message)
    }

    /**
     * @param forceUpdate   Pass in true to refresh the data in the [TasksDataSource]
     * @param showLoadingUI Pass in true to display a loading icon in the UI
     */
    private fun loadTasks(forceUpdate: Boolean, showLoadingUI: Boolean) {
        if (showLoadingUI) {
            _dataLoading.setValue(true)
        }
        if (forceUpdate) {

            tasksRepository.refreshTasks()
        }

        tasksRepository.getTasks(object : TasksDataSource.LoadTasksCallback {
            override fun onTasksLoaded(tasks: List<Task>) {
                val tasksToShow = ArrayList<Task>()

                // We filter the tasks based on the requestType
                for (task in tasks) {
                    when (_currentFiltering) {
                        TasksFilterType.ALL_TASKS -> tasksToShow.add(task)
                        TasksFilterType.ACTIVE_TASKS -> if (task.isActive) {
                            tasksToShow.add(task)
                        }
                        TasksFilterType.COMPLETED_TASKS -> if (task.isCompleted) {
                            tasksToShow.add(task)
                        }
                    }
                }
                if (showLoadingUI) {
                    _dataLoading.value = false
                }
                isDataLoadingError.value = false

                val itemsValue = ArrayList(tasksToShow)
                _items.value = itemsValue
            }

            override fun onDataNotAvailable() {
                isDataLoadingError.value = true
            }
        })
    }
}
