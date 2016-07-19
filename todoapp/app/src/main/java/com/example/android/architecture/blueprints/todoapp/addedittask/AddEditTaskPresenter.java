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

import com.example.android.architecture.blueprints.todoapp.UseCase;
import com.example.android.architecture.blueprints.todoapp.UseCaseHandler;
import com.example.android.architecture.blueprints.todoapp.addedittask.domain.usecase.GetTask;
import com.example.android.architecture.blueprints.todoapp.addedittask.domain.usecase.SaveTask;
import com.example.android.architecture.blueprints.todoapp.tasks.domain.model.Task;

import javax.inject.Inject;

/**
 * Listens to user actions from the UI ({@link AddEditTaskFragment}), retrieves the data and
 * updates
 * the UI as required.
 * <p />
 * By marking the constructor with {@code @Inject}, Dagger injects the dependencies required to
 * create an instance of the AddEditTaskPresenter (if it fails, it emits a compiler error). It uses
 * {@link AddEditTaskPresenterModule} to do so.
 * <p/>
 * Dagger generated code doesn't require public access to the constructor or class, and
 * therefore, to ensure the developer doesn't instantiate the class manually bypassing Dagger,
 * it's good practice minimise the visibility of the class/constructor as much as possible.
 */
public class AddEditTaskPresenter implements AddEditTaskContract.Presenter {

    @NonNull
    private final AddEditTaskContract.View mAddTaskView;

    private final GetTask mGetTask;

    private final SaveTask mSaveTask;

    private final UseCaseHandler mUseCaseHandler;

    @Nullable
    private String mTaskId;

    /**
     * Dagger strictly enforces that arguments not marked with {@code @Nullable} are not injected
     * with {@code @Nullable} values.
     */
    @Inject
    AddEditTaskPresenter(UseCaseHandler useCaseHandler, @Nullable String taskId,
                         AddEditTaskContract.View addTaskView,
                         GetTask getTask, SaveTask saveTask) {
        mTaskId = taskId;
        mAddTaskView = addTaskView;
        mUseCaseHandler = useCaseHandler;
        mGetTask = getTask;
        mSaveTask = saveTask;
    }

    /**
     * Method injection is used here to safely reference {@code this} after the object is created.
     * For more information, see Java Concurrency in Practice.
     */
    @Inject
    void setupListeners() {
        mAddTaskView.setPresenter(this);
    }

    @Override
    public void start() {
        if (!isNewTask()) {
            populateTask();
        }
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

        mUseCaseHandler.execute(mGetTask, new GetTask.RequestValues(mTaskId),
                new UseCase.UseCaseCallback<GetTask.ResponseValue>() {
                    @Override
                    public void onSuccess(GetTask.ResponseValue response) {
                        showTask(response.getTask());
                    }

                    @Override
                    public void onError() {
                        showEmptyTaskError();
                    }
                });
    }

    private void showTask(Task task) {
        // The view may not be able to handle UI updates anymore
        if (mAddTaskView.isActive()) {
            mAddTaskView.setTitle(task.getTitle());
            mAddTaskView.setDescription(task.getDescription());
        }
    }

    private void showSaveError() {
        // Show error, log, etc.
    }

    private void showEmptyTaskError() {
        // The view may not be able to handle UI updates anymore
        if (mAddTaskView.isActive()) {
            mAddTaskView.showEmptyTaskError();
        }
    }

    private boolean isNewTask() {
        return mTaskId == null;
    }

    private void createTask(String title, String description) {
        Task newTask = new Task(title, description);
        if (newTask.isEmpty()) {
            mAddTaskView.showEmptyTaskError();
        } else {
            mUseCaseHandler.execute(mSaveTask, new SaveTask.RequestValues(newTask),
                    new UseCase.UseCaseCallback<SaveTask.ResponseValue>() {
                        @Override
                        public void onSuccess(SaveTask.ResponseValue response) {
                            mAddTaskView.showTasksList();
                        }

                        @Override
                        public void onError() {
                            showSaveError();
                        }
                    });
        }
    }

    private void updateTask(String title, String description) {
        if (isNewTask()) {
            throw new RuntimeException("updateTask() was called but task is new.");
        }
        Task newTask = new Task(title, description, mTaskId);
        mUseCaseHandler.execute(mSaveTask, new SaveTask.RequestValues(newTask),
                new UseCase.UseCaseCallback<SaveTask.ResponseValue>() {
                    @Override
                    public void onSuccess(SaveTask.ResponseValue response) {
                        // After an edit, go back to the list.
                        mAddTaskView.showTasksList();
                    }

                    @Override
                    public void onError() {
                        showSaveError();
                    }
                });
    }
}
