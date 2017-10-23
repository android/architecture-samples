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

package com.example.android.architecture.blueprints.todoapp.data.source.local;

import android.arch.persistence.room.Room;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.example.android.architecture.blueprints.todoapp.tasks.domain.model.Task;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(AndroidJUnit4.class)
public class TasksDaoTest {

    private static final Task TASK = new Task("title", "description", "id", true);

    private ToDoDatabase mDatabase;

    @Before
    public void initDb() {
        // using an in-memory database because the information stored here disappears when the
        // process is killed
        mDatabase = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getContext(),
                ToDoDatabase.class).build();
    }

    @After
    public void closeDb() {
        mDatabase.close();
    }

    @Test
    public void insertTaskAndGetById() {
        // When inserting a task
        mDatabase.taskDao().insertTask(TASK);

        // When getting the task by id from the database
        Task loaded = mDatabase.taskDao().getTaskById(TASK.getId());

        // The loaded data contains the expected values
        assertTask(loaded, "id", "title", "description", true);
    }

    @Test
    public void insertTaskReplacesOnConflict() {
        //Given that a task is inserted
        mDatabase.taskDao().insertTask(TASK);

        // When a task with the same id is inserted
        Task newTask = new Task("title2", "description2", "id", true);
        mDatabase.taskDao().insertTask(newTask);
        // When getting the task by id from the database
        Task loaded = mDatabase.taskDao().getTaskById(TASK.getId());

        // The loaded data contains the expected values
        assertTask(loaded, "id", "title2", "description2", true);
    }

    @Test
    public void insertTaskAndGetTasks() {
        // When inserting a task
        mDatabase.taskDao().insertTask(TASK);

        // When getting the tasks from the database
        List<Task> tasks = mDatabase.taskDao().getTasks();

        // There is only 1 task in the database
        assertThat(tasks.size(), is(1));
        // The loaded data contains the expected values
        assertTask(tasks.get(0), "id", "title", "description", true);
    }

    @Test
    public void updateTaskAndGetById() {
        // When inserting a task
        mDatabase.taskDao().insertTask(TASK);

        // When the task is updated
        Task updatedTask = new Task("title2", "description2", "id", true);
        mDatabase.taskDao().updateTask(updatedTask);

        // When getting the task by id from the database
        Task loaded = mDatabase.taskDao().getTaskById("id");

        // The loaded data contains the expected values
        assertTask(loaded, "id", "title2", "description2", true);
    }

    @Test
    public void updateCompletedAndGetById() {
        // When inserting a task
        mDatabase.taskDao().insertTask(TASK);

        // When the task is updated
        mDatabase.taskDao().updateCompleted(TASK.getId(), false);

        // When getting the task by id from the database
        Task loaded = mDatabase.taskDao().getTaskById("id");

        // The loaded data contains the expected values
        assertTask(loaded, TASK.getId(), TASK.getTitle(), TASK.getDescription(), false);
    }

    @Test
    public void deleteTaskByIdAndGettingTasks() {
        //Given a task inserted
        mDatabase.taskDao().insertTask(TASK);

        //When deleting a task by id
        mDatabase.taskDao().deleteTaskById(TASK.getId());

        //When getting the tasks
        List<Task> tasks = mDatabase.taskDao().getTasks();
        // The list is empty
        assertThat(tasks.size(), is(0));
    }

    @Test
    public void deleteTasksAndGettingTasks() {
        //Given a task inserted
        mDatabase.taskDao().insertTask(TASK);

        //When deleting all tasks
        mDatabase.taskDao().deleteTasks();

        //When getting the tasks
        List<Task> tasks = mDatabase.taskDao().getTasks();
        // The list is empty
        assertThat(tasks.size(), is(0));
    }

    @Test
    public void deleteCompletedTasksAndGettingTasks() {
        //Given a completed task inserted
        mDatabase.taskDao().insertTask(TASK);

        //When deleting completed tasks
        mDatabase.taskDao().deleteCompletedTasks();

        //When getting the tasks
        List<Task> tasks = mDatabase.taskDao().getTasks();
        // The list is empty
        assertThat(tasks.size(), is(0));
    }

    private void assertTask(Task task, String id, String title,
            String description, boolean completed) {
        assertThat(task, notNullValue());
        assertThat(task.getId(), is(id));
        assertThat(task.getTitle(), is(title));
        assertThat(task.getDescription(), is(description));
        assertThat(task.isCompleted(), is(completed));
    }
}
