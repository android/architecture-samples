/*
 * Copyright 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.architecture.blueprints.todoapp.data.source.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.example.android.architecture.blueprints.todoapp.MainCoroutineRule
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class TaskDaoTest {

    // using an in-memory database because the information stored here disappears when the
    // process is killed
    private val database = Room.inMemoryDatabaseBuilder(
        getApplicationContext(),
        ToDoDatabase::class.java
    ).allowMainThreadQueries().build()

    // Ensure that we use an empty database for each test.
    @Before
    fun initDb() = database.clearAllTables()

    @Test
    fun insertTaskAndGetById() = runTest {
        // GIVEN - insert a task
        val task = LocalTask(
            title = "title",
            description = "description",
            id = "id",
            isCompleted = false,
        )
        database.taskDao().upsert(task)

        // WHEN - Get the task by id from the database
        val loaded = database.taskDao().getById(task.id)

        // THEN - The loaded data contains the expected values
        assertNotNull(loaded as LocalTask)
        assertEquals(task.id, loaded.id)
        assertEquals(task.title, loaded.title)
        assertEquals(task.description, loaded.description)
        assertEquals(task.isCompleted, loaded.isCompleted)
    }

    @Test
    fun insertTaskReplacesOnConflict() = runTest {
        // Given that a task is inserted
        val task = LocalTask(
            title = "title",
            description = "description",
            id = "id",
            isCompleted = false,
        )
        database.taskDao().upsert(task)

        // When a task with the same id is inserted
        val newTask = LocalTask(
            title = "title2",
            description = "description2",
            isCompleted = true,
            id = task.id
        )
        database.taskDao().upsert(newTask)

        // THEN - The loaded data contains the expected values
        val loaded = database.taskDao().getById(task.id)
        assertEquals(task.id, loaded?.id)
        assertEquals("title2", loaded?.title)
        assertEquals("description2", loaded?.description)
        assertEquals(true, loaded?.isCompleted)
    }

    @Test
    fun insertTaskAndGetTasks() = runTest {
        // GIVEN - insert a task
        val task = LocalTask(
            title = "title",
            description = "description",
            id = "id",
            isCompleted = false,
        )
        database.taskDao().upsert(task)

        // WHEN - Get tasks from the database
        val tasks = database.taskDao().getAll()

        // THEN - There is only 1 task in the database, and contains the expected values
        assertEquals(1, tasks.size)
        assertEquals(tasks[0].id, task.id)
        assertEquals(tasks[0].title, task.title)
        assertEquals(tasks[0].description, task.description)
        assertEquals(tasks[0].isCompleted, task.isCompleted)
    }

    @Test
    fun updateTaskAndGetById() = runTest {
        // When inserting a task
        val originalTask = LocalTask(
            title = "title",
            description = "description",
            id = "id",
            isCompleted = false,
        )

        database.taskDao().upsert(originalTask)

        // When the task is updated
        val updatedTask = LocalTask(
            title = "new title",
            description = "new description",
            isCompleted = true,
            id = originalTask.id
        )
        database.taskDao().upsert(updatedTask)

        // THEN - The loaded data contains the expected values
        val loaded = database.taskDao().getById(originalTask.id)
        assertEquals(originalTask.id, loaded?.id)
        assertEquals("new title", loaded?.title)
        assertEquals("new description", loaded?.description)
        assertEquals(true, loaded?.isCompleted)
    }

    @Test
    fun updateCompletedAndGetById() = runTest {
        // When inserting a task
        val task = LocalTask(
            title = "title",
            description = "description",
            id = "id",
            isCompleted = true
        )
        database.taskDao().upsert(task)

        // When the task is updated
        database.taskDao().updateCompleted(task.id, false)

        // THEN - The loaded data contains the expected values
        val loaded = database.taskDao().getById(task.id)
        assertEquals(task.id, loaded?.id)
        assertEquals(task.title, loaded?.title)
        assertEquals(task.description, loaded?.description)
        assertEquals(false, loaded?.isCompleted)
    }

    @Test
    fun deleteTaskByIdAndGettingTasks() = runTest {
        // Given a task inserted
        val task = LocalTask(
            title = "title",
            description = "description",
            id = "id",
            isCompleted = false,
        )
        database.taskDao().upsert(task)

        // When deleting a task by id
        database.taskDao().deleteById(task.id)

        // THEN - The list is empty
        val tasks = database.taskDao().getAll()
        assertEquals(true, tasks.isEmpty())
    }

    @Test
    fun deleteTasksAndGettingTasks() = runTest {
        // Given a task inserted
        database.taskDao().upsert(
            LocalTask(
                title = "title",
                description = "description",
                id = "id",
                isCompleted = false,
            )
        )

        // When deleting all tasks
        database.taskDao().deleteAll()

        // THEN - The list is empty
        val tasks = database.taskDao().getAll()
        assertEquals(true, tasks.isEmpty())
    }

    @Test
    fun deleteCompletedTasksAndGettingTasks() = runTest {
        // Given a completed task inserted
        database.taskDao().upsert(
            LocalTask(title = "completed", description = "task", id = "id", isCompleted = true)
        )

        // When deleting completed tasks
        database.taskDao().deleteCompleted()

        // THEN - The list is empty
        val tasks = database.taskDao().getAll()
        assertEquals(true, tasks.isEmpty())
    }
}
