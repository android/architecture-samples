package com.example.android.architecture.blueprints.todoapp.addedittask

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.android.architecture.blueprints.todoapp.Event
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository

/**
 * ViewModel for the Add/Edit screen.
 *
 *
 * This ViewModel only exposes [ObservableField]s, so it doesn't need to extend
 * [androidx.databinding.BaseObservable] and updates are notified automatically. See
 * [com.example.android.architecture.blueprints.todoapp.statistics.StatisticsViewModel] for
 * how to deal with more complex scenarios.
 */
class AddEditTaskViewModel(
    context: Application,
    private val tasksRepository: TasksRepository
) : AndroidViewModel(context), TasksDataSource.GetTaskCallback {

    // Two-way databinding, exposing MutableLiveData
    val title = MutableLiveData<String>()

    // Two-way databinding, exposing MutableLiveData
    val description = MutableLiveData<String>()

    private val dataLoading = MutableLiveData<Boolean>()

    private val _snackbarText = MutableLiveData<Event<Int>>()

    private val _taskUpdated = MutableLiveData<Event<Any>>()

    private var taskId: String? = null

    private var isNewTask: Boolean = false

    private var isDataLoaded = false

    private var taskCompleted = false

    val snackbarMessage: LiveData<Event<Int>>
        get() = _snackbarText

    val taskUpdatedEvent: LiveData<Event<Any>>
        get() = _taskUpdated

    fun start(taskId: String?) {
        if (dataLoading.value != null && dataLoading.value!!) {
            // Already loading, ignore.
            return
        }
        this.taskId = taskId
        if (taskId == null) {
            // No need to populate, it's a new task
            isNewTask = true
            return
        }
        if (isDataLoaded) {
            // No need to populate, already have data.
            return
        }
        isNewTask = false
        dataLoading.value = true

        tasksRepository.getTask(taskId, this)
    }

    override fun onTaskLoaded(task: Task) {
        title.value = task.title
        description.value = task.description
        taskCompleted = task.isCompleted
        dataLoading.value = false
        isDataLoaded = true
    }

    override fun onDataNotAvailable() {
        dataLoading.value = false
    }

    // Called when clicking on fab.
    internal fun saveTask() {
        val currentTitle = title.value
        val currentDescription = title.value

        if (currentTitle == null || currentDescription == null) {
            _snackbarText.value =  Event(R.string.empty_task_message)
            return
        }
        if (Task(currentTitle, currentDescription ?: "").isEmpty) {
            _snackbarText.value =  Event(R.string.empty_task_message)
            return
        }

        val currentTaskId = taskId
        if (isNewTask || currentTaskId == null) {
            createTask(Task(currentTitle, currentDescription))
        } else {
            val task = Task(currentTitle, currentDescription, currentTaskId)
                .apply { isCompleted = taskCompleted }
            updateTask(task)
        }
    }

    fun getDataLoading(): LiveData<Boolean> {
        return dataLoading
    }

    private fun createTask(newTask: Task) {
        tasksRepository.saveTask(newTask)
        _taskUpdated.value = Event(Any())
    }

    private fun updateTask(task: Task) {
        if (isNewTask) {
            throw RuntimeException("updateTask() was called but task is new.")
        }
        tasksRepository.saveTask(task)
        _taskUpdated.value = Event(Any())
    }
}