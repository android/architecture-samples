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

import static com.google.common.base.Preconditions.checkNotNull;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.example.android.architecture.blueprints.todoapp.data.Task;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import rx.Observable;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Concrete implementation to load tasks from the data sources into a cache.
 * <p>
 * For simplicity, this implements a dumb synchronisation between locally persisted data and data
 * obtained from the server, by using the remote data source only if the local database doesn't
 * exist or is empty.
 */
public class TasksRepository implements TasksDataSource {

    @Nullable
    private static TasksRepository INSTANCE = null;

    @NonNull
    private final TasksDataSource mTasksRemoteDataSource;

    @NonNull
    private final TasksDataSource mTasksLocalDataSource;

    /**
     * This variable has package local visibility so it can be accessed from tests.
     */
    @NonNull
    Map<String, Task> mCachedTasks;

    /**
     * Marks the cache as invalid, to force an update the next time data is requested. This variable
     * has package local visibility so it can be accessed from tests.
     */
    boolean mCacheIsDirty = false;

    // Prevent direct instantiation.
    private TasksRepository(@NonNull TasksDataSource tasksRemoteDataSource,
                            @NonNull TasksDataSource tasksLocalDataSource) {
        mTasksRemoteDataSource = checkNotNull(tasksRemoteDataSource);
        mTasksLocalDataSource = checkNotNull(tasksLocalDataSource);
    }

    /**
     * Returns the single instance of this class, creating it if necessary.
     *
     * @param tasksRemoteDataSource the backend data source
     * @param tasksLocalDataSource  the device storage data source
     * @return the {@link TasksRepository} instance
     */
    public static TasksRepository getInstance(@NonNull TasksDataSource tasksRemoteDataSource,
                                              @NonNull TasksDataSource tasksLocalDataSource) {
        if (INSTANCE == null) {
            INSTANCE = new TasksRepository(tasksRemoteDataSource, tasksLocalDataSource);
        }
        return INSTANCE;
    }

    /**
     * Used to force {@link #getInstance(TasksDataSource, TasksDataSource)} to create a new instance
     * next time it's called.
     */
    public static void destroyInstance() {
        INSTANCE = null;
    }

    /**
     * Gets tasks from cache, local data source (SQLite) or remote data source, whichever is
     * available first.
     */
    @Override
    public Observable<List<Task>> getTasks() {
        // Respond immediately with cache if available and dirty
        if (mCachedTasks != null && !mCacheIsDirty) {
            return Observable.from(mCachedTasks.values()).toList();
        } else if (mCachedTasks == null) {
            mCachedTasks = new LinkedHashMap<String, Task>();
        }

        Observable<List<Task>> remoteTasks = getAndSaveRemoteTasks();

        if (mCacheIsDirty) {
            return remoteTasks;
        } else {
            // Query the local storage if available. If not, query the network.
            Observable<List<Task>> localTasks = getAndCacheLocalTasks();
            return Observable.concat(localTasks, remoteTasks)
                    .filter(new Func1<List<Task>, Boolean>() {
                        @Override
                        public Boolean call(List<Task> tasks) {
                            return !tasks.isEmpty();
                        }
                    }).first();
        }
    }



    private Observable<List<Task>> getAndCacheLocalTasks() {
        return mTasksLocalDataSource.getTasks()
                .flatMap(new Func1<List<Task>, Observable<List<Task>>>() {
                    @Override
                    public Observable<List<Task>> call(List<Task> tasks) {
                        return Observable.from(tasks)
                                .doOnNext(new Action1<Task>() {
                                    @Override
                                    public void call(Task task) {
                                        mCachedTasks.put(task.getId(), task);
                                    }
                                })
                                .toList();
                    }
                });
    }

    private Observable<List<Task>> getAndSaveRemoteTasks() {
        return mTasksRemoteDataSource.getTasks()
                .flatMap(new Func1<List<Task>, Observable<List<Task>>>() {
                    @Override
                    public Observable<List<Task>> call(List<Task> tasks) {
                        return Observable.from(tasks)
                                .doOnNext(new Action1<Task>() {
                                    @Override
                                    public void call(Task task) {
                                        mTasksLocalDataSource.saveTask(task);
                                        mCachedTasks.put(task.getId(), task);
                                    }
                                }).toList();
                    }
                })
                .doOnCompleted(new Action0() {
                    @Override
                    public void call() {
                        mCacheIsDirty = false;
                    }
                });
    }

