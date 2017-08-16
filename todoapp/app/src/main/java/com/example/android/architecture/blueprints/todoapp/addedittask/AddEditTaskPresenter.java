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

package com.example.android.architecture.blueprints.todoapp.addedittask;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;

import javax.inject.Inject;

import dagger.Lazy;

/**
 * Listens to user actions from the UI ({@link AddEditTaskFragment}), retrieves the data and
 * updates
 * the UI as required.
 * <p/>
 * By marking the constructor with {@code @Inject}, Dagger injects the dependencies required to
 * create an instance of the AddEditTaskPresenter (if it fails, it emits a compiler error). It uses
 * {@link AddEditTaskModule} to do so.
 * <p/>
 * Dagger generated code doesn't require public access to the constructor or class, and
 * therefore, to ensure the developer doesn't instantiate the class manually bypassing Dagger,
 * it's good practice minimise the visibility of the class/constructor as much as possible.
 */
final class AddEditTaskPresenter implements AddEditTaskContract.Presenter,
        TasksDataSource.GetTaskCallback {

    @NonNull
    private final TasksDataSource mTasksRepository;

    @Nullable
    private AddEditTaskContract.View mAddTaskView;

    @Nullable
    private String mTaskId;

    // This is provided lazily because its value is determined in the Activity's onCreate. By
    // calling it in takeView(), the value is guaranteed to be set.
    private Lazy<Boolean> mIsDataMissingLazy;

    // Whether the data has been loaded with this presenter (or comes from a system restore)
    private boolean mIsDataMissing;

    /**
     * Dagger strictly enforces that arguments not marked with {@code @Nullable} are not injected
     * with {@code @Nullable} values.
     *
     * @param taskId the task ID or null if it's a new task
     * @param tasksRepository the data source
     * @param shouldLoadDataFromRepo a flag that controls whether we should load data from the
     *                               repository or not. It's lazy because it's determined in the
     *                               Activity's onCreate.
     */
    @Inject
    AddEditTaskPresenter(@Nullable String taskId, @NonNull TasksRepository tasksRepository,
                         Lazy<Boolean> shouldLoadDataFromRepo) {
        mTaskId = taskId;
        mTasksRepository = tasksRepository;
        mIsDataMissingLazy = shouldLoadDataFromRepo;
    }

    @Override
    public void saveTask(String title, String description) {
        if (isNewTask()) {
            createTask(title, description);
        } else {
            updateTask(title, description);
        }
    }

    @Override
    public void populateTask() {
        if (isNewTask()) {
            throw new RuntimeException("populateTask() was called but task is new.");
        }
        mTasksRepository.getTask(mTaskId, this);
    }

    @Override
    public void takeView(AddEditTaskContract.View view) {
        mAddTaskView = view;
        mIsDataMissing = mIsDataMissingLazy.get();
        if (!isNewTask() && mIsDataMissing) {
            populateTask();
        }
    }

    @Override
    public void dropView() {
        mAddTaskView = null;
    }

    @Override
    public void onTaskLoaded(Task task) {
        // The view may not be able to handle UI updates anymore
        if (mAddTaskView != null && mAddTaskView.isActive()) {
            mAddTaskView.setTitle(task.getTitle());
            mAddTaskView.setDescription(task.getDescription());
        }
        mIsDataMissing = false;
    }

    @Override
    public void onDataNotAvailable() {
        // The view may not be able to handle UI updates anymore
        if (mAddTaskView != null && mAddTaskView.isActive()) {
            mAddTaskView.showEmptyTaskError();
        }
    }

    @Override
    public boolean isDataMissing() {
        return mIsDataMissing;
    }

    private boolean isNewTask() {
        return mTaskId == null;
    }

    private void createTask(String title, String description) {
        Task newTask = new Task(title, description);
        if (newTask.isEmpty()) {
            if (mAddTaskView != null) {
                mAddTaskView.showEmptyTaskError();
            }
        } else {
            mTasksRepository.saveTask(newTask);
            if (mAddTaskView != null) {
                mAddTaskView.showTasksList();
            }
        }
    }

    private void updateTask(String title, String description) {
        if (isNewTask()) {
            throw new RuntimeException("updateTask() was called but task is new.");
        }
        mTasksRepository.saveTask(new Task(title, description, mTaskId));
        if (mAddTaskView != null) {
            mAddTaskView.showTasksList(); // After an edit, go back to the list.
        }
    }
}
