/*
 * Copyright (C) 2019 The Android Open Source Project
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
package com.example.android.architecture.blueprints.todoapp.data.source.remote

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.example.android.architecture.blueprints.todoapp.data.Result
import com.example.android.architecture.blueprints.todoapp.data.Result.Error
import com.example.android.architecture.blueprints.todoapp.data.Result.Success
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource
import com.example.android.architecture.blueprints.todoapp.data.source.remote.mapper.TasksResponseModelMapper
import kotlinx.coroutines.delay

/**
 * Implementation of the data source that adds a latency simulating network.
 */
class TasksRemoteDataSource : TasksDataSource {

    val mapper = TasksResponseModelMapper()
    val service = TodoServiceFactory.makeGithubTrendingService(true);

    override fun observeTasks(): LiveData<Result<List<Task>>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun getTasks(): Result<List<Task>> {
     val result
                = service.getTasks("user345").map {
            mapper.mapFromModel(it)
        }

        return Success(result)
    }

    override suspend fun refreshTasks() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun observeTask(taskId: String): LiveData<Result<Task>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun getTask(taskId: String): Result<Task> {
        val result
                = service.getTask("user345",taskId).map {
            mapper.mapFromModel(it)
        }
        val task = result.get(0);

        return Success(task)
    }

    override suspend fun refreshTask(taskId: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun saveTask(task: Task) {
       val model = TasksModel(
               id = task.taskID,
               user = task.userName,
               title = task.title,
               description = task.description,
               completed=task.isCompleted,
               priority = "MEDIUM"
               )
        val service = TodoServiceFactory.makeGithubTrendingService(true);
        val modelList = listOf<TasksModel>(mapper.mapToModel(task))
        val ans  = service.createOrUpdate("user345",modelList)

    }


    override suspend fun completeTask(task: Task) {
        val model = TasksModel(
                id = task.taskID,
                user = task.userName,
                title = task.title,
                description = task.description,
                completed=true,
                priority = task.priority
        )
        val service = TodoServiceFactory.makeGithubTrendingService(true);
        val modelList = listOf<TasksModel>(model)
        val ans  = service.createOrUpdate("user345",modelList)

    }

    override suspend fun completeTask(taskId: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun activateTask(task: Task) {

    }

    override suspend fun activateTask(taskId: String) {

    }

    override suspend fun clearCompletedTasks() {

    }

    override suspend fun deleteAllTasks() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun deleteTask(taskId: String) {
        TodoServiceFactory.makeGithubTrendingService(true).delete(taskId)
    }

}