    @Override
    public void saveTask(@NonNull Task task) {
        checkNotNull(task);
        mTasksRemoteDataSource.saveTask(task);
        mTasksLocalDataSource.saveTask(task);

        // Do in memory cache update to keep the app UI up to date
        if (mCachedTasks == null) {
            mCachedTasks = new LinkedHashMap<>();
        }
        mCachedTasks.put(task.getId(), task);
    }

    @Override
    public void completeTask(@NonNull Task task) {
        checkNotNull(task);
        mTasksRemoteDataSource.completeTask(task);
        mTasksLocalDataSource.completeTask(task);

        Task completedTask = new Task(task.getTitle(), task.getDescription(), task.getId(), true);

        // Do in memory cache update to keep the app UI up to date
        if (mCachedTasks == null) {
            mCachedTasks = new LinkedHashMap<>();
        }
        mCachedTasks.put(task.getId(), completedTask);
    }

    @Override
    public void completeTask(@NonNull String taskId) {
        checkNotNull(taskId);
        completeTask(getTaskWithId(taskId));
    }

    @Override
    public void activateTask(@NonNull Task task) {
        checkNotNull(task);
        mTasksRemoteDataSource.activateTask(task);
        mTasksLocalDataSource.activateTask(task);

        Task activeTask = new Task(task.getTitle(), task.getDescription(), task.getId());

        // Do in memory cache update to keep the app UI up to date
        if (mCachedTasks == null) {
            mCachedTasks = new LinkedHashMap<>();
        }
        mCachedTasks.put(task.getId(), activeTask);
    }

    @Override
    public void activateTask(@NonNull String taskId) {
        checkNotNull(taskId);
        Task taskWithId = getTaskWithId(taskId);
        if (taskWithId != null) {
            activateTask(taskWithId);
        }
    }

    @Override
    public void clearCompletedTasks() {
        mTasksRemoteDataSource.clearCompletedTasks();
        mTasksLocalDataSource.clearCompletedTasks();

        // Do in memory cache update to keep the app UI up to date
        if (mCachedTasks == null) {
            mCachedTasks = new LinkedHashMap<>();
        }
        Iterator<Map.Entry<String, Task>> it = mCachedTasks.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Task> entry = it.next();
            if (entry.getValue().isCompleted()) {
                it.remove();
            }
        }
    }

    /**
     * Gets tasks from local data source (sqlite) unless the table is new or empty. In that case it
     * uses the network data source. This is done to simplify the sample.
     */
    @Override
    public Observable<Task> getTask(@NonNull final String taskId) {
        checkNotNull(taskId);

        final Task cachedTask = getTaskWithId(taskId);

        // Respond immediately with cache if available.
        if (cachedTask != null) {
            mCachedTasks = new LinkedHashMap<String, Task>();
        }

        // Is the task in the local data source? If not, query the network.
        Observable<Task> localTask = getTaskWithIdFromLocalRepository(taskId);
        Observable<Task> remoteTask = mTasksRemoteDataSource.getTask(taskId)
                .doOnNext(new Action1<Task>() {
                    @Override
                    public void call(Task task) {
                        mTasksLocalDataSource.saveTask(task);
                        mCachedTasks.put(task.getId(), task);
                    }
                });
        return Observable.concat(localTask, remoteTask).first()
                .map(new Func1<Task, Task>() {
                    @Override
                    public Task call(Task task) {
                        if (task == null) {
                            throw  new NoSuchElementException("No task found with taskId " + taskId);
                        }
                        return task;
                    }
                });
    }


    @Override
    public void refreshTasks() {
        mCacheIsDirty = true;
    }

    @Override
    public void deleteAllTasks() {
        mTasksRemoteDataSource.deleteAllTasks();
        mTasksLocalDataSource.deleteAllTasks();

        if (mCachedTasks == null) {
            mCachedTasks = new LinkedHashMap<>();
        }
        mCachedTasks.clear();
    }

    @Override
    public void deleteTask(@NonNull String taskId) {
        mTasksRemoteDataSource.deleteTask(checkNotNull(taskId));
        mTasksLocalDataSource.deleteTask(checkNotNull(taskId));

        mCachedTasks.remove(taskId);
    }

    @Nullable
    private Task getTaskWithId(@NonNull String id) {
        checkNotNull(id);
        if (mCachedTasks == null || mCachedTasks.isEmpty()) {
            return null;
        } else {
            return mCachedTasks.get(id);
        }
    }

    @NonNull
    Observable<Task> getTaskWithIdFromLocalRepository(@NonNull final String taskId) {
        return mTasksLocalDataSource.getTask(taskId)
                .doOnNext(new Action1<Task>() {
                    @Override
                    public void call(Task task) {
                        mCachedTasks.put(taskId, task);
                    }
                })
                .first();
    }

}
