/*
 * Copyright (C) 2017 The Android Open Source Project
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

package com.example.android.architecture.blueprints.todoapp

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.room.Room
import com.example.android.architecture.blueprints.todoapp.data.source.DefaultTasksRepository
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository
import com.example.android.architecture.blueprints.todoapp.data.source.local.TasksLocalDataSource
import com.example.android.architecture.blueprints.todoapp.data.source.local.ToDoDatabase
import com.example.android.architecture.blueprints.todoapp.data.source.remote.TasksRemoteDataSource
import kotlinx.coroutines.runBlocking

/**
 * A Service Locator for the [TasksRepository]. This is the prod version, with
 * the "real" [TasksRemoteDataSource].
 */
object ServiceLocator {

	private val lock = Any()
	private var database: ToDoDatabase? = null
	@Volatile
	var tasksRepository: TasksRepository? = null
		@VisibleForTesting set

	fun provideTasksRepository(context: Context): TasksRepository {
		synchronized(this) {
			return tasksRepository ?: tasksRepository ?: createTasksRepository(context)
		}
	}

	private fun createTasksRepository(context: Context): TasksRepository {
		return DefaultTasksRepository(TasksRemoteDataSource, createTaskLocalDataSource(context))
	}

	private fun createTaskLocalDataSource(context: Context): TasksDataSource {
		val database = database ?: createDataBase(context)
		return TasksLocalDataSource(database.taskDao())
	}

	private fun createDataBase(context: Context): ToDoDatabase {
		val result = Room.databaseBuilder(
				context.applicationContext,
				ToDoDatabase::class.java, "Tasks.db"
		).build()
		database = result
		return result
	}

	@VisibleForTesting
	fun resetRepository() {
		synchronized(lock) {
			runBlocking {
				TasksRemoteDataSource.deleteAllTasks()
			}
			// Clear all data to avoid test pollution.
			database?.apply {
				clearAllTables()
				close()
			}
			database = null
			tasksRepository = null
		}
	}
}
