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

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.example.android.architecture.blueprints.todoapp.data.Task
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class TasksDaoTest {

    private lateinit var database: ToDoDatabase

    @Before
    fun initDb() {
        // using an in-memory database because the information stored here disappears when the
        // process is killed
        database = Room.inMemoryDatabaseBuilder(
            getApplicationContext(),
            ToDoDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @After
    fun closeDb() = database.close()

    @Test
    fun insertTaskAndGetById() = runBlocking {
        // GIVEN - insert a task
        val task = Task("title", "description", true, "id")
        database.taskDao().insertTask(task)

        // WHEN - Get the task by id from the database
        val loaded = database.taskDao().getTaskById(DEFAULT_TASK.id)

        // THEN - The loaded data contains the expected values
        assertThat<Task>(loaded as Task, notNullValue())
        assertThat(loaded.id, `is`(task.id))
        assertThat(loaded.title, `is`(task.title))
        assertThat(loaded.description, `is`(task.description))
        assertThat(loaded.isCompleted, `is`(task.isCompleted))
    }

    @Test
    fun insertTaskReplacesOnConflict() = runBlocking {
        // Given that a task is inserted
        database.taskDao().insertTask(DEFAULT_TASK)

        // When a task with the same id is inserted
        val newTask = Task(NEW_TITLE, NEW_DESCRIPTION, NEW_IS_COMPLETED, DEFAULT_ID)
        database.taskDao().insertTask(newTask)

        // When getting the task by id from the database
        val loaded = database.taskDao().getTaskById(DEFAULT_TASK.id)

        // The loaded data contains the expected values
        assertTask(loaded, DEFAULT_ID, NEW_TITLE, NEW_DESCRIPTION, NEW_IS_COMPLETED)
    }

    @Test
    fun insertTaskAndGetTasks() = runBlocking {
        // GIVEN - insert a task
        val task = Task("title", "description", true, "id")
        database.taskDao().insertTask(task)

        // WHEN - Get tasks from the database
        val tasks = database.taskDao().getTasks()

        // THEN - There is only 1 task in the database, and contains the expected values
        assertThat(tasks.size, `is`(1))
        assertThat<Task>(tasks[0] as Task, notNullValue())
        assertThat(tasks[0].id, `is`(task.id))
        assertThat(tasks[0].title, `is`(task.title))
        assertThat(tasks[0].description, `is`(task.description))
        assertThat(tasks[0].isCompleted, `is`(task.isCompleted))
    }

    @Test
    fun updateTaskAndGetById() = runBlocking {
        // When inserting a task
        database.taskDao().insertTask(DEFAULT_TASK)

        // When the task is updated
        val updatedTask = Task(NEW_TITLE, NEW_DESCRIPTION, NEW_IS_COMPLETED, DEFAULT_ID)
        database.taskDao().updateTask(updatedTask)

        // When getting the task by id from the database
        val loaded = database.taskDao().getTaskById(DEFAULT_ID)

        // The loaded data contains the expected values
        assertTask(loaded, DEFAULT_ID, NEW_TITLE, NEW_DESCRIPTION, NEW_IS_COMPLETED)
    }

    @Test
    fun updateCompletedAndGetById() = runBlocking {
        // When inserting a task
        database.taskDao().insertTask(DEFAULT_TASK)

        // When the task is updated
        database.taskDao().updateCompleted(DEFAULT_TASK.id, false)

        // When getting the task by id from the database
        val loaded = database.taskDao().getTaskById(DEFAULT_ID)

        // The loaded data contains the expected values
        assertTask(loaded, DEFAULT_TASK.id, DEFAULT_TASK.title, DEFAULT_TASK.description, false)
    }

    @Test
    fun deleteTaskByIdAndGettingTasks() = runBlocking {
        // Given a task inserted
        database.taskDao().insertTask(DEFAULT_TASK)

        // When deleting a task by id
        database.taskDao().deleteTaskById(DEFAULT_TASK.id)

        // When getting the tasks
        val tasks = database.taskDao().getTasks()

        // The list is empty
        assertThat(tasks.size, `is`(0))
    }

    @Test
    fun deleteTasksAndGettingTasks() = runBlocking {
        // Given a task inserted
        database.taskDao().insertTask(DEFAULT_TASK)

        // When deleting all tasks
        database.taskDao().deleteTasks()

        // When getting the tasks
        val tasks = database.taskDao().getTasks()

        // The list is empty
        assertThat(tasks.size, `is`(0))
    }

    @Test
    fun deleteCompletedTasksAndGettingTasks() = runBlocking {
        // Given a completed task inserted
        database.taskDao().insertTask(DEFAULT_TASK)

        // When deleting completed tasks
        database.taskDao().deleteCompletedTasks()

        // When getting the tasks
        val tasks = database.taskDao().getTasks()

        // The list is empty
        assertThat(tasks.size, `is`(0))
    }

    private fun assertTask(
        task: Task?,
        id: String,
        title: String,
        description: String,
        completed: Boolean
    ) {
        assertThat<Task>(task as Task, notNullValue())
        assertThat(task.id, `is`(id))
        assertThat(task.title, `is`(title))
        assertThat(task.description, `is`(description))
        assertThat(task.isCompleted, `is`(completed))
    }

    companion object {

        private val DEFAULT_TITLE = "title"
        private val DEFAULT_DESCRIPTION = "description"
        private val DEFAULT_ID = "id"
        private val DEFAULT_IS_COMPLETED = true
        private val DEFAULT_TASK = Task(DEFAULT_TITLE, DEFAULT_DESCRIPTION, DEFAULT_IS_COMPLETED,
          DEFAULT_ID)

        private val NEW_TITLE = "title2"
        private val NEW_DESCRIPTION = "description2"
        private val NEW_IS_COMPLETED = true
    }
}