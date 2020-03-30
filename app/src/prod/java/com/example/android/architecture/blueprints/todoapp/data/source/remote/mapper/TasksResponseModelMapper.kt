package com.example.android.architecture.blueprints.todoapp.data.source.remote.mapper

import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.remote.TasksModel

class TasksResponseModelMapper : ModelMapper<TasksModel, Task> {

    override fun mapFromModel(model: TasksModel): Task {
        return Task(title = model.title,description = model.description,
                isCompleted = model.completed,
                taskID =model.id,
                priority = model.priority,
                userName = model.user)
    }

    override fun mapToModel(task: Task): TasksModel {
        return  TasksModel(
                id = task.taskID,
                user = task.userName,
                title = task.title,
                description = task.description,
                completed=task.isCompleted,
                priority = task.priority
        )
    }


}