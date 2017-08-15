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
import com.example.android.architecture.blueprints.todoapp.util.schedulers.BaseSchedulerProvider;

import io.reactivex.disposables.CompositeDisposable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Listens to user actions from the UI ({@link AddEditTaskFragment}), retrieves the data and updates
 * the UI as required.
 */
public class AddEditTaskPresenter implements AddEditTaskContract.Presenter {

    @NonNull
    private final TasksDataSource mTasksRepository;

    @NonNull
    private final AddEditTaskContract.View mAddTaskView;

    @NonNull
    private final BaseSchedulerProvider mSchedulerProvider;

    @Nullable
    private String mTaskId;

    private boolean mIsDataMissing;

    @NonNull
    private CompositeDisposable mCompositeDisposable;

    /**
     * Creates a presenter for the add/edit view.
     *
     * @param taskId                 ID of the task to edit or null for a new task
     * @param tasksRepository        a repository of data for tasks
     * @param addTaskView            the add/edit view
     * @param shouldLoadDataFromRepo whether data needs to be loaded or not (for config changes)
     */
    public AddEditTaskPresenter(@Nullable String taskId, @NonNull TasksDataSource tasksRepository,
                                @NonNull AddEditTaskContract.View addTaskView, boolean shouldLoadDataFromRepo,
                                @NonNull BaseSchedulerProvider schedulerProvider) {
        mTaskId = taskId;
        mTasksRepository = checkNotNull(tasksRepository);
        mAddTaskView = checkNotNull(addTaskView);
        mIsDataMissing = shouldLoadDataFromRepo;

        mSchedulerProvider = checkNotNull(schedulerProvider, "schedulerProvider cannot be null!");

        mCompositeDisposable = new CompositeDisposable();
        mAddTaskView.setPresenter(this);
    }

    @Override
    public void subscribe() {
        if (!isNewTask() && mIsDataMissing) {
            populateTask();
        }
    }

    @Override
    public void unSubscribe() {
        mCompositeDisposable.clear();
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
        mCompositeDisposable.add(mTasksRepository
                .getTask(mTaskId)
                .subscribeOn(mSchedulerProvider.computation())
                .observeOn(mSchedulerProvider.ui())
                .subscribe(
                        // onNext
                        task -> {
                            if (mAddTaskView.isActive()) {
                                mAddTaskView.setTitle(task.getTitle());
                                mAddTaskView.setDescription(task.getDescription());

                                mIsDataMissing = false;
                            }
                        }, // onError
                        __ -> {
                            if (mAddTaskView.isActive()) {
                                mAddTaskView.showEmptyTaskError();
                            }
                        }));
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
            mAddTaskView.showEmptyTaskError();
        } else {
            mTasksRepository.saveTask(newTask);
            mAddTaskView.showTasksList();
        }
    }

    private void updateTask(String title, String description) {
        if (isNewTask()) {
            throw new RuntimeException("updateTask() was called but task is new.");
        }
        mTasksRepository.saveTask(new Task(title, description, mTaskId));
        mAddTaskView.showTasksList(); // After an edit, go back to the list.
    }
}
