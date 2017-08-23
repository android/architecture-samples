/*
 * Copyright 2017, The Android Open Source Project
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
package com.example.android.architecture.blueprints.todoapp.data.source.local

import android.content.ContentValues
import android.content.Context
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource
import com.example.android.architecture.blueprints.todoapp.data.source.local.TasksPersistenceContract.TaskEntry.COLUMN_NAME_COMPLETED
import com.example.android.architecture.blueprints.todoapp.data.source.local.TasksPersistenceContract.TaskEntry.COLUMN_NAME_DESCRIPTION
import com.example.android.architecture.blueprints.todoapp.data.source.local.TasksPersistenceContract.TaskEntry.COLUMN_NAME_ENTRY_ID
import com.example.android.architecture.blueprints.todoapp.data.source.local.TasksPersistenceContract.TaskEntry.COLUMN_NAME_TITLE
import com.example.android.architecture.blueprints.todoapp.data.source.local.TasksPersistenceContract.TaskEntry.TABLE_NAME

/**
 * Concrete implementation of a data source as a db.
 */
class TasksLocalDataSource private constructor(context: Context) : TasksDataSource {

    private val dbHelper: TasksDbHelper = TasksDbHelper(context)

    /**
     * Note: [TasksDataSource.LoadTasksCallback.onDataNotAvailable] is fired if the database doesn't exist
     * or the table is empty.
     */
    override fun getTasks(callback: TasksDataSource.LoadTasksCallback) {
        val db = dbHelper.readableDatabase

        val projection = arrayOf(COLUMN_NAME_ENTRY_ID, COLUMN_NAME_TITLE,
                COLUMN_NAME_DESCRIPTION, COLUMN_NAME_COMPLETED)

        val cursor = db.query(
                TABLE_NAME, projection, null, null, null, null, null)

        val tasks = ArrayList<Task>()
        with(cursor) {
            while (moveToNext()) {
                val itemId = getString(getColumnIndexOrThrow(COLUMN_NAME_ENTRY_ID))
                val title = getString(getColumnIndexOrThrow(COLUMN_NAME_TITLE))
                val description = getString(getColumnIndexOrThrow(COLUMN_NAME_DESCRIPTION))
                val task = Task(title, description, itemId).apply {
                    isCompleted = getInt(getColumnIndexOrThrow(COLUMN_NAME_COMPLETED)) == 1
                }
                tasks.add(task)
            }
            close()
        }
        db.close()

        if (!tasks.isEmpty()) {
            callback.onTasksLoaded(tasks)
        } else {
            // This will be called if the table is new or just empty.
            callback.onDataNotAvailable()
        }
    }

    /**
     * Note: [TasksDataSource.GetTaskCallback.onDataNotAvailable] is fired if the [Task] isn't
     * found.
     */
    override fun getTask(taskId: String, callback: TasksDataSource.GetTaskCallback) {
        val db = dbHelper.readableDatabase

        val projection = arrayOf(COLUMN_NAME_ENTRY_ID, COLUMN_NAME_TITLE,
                COLUMN_NAME_DESCRIPTION, COLUMN_NAME_COMPLETED)

        val cursor = db.query(
                TABLE_NAME, projection, "$COLUMN_NAME_ENTRY_ID LIKE ?", arrayOf(taskId), null,
                null, null)
        var task: Task? = null
        with(cursor) {
            if (moveToFirst()) {
                val itemId = getString(getColumnIndexOrThrow(COLUMN_NAME_ENTRY_ID))
                val title = getString(getColumnIndexOrThrow(COLUMN_NAME_TITLE))
                val description = getString(getColumnIndexOrThrow(COLUMN_NAME_DESCRIPTION))
                 task = Task(title, description, itemId).apply {
                    isCompleted = getInt(getColumnIndexOrThrow(COLUMN_NAME_COMPLETED)) == 1
                }
            }
            close()
        }
        db.close()
        task?.let { callback.onTaskLoaded(it) } ?: callback.onDataNotAvailable()
    }

    override fun saveTask(task: Task) {
        val values = ContentValues().apply {
            put(COLUMN_NAME_ENTRY_ID, task.id)
            put(COLUMN_NAME_TITLE, task.title)
            put(COLUMN_NAME_DESCRIPTION, task.description)
            put(COLUMN_NAME_COMPLETED, task.isCompleted)
        }
        with(dbHelper.writableDatabase) {
            insert(TABLE_NAME, null, values)
            close()
        }
    }

    override fun completeTask(task: Task) {
        val values = ContentValues().apply {
            put(COLUMN_NAME_COMPLETED, true)
        }
        with(dbHelper.writableDatabase) {
            update(TABLE_NAME, values, "$COLUMN_NAME_ENTRY_ID LIKE ?", arrayOf(task.id))
            close()
        }
    }

    override fun completeTask(taskId: String) {
        // Not required for the local data source because the {@link TasksRepository} handles
        // converting from a {@code taskId} to a {@link task} using its cached data.
    }

    override fun activateTask(task: Task) {
        val values = ContentValues().apply {
            put(COLUMN_NAME_COMPLETED, false)
        }

        with(dbHelper.writableDatabase) {
            update(TABLE_NAME, values, "$COLUMN_NAME_ENTRY_ID LIKE ?", arrayOf(task.id))
            close()
        }
    }

    override fun activateTask(taskId: String) {
        // Not required for the local data source because the {@link TasksRepository} handles
        // converting from a {@code taskId} to a {@link task} using its cached data.
    }

    override fun clearCompletedTasks() {
        val selection = "$COLUMN_NAME_COMPLETED LIKE ?"
        val selectionArgs = arrayOf("1")
        with(dbHelper.writableDatabase) {
            delete(TABLE_NAME, selection, selectionArgs)
            close()
        }
    }

    override fun refreshTasks() {
        // Not required because the {@link TasksRepository} handles the logic of refreshing the
        // tasks from all the available data sources.
    }

    override fun deleteAllTasks() {
        with(dbHelper.writableDatabase) {
            delete(TABLE_NAME, null, null)
            close()
        }
    }

    override fun deleteTask(taskId: String) {
        val selection = "$COLUMN_NAME_ENTRY_ID LIKE ?"
        val selectionArgs = arrayOf(taskId)
        with(dbHelper.writableDatabase) {
            delete(TABLE_NAME, selection, selectionArgs)
            close()
        }
    }

    companion object {
        private var INSTANCE: TasksLocalDataSource? = null

        @JvmStatic fun getInstance(context: Context) =
                INSTANCE ?: TasksLocalDataSource(context).apply { INSTANCE = this }
    }
}
