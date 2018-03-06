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
import com.example.android.architecture.blueprints.todoapp.data.source.cache.TasksCache;

import java.util.List;

/**
 * Concrete implementation to load tasks from the data sources into a cache.
 * <p>
 * For simplicity, this implements a dumb synchronisation between locally persisted data and data
 * obtained from the server, by using the remote data source only if the local database doesn't
 * exist or is empty.
 */
public class TasksRepository implements TasksDataSource {

    private static TasksRepository INSTANCE = null;

    private final TasksDataSource mTasksRemoteDataSource;

    private final TasksDataSource mTasksLocalDataSource;

    private final TasksCache mTasksCache;

    // Prevent direct instantiation.
    private TasksRepository(@NonNull TasksDataSource tasksRemoteDataSource,
                            @NonNull TasksDataSource tasksLocalDataSource,
                            @NonNull TasksCache tasksCache) {
        mTasksRemoteDataSource = checkNotNull(tasksRemoteDataSource);
        mTasksLocalDataSource = checkNotNull(tasksLocalDataSource);
        mTasksCache = tasksCache;
    }

    /**
     * Returns the single instance of this class, creating it if necessary.
     *
     * @param tasksRemoteDataSource the backend data source
     * @param tasksLocalDataSource  the device storage data source
     * @return the {@link TasksRepository} instance
     */
    public static TasksRepository getInstance(TasksDataSource tasksRemoteDataSource,
                                              TasksDataSource tasksLocalDataSource,
                                              TasksCache tasksCache) {
        if (INSTANCE == null) {
            INSTANCE = new TasksRepository(tasksRemoteDataSource, tasksLocalDataSource, tasksCache);
        }
        return INSTANCE;
    }

    /**
     * Used to force {@link #getInstance(TasksDataSource, TasksDataSource, TasksCache)} to create a
     * new instance next time it's called.
     */
    public static void destroyInstance() {
        INSTANCE = null;
    }

    /**
     * Gets tasks from cache, local data source (SQLite) or remote data source, whichever is
     * available first.
     * <p>
     * Note: {@link LoadTasksCallback#onDataNotAvailable()} is fired if all data sources fail to
     * get the data.
     */
    @Override
    public void getTasks(@NonNull final LoadTasksCallback callback) {
        checkNotNull(callback);

        // Respond immediately with cache if available and not dirty
        if (!mTasksCache.isDirty()) {
            callback.onTasksLoaded(mTasksCache.getTasks());
            return;
        }

        if (mTasksCache.isDirty()) {
            // If the cache is dirty we need to fetch new data from the network.
            getTasksFromRemoteDataSource(callback);
        } else {
            // Query the local storage if available. If not, query the network.
            mTasksLocalDataSource.getTasks(new LoadTasksCallback() {
                @Override
                public void onTasksLoaded(List<Task> tasks) {
                    refreshCache(tasks);
                    callback.onTasksLoaded(mTasksCache.getTasks());
                }

                @Override
                public void onDataNotAvailable() {
                    getTasksFromRemoteDataSource(callback);
                }
            });
        }
    }

    @Override
    public void saveTask(@NonNull Task task) {
        checkNotNull(task);
        mTasksRemoteDataSource.saveTask(task);
        mTasksLocalDataSource.saveTask(task);

        // Do in memory cache update to keep the app UI up to date
        mTasksCache.saveTask(task);
    }

    @Override
    public void completeTask(@NonNull Task task) {
        checkNotNull(task);
        mTasksRemoteDataSource.completeTask(task);
        mTasksLocalDataSource.completeTask(task);

        Task completedTask = new Task(task.getTitle(), task.getDescription(), task.getId(), true);

        // Do in memory cache update to keep the app UI up to date
        mTasksCache.saveTask(completedTask);
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
        mTasksCache.saveTask(activeTask);
    }

    @Override
    public void activateTask(@NonNull String taskId) {
        checkNotNull(taskId);
        activateTask(getTaskWithId(taskId));
    }

    @Override
    public void clearCompletedTasks() {
        mTasksRemoteDataSource.clearCompletedTasks();
        mTasksLocalDataSource.clearCompletedTasks();

        // Do in memory cache update to keep the app UI up to date
        mTasksCache.clearCompletedTasks();
    }

    /**
     * Gets tasks from local data source (sqlite) unless the table is new or empty. In that case it
     * uses the network data source. This is done to simplify the sample.
     * <p>
     * Note: {@link GetTaskCallback#onDataNotAvailable()} is fired if both data sources fail to
     * get the data.
     */
    @Override
    public void getTask(@NonNull final String taskId, @NonNull final GetTaskCallback callback) {
        checkNotNull(taskId);
        checkNotNull(callback);

        Task cachedTask = getTaskWithId(taskId);

        // Respond immediately with cache if available
        if (cachedTask != null) {
            callback.onTaskLoaded(cachedTask);
            return;
        }

        // Load from server/persisted if needed.

        // Is the task in the local data source? If not, query the network.
        mTasksLocalDataSource.getTask(taskId, new GetTaskCallback() {
            @Override
            public void onTaskLoaded(Task task) {
                // Do in memory cache update to keep the app UI up to date
                mTasksCache.saveTask(task);
                callback.onTaskLoaded(task);
            }

            @Override
            public void onDataNotAvailable() {
                mTasksRemoteDataSource.getTask(taskId, new GetTaskCallback() {
                    @Override
                    public void onTaskLoaded(Task task) {
                        // Do in memory cache update to keep the app UI up to date
                        mTasksCache.saveTask(task);
                        callback.onTaskLoaded(task);
                    }

                    @Override
                    public void onDataNotAvailable() {
                        callback.onDataNotAvailable();
                    }
                });
            }
        });
    }

    @Override
    public void refreshTasks() {
        mTasksCache.invalidate();
    }

    @Override
    public void deleteAllTasks() {
        mTasksRemoteDataSource.deleteAllTasks();
        mTasksLocalDataSource.deleteAllTasks();
        mTasksCache.deleteAllTasks();
    }

    @Override
    public void deleteTask(@NonNull String taskId) {
        mTasksRemoteDataSource.deleteTask(checkNotNull(taskId));
        mTasksLocalDataSource.deleteTask(checkNotNull(taskId));
        mTasksCache.deleteTask(taskId);
    }

    private void getTasksFromRemoteDataSource(@NonNull final LoadTasksCallback callback) {
        mTasksRemoteDataSource.getTasks(new LoadTasksCallback() {
            @Override
            public void onTasksLoaded(List<Task> tasks) {
                refreshCache(tasks);
                refreshLocalDataSource(tasks);
                callback.onTasksLoaded(mTasksCache.getTasks());
            }

            @Override
            public void onDataNotAvailable() {
                callback.onDataNotAvailable();
            }
        });
    }

    private void refreshCache(List<Task> tasks) {
        mTasksCache.refresh(tasks);
    }

    private void refreshLocalDataSource(List<Task> tasks) {
        mTasksLocalDataSource.deleteAllTasks();
        for (Task task : tasks) {
            mTasksLocalDataSource.saveTask(task);
        }
    }

    @Nullable
    private Task getTaskWithId(@NonNull String id) {
        checkNotNull(id);
        return mTasksCache.getTask(id);
    }
}
