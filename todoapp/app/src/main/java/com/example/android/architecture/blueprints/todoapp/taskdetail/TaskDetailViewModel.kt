package com.example.android.architecture.blueprints.todoapp.taskdetail

import android.app.Application
import androidx.annotation.StringRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.example.android.architecture.blueprints.todoapp.Event
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository

/**
 * Listens to user actions from the list item in ([TasksFragment]) and redirects them to the
 * Fragment's actions listener.
 */
open class TaskDetailViewModel(context: Application, private val tasksRepository: TasksRepository) :
    AndroidViewModel(context), TasksDataSource.GetTaskCallback {

    private val _task = MutableLiveData<Task>()

    private val _isDataAvailable = MutableLiveData<Boolean>()

    private val _dataLoading = MutableLiveData<Boolean>()

    val editTaskCommand = MutableLiveData<Event<Any>>()

    val deleteTaskCommand = MutableLiveData<Event<Any>>()

    private val _snackbarText = MutableLiveData<Event<Int>>()

    // This LiveData depends on another so we can use a transformation.
    val completed: LiveData<Boolean> = Transformations.map(_task) {
            input -> input.isCompleted
    }

    val snackbarMessage: LiveData<Event<Int>>
        get() = _snackbarText

    val task: LiveData<Task>
        get() = _task

    val isDataAvailable: LiveData<Boolean>
        get() = _isDataAvailable

    val dataLoading: LiveData<Boolean>
        get() = _dataLoading

    protected val taskId: String?
        get() = _task.value!!.id

    fun deleteTask() {
        if (_task.value != null) {
            tasksRepository.deleteTask(_task.value!!.id)
            deleteTaskCommand.value = Event(Any())
        }
    }

    fun editTask() {
        editTaskCommand.value = Event(Any())
    }

    fun setCompleted(completed: Boolean) {
        if (_dataLoading.value!!) {
            return
        }
        val task = this._task.value
        if (completed) {
            tasksRepository.completeTask(task!!)
            showSnackbarMessage(R.string.task_marked_complete)
        } else {
            tasksRepository.activateTask(task!!)
            showSnackbarMessage(R.string.task_marked_active)
        }
    }

    fun start(taskId: String?) {
        if (taskId != null) {
            _dataLoading.value = true
            tasksRepository.getTask(taskId, this)
        }
    }

    private fun setTask(task: Task?) {
        this._task.value = task
        _isDataAvailable.value = task != null
    }

    override fun onTaskLoaded(task: Task) {
        setTask(task)
        _dataLoading.value = false
    }

    override fun onDataNotAvailable() {
        _task.value = null
        _dataLoading.value = false
        _isDataAvailable.value = false
    }

    fun onRefresh() {
        if (_task.value != null) {
            start(_task.value!!.id)
        }
    }

    private fun showSnackbarMessage(@StringRes message: Int) {
        _snackbarText.value = Event(message)
    }
}