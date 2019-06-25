package com.example.android.architecture.blueprints.todoapp.domain

import com.example.android.architecture.blueprints.todoapp.data.Result
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository

class GetTaskUseCase(
    private val tasksRepository: TasksRepository
) {
    suspend operator fun invoke(taskId: String, forceUpdate: Boolean = false): Result<Task> {

        wrapEspressoIdlingResource {
            return tasksRepository.getTask(taskId, forceUpdate)
        }
    }

}