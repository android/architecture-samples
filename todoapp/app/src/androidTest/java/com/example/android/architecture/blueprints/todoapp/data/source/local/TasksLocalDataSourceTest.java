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

package com.example.android.architecture.blueprints.todoapp.data.source.local;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import android.arch.persistence.room.Room;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource;
import com.example.android.architecture.blueprints.todoapp.util.SingleExecutors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

/**
 * Integration test for the {@link TasksDataSource}.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class TasksLocalDataSourceTest {

    private final static String TITLE = "title";

    private final static String TITLE2 = "title2";

    private final static String TITLE3 = "title3";

    private TasksLocalDataSource mLocalDataSource;

    private ToDoDatabase mDatabase;

    @Before
    public void setup() {
        // using an in-memory database for testing, since it doesn't survive killing the process
        mDatabase = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getContext(),
                ToDoDatabase.class)
                .build();
        TasksDao tasksDao = mDatabase.taskDao();

        // Make sure that we're not keeping a reference to the wrong instance.
        TasksLocalDataSource.clearInstance();
        mLocalDataSource = TasksLocalDataSource.getInstance(new SingleExecutors(), tasksDao);
    }

    @After
    public void cleanUp() {
        mDatabase.close();
        TasksLocalDataSource.clearInstance();
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
        mLocalDataSource.getTask(newTask.getId(), new TasksDataSource.GetTaskCallback() {
            @Override
            public void onTaskLoaded(Task task) {
                assertThat(task, is(newTask));
            }

            @Override
            public void onDataNotAvailable() {
                fail("Callback error");
            }
        });
    }

    @Test
    public void completeTask_retrievedTaskIsComplete() {
        // Initialize mock for the callback.
        TasksDataSource.GetTaskCallback callback = mock(TasksDataSource.GetTaskCallback.class);
        // Given a new task in the persistent repository
        final Task newTask = new Task(TITLE, "");
        mLocalDataSource.saveTask(newTask);

        // When completed in the persistent repository
        mLocalDataSource.completeTask(newTask);

        // Then the task can be retrieved from the persistent repository and is complete
        mLocalDataSource.getTask(newTask.getId(), new TasksDataSource.GetTaskCallback() {
            @Override
            public void onTaskLoaded(Task task) {
                assertThat(task, is(newTask));
                assertThat(task.isCompleted(), is(true));
            }

            @Override
            public void onDataNotAvailable() {
                fail("Callback error");
            }
        });
    }

    @Test
    public void activateTask_retrievedTaskIsActive() {
        // Initialize mock for the callback.
        TasksDataSource.GetTaskCallback callback = mock(TasksDataSource.GetTaskCallback.class);

        // Given a new completed task in the persistent repository
        final Task newTask = new Task(TITLE, "");
        mLocalDataSource.saveTask(newTask);
        mLocalDataSource.completeTask(newTask);

        // When activated in the persistent repository
        mLocalDataSource.activateTask(newTask);

        // Then the task can be retrieved from the persistent repository and is active
        mLocalDataSource.getTask(newTask.getId(), callback);

        verify(callback, never()).onDataNotAvailable();
        verify(callback).onTaskLoaded(newTask);

        assertThat(newTask.isCompleted(), is(false));
    }

    @Test
    public void clearCompletedTask_taskNotRetrievable() {
        // Initialize mocks for the callbacks.
        TasksDataSource.GetTaskCallback callback1 = mock(TasksDataSource.GetTaskCallback.class);
        TasksDataSource.GetTaskCallback callback2 = mock(TasksDataSource.GetTaskCallback.class);
        TasksDataSource.GetTaskCallback callback3 = mock(TasksDataSource.GetTaskCallback.class);

        // Given 2 new completed tasks and 1 active task in the persistent repository
        final Task newTask1 = new Task(TITLE, "");
        mLocalDataSource.saveTask(newTask1);
        mLocalDataSource.completeTask(newTask1);
        final Task newTask2 = new Task(TITLE2, "");
        mLocalDataSource.saveTask(newTask2);
        mLocalDataSource.completeTask(newTask2);
        final Task newTask3 = new Task(TITLE3, "");
        mLocalDataSource.saveTask(newTask3);

        // When completed tasks are cleared in the repository
        mLocalDataSource.clearCompletedTasks();

        // Then the completed tasks cannot be retrieved and the active one can
        mLocalDataSource.getTask(newTask1.getId(), callback1);

        verify(callback1).onDataNotAvailable();
        verify(callback1, never()).onTaskLoaded(newTask1);

        mLocalDataSource.getTask(newTask2.getId(), callback2);

        verify(callback2).onDataNotAvailable();
        verify(callback2, never()).onTaskLoaded(newTask2);

        mLocalDataSource.getTask(newTask3.getId(), callback3);

        verify(callback3, never()).onDataNotAvailable();
        verify(callback3).onTaskLoaded(newTask3);
    }

    @Test
    public void deleteAllTasks_emptyListOfRetrievedTask() {
        // Given a new task in the persistent repository and a mocked callback
        Task newTask = new Task(TITLE, "");
        mLocalDataSource.saveTask(newTask);
        TasksDataSource.LoadTasksCallback callback = mock(TasksDataSource.LoadTasksCallback.class);

        // When all tasks are deleted
        mLocalDataSource.deleteAllTasks();

        // Then the retrieved tasks is an empty list
        mLocalDataSource.getTasks(callback);

        verify(callback).onDataNotAvailable();
        verify(callback, never()).onTasksLoaded(anyList());
    }

    @Test
    public void getTasks_retrieveSavedTasks() {
        // Given 2 new tasks in the persistent repository
        final Task newTask1 = new Task(TITLE, "");
        mLocalDataSource.saveTask(newTask1);
        final Task newTask2 = new Task(TITLE, "");
        mLocalDataSource.saveTask(newTask2);

        // Then the tasks can be retrieved from the persistent repository
        mLocalDataSource.getTasks(new TasksDataSource.LoadTasksCallback() {
            @Override
            public void onTasksLoaded(List<Task> tasks) {
                assertNotNull(tasks);
                assertTrue(tasks.size() >= 2);

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

            @Override
            public void onDataNotAvailable() {
                fail();
            }
        });
    }
}
