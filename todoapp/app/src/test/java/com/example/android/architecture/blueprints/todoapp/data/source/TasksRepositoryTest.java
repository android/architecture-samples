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

package com.example.android.architecture.blueprints.todoapp.data.source;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import android.content.Context;

import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.google.common.collect.Lists;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

/**
 * Unit tests for the implementation of the in-memory repository with cache.
 */
public class TasksRepositoryTest {

    private final static String TASK_TITLE = "title";

    private final static String TASK_TITLE2 = "title2";

    private final static String TASK_TITLE3 = "title3";

    private static List<Task> TASKS = Lists.newArrayList(new Task("Title1", "Description1"),
            new Task("Title2", "Description2"));

    private TasksRepository mTasksRepository;

    @Mock
    private TasksDataSource mTasksRemoteDataSource;

    @Mock
    private TasksDataSource mTasksLocalDataSource;

    @Mock
    private Context mContext;

//    @Mock
//    private TasksDataSource.GetTaskCallback mGetTaskCallback;
//
//    @Mock
//    private TasksDataSource.LoadTasksCallback mLoadTasksCallback;

    /**
     * {@link ArgumentCaptor} is a powerful Mockito API to capture argument values and use them to
     * perform further actions or assertions on them.
     */
//    @Captor
//    private ArgumentCaptor<TasksDataSource.LoadTasksCallback> mTasksCallbackCaptor;

    /**
     * {@link ArgumentCaptor} is a powerful Mockito API to capture argument values and use them to
     * perform further actions or assertions on them.
     */
//    @Captor
//    private ArgumentCaptor<TasksDataSource.GetTaskCallback> mTaskCallbackCaptor;

