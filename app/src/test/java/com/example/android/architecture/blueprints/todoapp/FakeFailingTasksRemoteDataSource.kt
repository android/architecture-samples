package com.example.android.architecture.blueprints.todoapp

import com.example.android.architecture.blueprints.todoapp.data.Result
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource

object FakeFailingTasksRemoteDataSource : TasksDataSource {
    override suspend fun getTasks(): Result<List<Task>> {
        return Result.Error(Exception("Test"))
    }

    override suspend fun getTask(taskId: String): Result<Task> {
        return Result.Error(Exception("Test"))
    }

    override fun saveTask(task: Task) {
        TODO("not implemented")
    }

    override fun completeTask(task: Task) {
        TODO("not implemented")
    }

    override fun completeTask(taskId: String) {
        TODO("not implemented")
    }

    override fun activateTask(task: Task) {
        TODO("not implemented")
    }

    override fun activateTask(taskId: String) {
        TODO("not implemented")
    }

    override fun clearCompletedTasks() {
        TODO("not implemented")
    }

    override fun refreshTasks() {
        TODO("not implemented")
    }

    override fun deleteAllTasks() {
        TODO("not implemented")
    }

    override fun deleteTask(taskId: String) {
        TODO("not implemented")
    }
}
