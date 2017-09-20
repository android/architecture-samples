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

import android.content.Context;

import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import io.reactivex.Flowable;
import io.reactivex.subscribers.TestSubscriber;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    private TestSubscriber<List<Task>> mTasksTestSubscriber;

    @Mock
    private TasksDataSource mTasksRemoteDataSource;

    @Mock
    private TasksDataSource mTasksLocalDataSource;

    @Mock
    private Context mContext;


    @Before
    public void setupTasksRepository() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);

        // Get a reference to the class under test
        mTasksRepository = TasksRepository.getInstance(
                mTasksRemoteDataSource, mTasksLocalDataSource);

        mTasksTestSubscriber = new TestSubscriber<>();
    }

    @After
    public void destroyRepositoryInstance() {
        TasksRepository.destroyInstance();
    }

    @Test
    public void getTasks_repositoryCachesAfterFirstSubscription_whenTasksAvailableInLocalStorage() {
        // Given that the local data source has data available
        setTasksAvailable(mTasksLocalDataSource, TASKS);
        // And the remote data source does not have any data available
        setTasksNotAvailable(mTasksRemoteDataSource);

        // When two subscriptions are set
        TestSubscriber<List<Task>> testSubscriber1 = new TestSubscriber<>();
        mTasksRepository.getTasks().subscribe(testSubscriber1);

        TestSubscriber<List<Task>> testSubscriber2 = new TestSubscriber<>();
        mTasksRepository.getTasks().subscribe(testSubscriber2);

        // Then tasks were only requested once from remote and local sources
        verify(mTasksRemoteDataSource).getTasks();
        verify(mTasksLocalDataSource).getTasks();
        //
        assertFalse(mTasksRepository.mCacheIsDirty);
        testSubscriber1.assertValue(TASKS);
        testSubscriber2.assertValue(TASKS);
    }

    @Test
    public void getTasks_repositoryCachesAfterFirstSubscription_whenTasksAvailableInRemoteStorage() {
        // Given that the local data source has data available
        setTasksAvailable(mTasksRemoteDataSource, TASKS);
        // And the remote data source does not have any data available
        setTasksNotAvailable(mTasksLocalDataSource);

        // When two subscriptions are set
        TestSubscriber<List<Task>> testSubscriber1 = new TestSubscriber<>();
        mTasksRepository.getTasks().subscribe(testSubscriber1);

        TestSubscriber<List<Task>> testSubscriber2 = new TestSubscriber<>();
        mTasksRepository.getTasks().subscribe(testSubscriber2);

        // Then tasks were only requested once from remote and local sources
        verify(mTasksRemoteDataSource).getTasks();
        verify(mTasksLocalDataSource).getTasks();
        assertFalse(mTasksRepository.mCacheIsDirty);
        testSubscriber1.assertValue(TASKS);
        testSubscriber2.assertValue(TASKS);
    }

    @Test
    public void getTasks_requestsAllTasksFromLocalDataSource() {
        // Given that the local data source has data available
        setTasksAvailable(mTasksLocalDataSource, TASKS);
        // And the remote data source does not have any data available
        setTasksNotAvailable(mTasksRemoteDataSource);

        // When tasks are requested from the tasks repository
        mTasksRepository.getTasks().subscribe(mTasksTestSubscriber);

        // Then tasks are loaded from the local data source
        verify(mTasksLocalDataSource).getTasks();
        mTasksTestSubscriber.assertValue(TASKS);
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
        // Given a stub completed task with title and description in the local repository
        Task task = new Task(TASK_TITLE, "Some Task Description", true);
        Optional<Task> taskOptional = Optional.of(task);
        setTaskAvailable(mTasksLocalDataSource, taskOptional);
        // And the task not available in the remote repository
        setTaskNotAvailable(mTasksRemoteDataSource, taskOptional.get().getId());

        // When a task is requested from the tasks repository
        TestSubscriber<Optional<Task>> testSubscriber = new TestSubscriber<>();
        mTasksRepository.getTask(task.getId()).subscribe(testSubscriber);

        // Then the task is loaded from the database
        verify(mTasksLocalDataSource).getTask(eq(task.getId()));
        testSubscriber.assertValue(taskOptional);
    }

    @Test
    public void getTask_whenDataNotLocal_fails() {
        // Given a stub completed task with title and description in the remote repository
        Task task = new Task(TASK_TITLE, "Some Task Description", true);
        Optional<Task> taskOptional = Optional.of(task);
        setTaskAvailable(mTasksRemoteDataSource, taskOptional);
        // And the task not available in the local repository
        setTaskNotAvailable(mTasksLocalDataSource, task.getId());

        // When a task is requested from the tasks repository
        TestSubscriber<Optional<Task>> testSubscriber = new TestSubscriber<>();
        mTasksRepository.getTask(task.getId()).subscribe(testSubscriber);

        // then empty Optional is returned
        testSubscriber.assertValue(Optional.absent());
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
        // Given that the remote data source has data available
        setTasksAvailable(mTasksRemoteDataSource, TASKS);

        // When calling getTasks in the repository with dirty cache
        mTasksRepository.refreshTasks();
        mTasksRepository.getTasks().subscribe(mTasksTestSubscriber);

        // Verify the tasks from the remote data source are returned, not the local
        verify(mTasksLocalDataSource, never()).getTasks();
        verify(mTasksRemoteDataSource).getTasks();
        mTasksTestSubscriber.assertValue(TASKS);
    }

    @Test
    public void getTasksWithLocalDataSourceUnavailable_tasksAreRetrievedFromRemote() {
        // Given that the local data source has no data available
        setTasksNotAvailable(mTasksLocalDataSource);
        // And the remote data source has data available
        setTasksAvailable(mTasksRemoteDataSource, TASKS);

        // When calling getTasks in the repository
        mTasksRepository.getTasks().subscribe(mTasksTestSubscriber);

        // Verify the tasks from the remote data source are returned
        verify(mTasksRemoteDataSource).getTasks();
        mTasksTestSubscriber.assertValue(TASKS);
    }

    @Test
    public void getTasksWithBothDataSourcesUnavailable_firesOnDataUnavailable() {
        // Given that the local data source has no data available
        setTasksNotAvailable(mTasksLocalDataSource);
        // And the remote data source has no data available
        setTasksNotAvailable(mTasksRemoteDataSource);

        // When calling getTasks in the repository
        mTasksRepository.getTasks().subscribe(mTasksTestSubscriber);

        // Verify no data is returned
        mTasksTestSubscriber.assertNoValues();
        // Verify that error is returned
        mTasksTestSubscriber.assertError(NoSuchElementException.class);
    }

    @Test
    public void getTaskWithBothDataSourcesUnavailable_firesOnError() {
        // Given a task id
        final String taskId = "123";
        // And the local data source has no data available
        setTaskNotAvailable(mTasksLocalDataSource, taskId);
        // And the remote data source has no data available
        setTaskNotAvailable(mTasksRemoteDataSource, taskId);

        // When calling getTask in the repository
        TestSubscriber<Optional<Task>> testSubscriber = new TestSubscriber<>();
        mTasksRepository.getTask(taskId).subscribe(testSubscriber);

        // Verify that error is returned
        testSubscriber.assertValue(Optional.absent());
    }

    @Test
    public void getTasks_refreshesLocalDataSource() {
        // Given that the remote data source has data available
        setTasksAvailable(mTasksRemoteDataSource, TASKS);

        // Mark cache as dirty to force a reload of data from remote data source.
        mTasksRepository.refreshTasks();

        // When calling getTasks in the repository
        mTasksRepository.getTasks().subscribe(mTasksTestSubscriber);

        // Verify that the data fetched from the remote data source was saved in local.
        verify(mTasksLocalDataSource, times(TASKS.size())).saveTask(any(Task.class));
        mTasksTestSubscriber.assertValue(TASKS);
    }

    private void setTasksNotAvailable(TasksDataSource dataSource) {
        when(dataSource.getTasks()).thenReturn(Flowable.just(Collections.emptyList()));
    }

    private void setTasksAvailable(TasksDataSource dataSource, List<Task> tasks) {
        // don't allow the data sources to complete.
        when(dataSource.getTasks()).thenReturn(Flowable.just(tasks).concatWith(Flowable.never()));
    }

    private void setTaskNotAvailable(TasksDataSource dataSource, String taskId) {
        when(dataSource.getTask(eq(taskId))).thenReturn(Flowable.just(Optional.absent()));
    }

    private void setTaskAvailable(TasksDataSource dataSource, Optional<Task> taskOptional) {
        when(dataSource.getTask(eq(taskOptional.get().getId()))).thenReturn(Flowable.just(taskOptional).concatWith(Flowable.never()));
    }
}
