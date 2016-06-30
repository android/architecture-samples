/*
 * Copyright 2016, The Android Open Source Project
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

package com.example.android.architecture.blueprints.todoapp.data;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource;
import com.example.android.architecture.blueprints.todoapp.data.source.local.TasksLocalDataSource;
import com.example.android.architecture.blueprints.todoapp.data.source.local.TasksPersistenceContract;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Integration test for the {@link TasksDataSource}, which uses the {@link com.example.android.architecture.blueprints.todoapp.data.source.TasksProvider}.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class TasksLocalDataSourceTest {

    private final static String TITLE = "title";

    private final static String TITLE2 = "title2";

    private final static String TITLE3 = "title3";

    private TasksLocalDataSource mLocalDataSource;
    private ContentResolver mContentResolver;

    @Before
    public void setup() {
        mContentResolver = InstrumentationRegistry.getTargetContext().getContentResolver();
        mLocalDataSource = TasksLocalDataSource.getInstance(mContentResolver);
    }

    @After
    public void cleanUp() {
        mLocalDataSource.deleteAllTasks();
    }

    @Test
    public void testPreConditions() {
        assertNotNull(mLocalDataSource);
    }

    @Test
    public void saveTask_retrievesTask() {
        // Given a new task
        final Task newTask = new Task(TITLE, "");

        // When saved into the persistent repository
        mLocalDataSource.saveTask(newTask);

        // Then the task can be retrieved from the persistent repository
        Cursor savedTaskCursor = mContentResolver.query(
                TasksPersistenceContract.TaskEntry.buildTasksUriWith(newTask.getId()),
                TasksPersistenceContract.TaskEntry.TASKS_COLUMNS,
                null,
                new String[]{String.valueOf(newTask.getId())},
                null
        );

        savedTaskCursor.moveToFirst();
        assertNotNull(savedTaskCursor);
        assertTrue(savedTaskCursor.getCount() >= 1);

        Task savedTask = Task.from(savedTaskCursor);
        assertThat(savedTask, is(newTask));
    }

    @Test
    public void completeTask_retrievedTaskIsComplete() {
        // Given a new task in the persistent repository
        final Task newTask = new Task(TITLE, "");
        mLocalDataSource.saveTask(newTask);

        // When completed in the persistent repository
        mLocalDataSource.completeTask(newTask);

        // Then the task can be retrieved from the persistent repository and is completed
        Cursor taskCursor = mContentResolver.query(
                TasksPersistenceContract.TaskEntry.buildTasksUriWith(newTask.getId()),
                TasksPersistenceContract.TaskEntry.TASKS_COLUMNS,
                null,
                new String[]{String.valueOf(newTask.getId())},
                null
        );
        taskCursor.moveToFirst();

        Task completedTask = Task.from(taskCursor);

        assertThat(completedTask, is(newTask));
        assertThat(completedTask.isCompleted(), is(true));
    }

    @Test
    public void activateTask_retrievedTaskIsActive() {
        // Given a new completed task in the persistent repository
        final Task newTask = new Task(TITLE, "");

        // And it's representative content values
        ContentValues values = new ContentValues();
        values.put(TasksPersistenceContract.TaskEntry.COLUMN_NAME_ENTRY_ID, newTask.getId());
        values.put(TasksPersistenceContract.TaskEntry.COLUMN_NAME_TITLE, newTask.getTitle());
        values.put(TasksPersistenceContract.TaskEntry.COLUMN_NAME_DESCRIPTION, newTask.getDescription());
        values.put(TasksPersistenceContract.TaskEntry.COLUMN_NAME_COMPLETED, newTask.isCompleted() ? 1 : 0);

        mLocalDataSource.saveTask(newTask);
        mLocalDataSource.completeTask(newTask);

        // When activated in the persistent repository
        mLocalDataSource.activateTask(newTask);

        // Then the task can be retrieved from the persistent repository and is active
        Cursor taskCursor = mContentResolver.query(
                TasksPersistenceContract.TaskEntry.buildTasksUriWith(newTask.getId()),
                TasksPersistenceContract.TaskEntry.TASKS_COLUMNS,
                null,
                new String[]{String.valueOf(newTask.getId())},
                null
        );
        taskCursor.moveToFirst();

        Task activeTask = Task.from(taskCursor);

        assertThat(activeTask.isCompleted(), is(false));
    }

    @Test
    public void clearCompletedTask_taskNotRetrievable() {
        // Given 2 new completed tasks and 1 active task in the persistent repository
        final Task newTask1 = new Task(TITLE, "");
        final Task newTask2 = new Task(TITLE2, "");
        final Task newTask3 = new Task(TITLE3, "");

        mLocalDataSource.saveTask(newTask1);
        mLocalDataSource.saveTask(newTask2);
        mLocalDataSource.saveTask(newTask3);

        mLocalDataSource.completeTask(newTask1);
        mLocalDataSource.completeTask(newTask2);

        // When completed tasks are cleared in the repository
        mLocalDataSource.clearCompletedTasks();

        // Then the completed tasks cannot be retrieved and the active one can
        Cursor task1Cursor = mContentResolver.query(
                TasksPersistenceContract.TaskEntry.buildTasksUriWith(newTask1.getId()),
                TasksPersistenceContract.TaskEntry.TASKS_COLUMNS,
                null,
                new String[]{String.valueOf(newTask1.getId())},
                null
        );
        assertFalse(task1Cursor.moveToFirst());

        Cursor task2Cursor = mContentResolver.query(
                TasksPersistenceContract.TaskEntry.buildTasksUriWith(newTask2.getId()),
                TasksPersistenceContract.TaskEntry.TASKS_COLUMNS,
                null,
                new String[]{String.valueOf(newTask2.getId())},
                null
        );
        assertFalse(task2Cursor.moveToFirst());

        Cursor task3Cursor = mContentResolver.query(
                TasksPersistenceContract.TaskEntry.buildTasksUriWith(newTask3.getId()),
                TasksPersistenceContract.TaskEntry.TASKS_COLUMNS,
                null,
                new String[]{String.valueOf(newTask3.getId())},
                null
        );
        assertTrue(task3Cursor.moveToFirst());
    }

    @Test
    public void deleteAllTasks_emptyListOfRetrievedTask() {
        // Given a new task in the persistent repository and a mocked callback
        Task newTask = new Task(TITLE, "");
        mLocalDataSource.saveTask(newTask);

        // When all tasks are deleted
        mLocalDataSource.deleteAllTasks();

        // Then the retrieved tasks is an empty list
        Cursor cursor = mContentResolver.query(
                TasksPersistenceContract.TaskEntry.buildTasksUri(),
                TasksPersistenceContract.TaskEntry.TASKS_COLUMNS,
                null,
                null,
                null
        );
        cursor.moveToFirst();
        assertNotNull(cursor);
        assertEquals(cursor.getCount(), 0);
    }

    @Test
    public void getTasks_retrieveSavedTasks() {
        // Given 2 new tasks in the persistent repository
        final Task newTask1 = new Task(TITLE, "");
        mLocalDataSource.saveTask(newTask1);
        final Task newTask2 = new Task(TITLE, "");
        mLocalDataSource.saveTask(newTask2);

        // Then the tasks can be retrieved from the persistent repository
        Cursor cursor = mContentResolver.query(
                TasksPersistenceContract.TaskEntry.buildTasksUri(),
                TasksPersistenceContract.TaskEntry.TASKS_COLUMNS,
                null,
                null,
                null
        );
        cursor.moveToFirst();
        assertNotNull(cursor);
        assertTrue(cursor.getCount() >= 2);

        List<Task> tasks = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                Task task = Task.from(cursor);
                tasks.add(task);
            } while (cursor.moveToNext());
        }

        boolean newTask1IdFound = false;
        boolean newTask2IdFound = false;
        for (Task task : tasks) {
            if (task.getId().equals(newTask1.getId())) {
                newTask1IdFound = true;
            }
            if (task.getId().equals(newTask2.getId())) {
                newTask2IdFound = true;
            }
        }
        assertTrue(newTask1IdFound);
        assertTrue(newTask2IdFound);
    }
}
