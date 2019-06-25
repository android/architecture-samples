package com.example.android.architecture.blueprints.todoapp.domain

import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository

class SaveTaskUseCase(
    private val tasksRepository: TasksRepository
) {
    suspend operator fun invoke(task: Task) {

        wrapEspressoIdlingResource {
            return tasksRepository.saveTask(task)
        }
    }

}