package com.example.android.architecture.blueprints.todoapp.domain

import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository
import com.example.android.architecture.blueprints.todoapp.util.wrapEspressoIdlingResource

class DeleteTaskUseCase(
    private val tasksRepository: TasksRepository
) {
    suspend operator fun invoke(taskId: String) {

        wrapEspressoIdlingResource {
            return tasksRepository.deleteTask(taskId)
        }
    }

}