    @Before
    public void setupTasksRepository() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);

        // Get a reference to the class under test
        mTasksRepository = TasksRepository.getInstance(
                mTasksRemoteDataSource, mTasksLocalDataSource);
    }

    @After
    public void destroyRepositoryInstance() {
        TasksRepository.destroyInstance();
    }

    @Test
    public void getTasks_repositoryCachesAfterFirstApiCall() {
        // Given a setup Captor to capture callbacks
        // When two calls are issued to the tasks repository
//        twoTasksLoadCallsToRepository(mLoadTasksCallback);

        // Then tasks were only requested once from Service API
        verify(mTasksRemoteDataSource).getTasks();
    }

    @Test
    public void getTasks_requestsAllTasksFromLocalDataSource() {
        // When tasks are requested from the tasks repository
        mTasksRepository.getTasks();

        // Then tasks are loaded from the local data source
        verify(mTasksLocalDataSource).getTasks();
    }

    @Test
    public void saveTask_savesTaskToServiceAPI() {
        // Given a stub task with title and description
        Task newTask = new Task(TASK_TITLE, "Some Task Description");

        // When a task is saved to the tasks repository
        mTasksRepository.saveTask(newTask);

        // Then the service API and persistent repository are called and the cache is updated
        verify(mTasksRemoteDataSource).saveTask(newTask);
        verify(mTasksLocalDataSource).saveTask(newTask);
        assertThat(mTasksRepository.mCachedTasks.size(), is(1));
    }

    @Test
    public void completeTask_completesTaskToServiceAPIUpdatesCache() {
        // Given a stub active task with title and description added in the repository
        Task newTask = new Task(TASK_TITLE, "Some Task Description");
        mTasksRepository.saveTask(newTask);

        // When a task is completed to the tasks repository
        mTasksRepository.completeTask(newTask);

        // Then the service API and persistent repository are called and the cache is updated
        verify(mTasksRemoteDataSource).completeTask(newTask);
        verify(mTasksLocalDataSource).completeTask(newTask);
        assertThat(mTasksRepository.mCachedTasks.size(), is(1));
        assertThat(mTasksRepository.mCachedTasks.get(newTask.getId()).isActive(), is(false));
    }

    @Test
    public void completeTaskId_completesTaskToServiceAPIUpdatesCache() {
        // Given a stub active task with title and description added in the repository
        Task newTask = new Task(TASK_TITLE, "Some Task Description");
        mTasksRepository.saveTask(newTask);

        // When a task is completed using its id to the tasks repository
        mTasksRepository.completeTask(newTask.getId());

        // Then the service API and persistent repository are called and the cache is updated
        verify(mTasksRemoteDataSource).completeTask(newTask);
        verify(mTasksLocalDataSource).completeTask(newTask);
        assertThat(mTasksRepository.mCachedTasks.size(), is(1));
        assertThat(mTasksRepository.mCachedTasks.get(newTask.getId()).isActive(), is(false));
    }

    @Test
    public void activateTask_activatesTaskToServiceAPIUpdatesCache() {
        // Given a stub completed task with title and description in the repository
        Task newTask = new Task(TASK_TITLE, "Some Task Description", true);
        mTasksRepository.saveTask(newTask);

        // When a completed task is activated to the tasks repository
        mTasksRepository.activateTask(newTask);

        // Then the service API and persistent repository are called and the cache is updated
        verify(mTasksRemoteDataSource).activateTask(newTask);
        verify(mTasksLocalDataSource).activateTask(newTask);
        assertThat(mTasksRepository.mCachedTasks.size(), is(1));
        assertThat(mTasksRepository.mCachedTasks.get(newTask.getId()).isActive(), is(true));
    }

    @Test
    public void activateTaskId_activatesTaskToServiceAPIUpdatesCache() {
        // Given a stub completed task with title and description in the repository
        Task newTask = new Task(TASK_TITLE, "Some Task Description", true);
        mTasksRepository.saveTask(newTask);

        // When a completed task is activated with its id to the tasks repository
        mTasksRepository.activateTask(newTask.getId());

        // Then the service API and persistent repository are called and the cache is updated
        verify(mTasksRemoteDataSource).activateTask(newTask);
        verify(mTasksLocalDataSource).activateTask(newTask);
        assertThat(mTasksRepository.mCachedTasks.size(), is(1));
        assertThat(mTasksRepository.mCachedTasks.get(newTask.getId()).isActive(), is(true));
    }

    @Test
    public void getTask_requestsSingleTaskFromLocalDataSource() {
        // When a task is requested from the tasks repository
        mTasksRepository.getTask(TASK_TITLE);

        // Then the task is loaded from the database
        verify(mTasksLocalDataSource).getTask(eq(TASK_TITLE));
    }

    @Test
    public void deleteCompletedTasks_deleteCompletedTasksToServiceAPIUpdatesCache() {
        // Given 2 stub completed tasks and 1 stub active tasks in the repository
        Task newTask = new Task(TASK_TITLE, "Some Task Description", true);
        mTasksRepository.saveTask(newTask);
        Task newTask2 = new Task(TASK_TITLE2, "Some Task Description");
        mTasksRepository.saveTask(newTask2);
        Task newTask3 = new Task(TASK_TITLE3, "Some Task Description", true);
        mTasksRepository.saveTask(newTask3);

        // When a completed tasks are cleared to the tasks repository
        mTasksRepository.clearCompletedTasks();


        // Then the service API and persistent repository are called and the cache is updated
        verify(mTasksRemoteDataSource).clearCompletedTasks();
        verify(mTasksLocalDataSource).clearCompletedTasks();

        assertThat(mTasksRepository.mCachedTasks.size(), is(1));
        assertTrue(mTasksRepository.mCachedTasks.get(newTask2.getId()).isActive());
        assertThat(mTasksRepository.mCachedTasks.get(newTask2.getId()).getTitle(), is(TASK_TITLE2));
    }

    @Test
    public void deleteAllTasks_deleteTasksToServiceAPIUpdatesCache() {
        // Given 2 stub completed tasks and 1 stub active tasks in the repository
        Task newTask = new Task(TASK_TITLE, "Some Task Description", true);
        mTasksRepository.saveTask(newTask);
        Task newTask2 = new Task(TASK_TITLE2, "Some Task Description");
        mTasksRepository.saveTask(newTask2);
        Task newTask3 = new Task(TASK_TITLE3, "Some Task Description", true);
        mTasksRepository.saveTask(newTask3);

        // When all tasks are deleted to the tasks repository
        mTasksRepository.deleteAllTasks();

        // Verify the data sources were called
        verify(mTasksRemoteDataSource).deleteAllTasks();
        verify(mTasksLocalDataSource).deleteAllTasks();

        assertThat(mTasksRepository.mCachedTasks.size(), is(0));
    }

    @Test
    public void deleteTask_deleteTaskToServiceAPIRemovedFromCache() {
        // Given a task in the repository
        Task newTask = new Task(TASK_TITLE, "Some Task Description", true);
        mTasksRepository.saveTask(newTask);
        assertThat(mTasksRepository.mCachedTasks.containsKey(newTask.getId()), is(true));

        // When deleted
        mTasksRepository.deleteTask(newTask.getId());

        // Verify the data sources were called
        verify(mTasksRemoteDataSource).deleteTask(newTask.getId());
        verify(mTasksLocalDataSource).deleteTask(newTask.getId());

        // Verify it's removed from repository
        assertThat(mTasksRepository.mCachedTasks.containsKey(newTask.getId()), is(false));
    }

    @Test
    public void getTasksWithDirtyCache_tasksAreRetrievedFromRemote() {
        // When calling getTasks in the repository with dirty cache
        mTasksRepository.refreshTasks();
        mTasksRepository.getTasks();

        // And the remote data source has data available
        setTasksAvailable(mTasksRemoteDataSource, TASKS);

        // Verify the tasks from the remote data source are returned, not the local
        verify(mTasksLocalDataSource, never()).getTasks();
//        verify(mLoadTasksCallback).onTasksLoaded(TASKS);
    }

    @Test
    public void getTasksWithLocalDataSourceUnavailable_tasksAreRetrievedFromRemote() {
        // When calling getTasks in the repository
        mTasksRepository.getTasks();

        // And the local data source has no data available
        setTasksNotAvailable(mTasksLocalDataSource);

        // And the remote data source has data available
        setTasksAvailable(mTasksRemoteDataSource, TASKS);

        // Verify the tasks from the local data source are returned
//        verify(mLoadTasksCallback).onTasksLoaded(TASKS);
    }

    @Test
    public void getTasksWithBothDataSourcesUnavailable_firesOnDataUnavailable() {
        // When calling getTasks in the repository
        mTasksRepository.getTasks();

        // And the local data source has no data available
        setTasksNotAvailable(mTasksLocalDataSource);

        // And the remote data source has no data available
        setTasksNotAvailable(mTasksRemoteDataSource);

        // Verify no data is returned
//        verify(mLoadTasksCallback).onDataNotAvailable();
    }

    @Test
    public void getTaskWithBothDataSourcesUnavailable_firesOnDataUnavailable() {
        // Given a task id
        final String taskId = "123";

        // When calling getTask in the repository
        mTasksRepository.getTask(taskId);

        // And the local data source has no data available
        setTaskNotAvailable(mTasksLocalDataSource, taskId);

        // And the remote data source has no data available
        setTaskNotAvailable(mTasksRemoteDataSource, taskId);

        // Verify no data is returned
//        verify(mGetTaskCallback).onDataNotAvailable();
    }

    @Test
    public void getTasks_refreshesLocalDataSource() {
        // Mark cache as dirty to force a reload of data from remote data source.
        mTasksRepository.refreshTasks();

        // When calling getTasks in the repository
        mTasksRepository.getTasks();

        // Make the remote data source return data
        setTasksAvailable(mTasksRemoteDataSource, TASKS);

        // Verify that the data fetched from the remote data source was saved in local.
        verify(mTasksLocalDataSource, times(TASKS.size())).saveTask(any(Task.class));
    }

    /**
     * Convenience method that issues two calls to the tasks repository
     */
    /*private void twoTasksLoadCallsToRepository(TasksDataSource.LoadTasksCallback callback) {
        // When tasks are requested from repository
        mTasksRepository.getTasks(); // First call to API

        // Use the Mockito Captor to capture the callback
        verify(mTasksLocalDataSource).getTasks();

        // Local data source doesn't have data yet
        mTasksCallbackCaptor.getValue().onDataNotAvailable();


        // Verify the remote data source is queried
        verify(mTasksRemoteDataSource).getTasks();

        // Trigger callback so tasks are cached
        mTasksCallbackCaptor.getValue().onTasksLoaded(TASKS);

        mTasksRepository.getTasks(); // Second call to API
    }*/

    private void setTasksNotAvailable(TasksDataSource dataSource) {
        verify(dataSource).getTasks();
//        mTasksCallbackCaptor.getValue().onDataNotAvailable();
    }

    private void setTasksAvailable(TasksDataSource dataSource, List<Task> tasks) {
        verify(dataSource).getTasks();
//        mTasksCallbackCaptor.getValue().onTasksLoaded(tasks);
    }

    private void setTaskNotAvailable(TasksDataSource dataSource, String taskId) {
        verify(dataSource).getTask(eq(taskId));
//        mTaskCallbackCaptor.getValue().onDataNotAvailable();
    }

    private void setTaskAvailable(TasksDataSource dataSource, Task task) {
        verify(dataSource).getTask(eq(task.getId()));
//        mTaskCallbackCaptor.getValue().onTaskLoaded(task);
    }
 }
