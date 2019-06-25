package com.example.android.architecture.blueprints.todoapp.domain

import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository

class ClearCompletedTasksUseCase(
    private val tasksRepository: TasksRepository
) {
    suspend operator fun invoke() {

        wrapEspressoIdlingResource {
            tasksRepository.clearCompletedTasks()
        }
    }
}