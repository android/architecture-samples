package com.example.android.architecture.blueprints.todoapp.tasks

import android.app.Application
import android.content.Context
import android.graphics.drawable.Drawable
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

    private val _dataLoading = MutableLiveData<Boolean>()

    private val _currentFilteringLabel = MutableLiveData<String>()

    private val _noTasksLabel = MutableLiveData<String>()

    private val _noTaskIconRes = MutableLiveData<Drawable>()

    private val _tasksAddViewVisible = MutableLiveData<Boolean>()

    private val _snackbarText = MutableLiveData<Event<Int>>()

    private var _currentFiltering = TasksFilterType.ALL_TASKS

    // Not used at the moment
    private val isDataLoadingError = MutableLiveData<Boolean>()

    private val _openTaskEvent = MutableLiveData<Event<String>>()

    private val _newTaskEvent = MutableLiveData<Event<Any>>()

    // To prevent leaks, this must be an Application Context.
    private val context: Context = context.applicationContext

    // This LiveData depends on another so we can use a transformation.
    val empty = Transformations.map(_items) {
        it.isEmpty()
    }

    // LiveData getters

    val tasksAddViewVisible: LiveData<Boolean>
        get() = _tasksAddViewVisible

    val isDataLoading: LiveData<Boolean>
        get() = _dataLoading

    val currentFilteringLabel: LiveData<String>
        get() = _currentFilteringLabel

    val noTasksLabel: LiveData<String>
        get() = _noTasksLabel

    val noTaskIconRes: LiveData<Drawable>
        get() = _noTaskIconRes

    val snackbarMessage: LiveData<Event<Int>>
        get() = _snackbarText

    val openTaskEvent: LiveData<Event<String>>
        get() = _openTaskEvent

    val newTaskEvent: LiveData<Event<Any>>
        get() = _newTaskEvent

    val items: LiveData<List<Task>>
        get() = _items

    init {
        // Force use of Application Context.

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
                _currentFilteringLabel.value = context.getString(R.string.label_all)
                _noTasksLabel.value = context.resources.getString(R.string.no_tasks_all)
                _noTaskIconRes.value = context.resources.getDrawable(
                    R.drawable.ic_assignment_turned_in_24dp
                )
                _tasksAddViewVisible.setValue(true)
            }
            TasksFilterType.ACTIVE_TASKS -> {
                _currentFilteringLabel.value = context.getString(R.string.label_active)
                _noTasksLabel.value = context.resources.getString(R.string.no_tasks_active)
                _noTaskIconRes.value = context.resources.getDrawable(
                    R.drawable.ic_check_circle_24dp
                )
                _tasksAddViewVisible.setValue(false)
            }
            TasksFilterType.COMPLETED_TASKS -> {
                _currentFilteringLabel.value = context.getString(R.string.label_completed)
                _noTasksLabel.value = context.resources.getString(R.string.no_tasks_completed)
                _noTaskIconRes.value = context.resources.getDrawable(
                    R.drawable.ic_verified_user_24dp
                )
                _tasksAddViewVisible.setValue(false)
            }
        }
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
        _newTaskEvent.value = Event(Any())
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