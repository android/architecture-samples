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

import android.content.ContentValues;
import android.database.Cursor;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource;
import com.example.android.architecture.blueprints.todoapp.data.source.local.TasksLocalDataSource;
import com.example.android.architecture.blueprints.todoapp.data.source.local.TasksPersistenceContract;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.*;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.isNull;

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

    @Before
    public void setup() {
        mLocalDataSource = TasksLocalDataSource.getInstance(
                InstrumentationRegistry.getTargetContext());
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
        Cursor savedTaskCursor = mLocalDataSource.getTask(newTask.getId());
        Task savedTask = Task.from(savedTaskCursor);
        assertThat(savedTask, is(newTask));
    }

    @Test
    public void completeTask_retrievedTaskIsComplete() {
        // Given a new task in the persistent repository
        final Task newTask = new Task(TITLE, "");

        // And it's representative content values
        ContentValues values = new ContentValues();
        values.put(TasksPersistenceContract.TaskEntry.COLUMN_NAME_ENTRY_ID, newTask.getId());
        values.put(TasksPersistenceContract.TaskEntry.COLUMN_NAME_TITLE, newTask.getTitle());
        values.put(TasksPersistenceContract.TaskEntry.COLUMN_NAME_DESCRIPTION, newTask.getDescription());
        values.put(TasksPersistenceContract.TaskEntry.COLUMN_NAME_COMPLETED, newTask.isCompleted() ? 1 : 0);

        String[] selectionArgs = {newTask.getId()};

        mLocalDataSource.saveTask(newTask);

        // When completed in the persistent repository
        mLocalDataSource.updateTask(values, selectionArgs);

        // Then the task can be retrieved from the persistent repository and is completed
        assertThat(Task.from(mLocalDataSource.getTask(newTask.getId())), is(newTask));
        assertThat(Task.from(mLocalDataSource.getTask(newTask.getId())).isCompleted(), is(true));
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

        String[] selectionArgs = {newTask.getId()};

        mLocalDataSource.saveTask(newTask);
        mLocalDataSource.updateTask(values, selectionArgs);

        // When activated in the persistent repository
        mLocalDataSource.updateTask(values, selectionArgs);

        // Then the task can be retrieved from the persistent repository and is active
        mLocalDataSource.getTask(newTask.getId());

        assertThat(newTask.isCompleted(), is(false));
    }

    @Test
    public void clearCompletedTask_taskNotRetrievable() {
        // Given 2 new completed tasks and 1 active task in the persistent repository
        final Task newTask1 = new Task(TITLE, "");

        // And it's representative content values
        ContentValues task1Values = new ContentValues();
        task1Values.put(TasksPersistenceContract.TaskEntry.COLUMN_NAME_ENTRY_ID, newTask1.getId());
        task1Values.put(TasksPersistenceContract.TaskEntry.COLUMN_NAME_TITLE, newTask1.getTitle());
        task1Values.put(TasksPersistenceContract.TaskEntry.COLUMN_NAME_DESCRIPTION, newTask1.getDescription());
        task1Values.put(TasksPersistenceContract.TaskEntry.COLUMN_NAME_COMPLETED, newTask1.isCompleted() ? 1 : 0);

        String[] task1SelectionArgs = {newTask1.getId()};

        final Task newTask2 = new Task(TITLE2, "");
        // And it's representative content values
        ContentValues task2Values = new ContentValues();
        task1Values.put(TasksPersistenceContract.TaskEntry.COLUMN_NAME_ENTRY_ID, newTask2.getId());
        task1Values.put(TasksPersistenceContract.TaskEntry.COLUMN_NAME_TITLE, newTask2.getTitle());
        task1Values.put(TasksPersistenceContract.TaskEntry.COLUMN_NAME_DESCRIPTION, newTask2.getDescription());
        task1Values.put(TasksPersistenceContract.TaskEntry.COLUMN_NAME_COMPLETED, newTask2.isCompleted() ? 1 : 0);

        String[] task2SelectionArgs = {newTask2.getId()};

        final Task newTask3 = new Task(TITLE3, "");

        mLocalDataSource.saveTask(newTask1);
        mLocalDataSource.updateTask(task1Values, task1SelectionArgs);

        mLocalDataSource.saveTask(newTask2);
        mLocalDataSource.updateTask(task2Values, task2SelectionArgs);

        mLocalDataSource.saveTask(newTask3);

        // When completed tasks are cleared in the repository
        String selection = TasksPersistenceContract.TaskEntry.COLUMN_NAME_COMPLETED + " LIKE ?";
        String[] selectionArgs = {"1"};

        mLocalDataSource.clearCompletedTasks(selection, selectionArgs);

        // Then the completed tasks cannot be retrieved and the active one can
        assertThat(mLocalDataSource.getTask(newTask1.getId()), is(isNull()));
        assertThat(mLocalDataSource.getTask(newTask2.getId()), is(isNull()));
        assertThat(mLocalDataSource.getTask(newTask3.getId()), notNullValue());
    }

    @Test
    public void deleteAllTasks_emptyListOfRetrievedTask() {
        // Given a new task in the persistent repository and a mocked callback
        Task newTask = new Task(TITLE, "");
        mLocalDataSource.saveTask(newTask);

        // When all tasks are deleted
        mLocalDataSource.deleteAllTasks();

        // Then the retrieved tasks is an empty list
        Cursor cursor = mLocalDataSource.getTasks(null, null);
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
        Cursor cursor = mLocalDataSource.getTasks(null, null);
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
