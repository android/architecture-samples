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

package com.example.android.architecture.blueprints.todoapp.tasks;

import android.app.Activity;
import android.support.annotation.NonNull;

import com.example.android.architecture.blueprints.todoapp.UseCase;
import com.example.android.architecture.blueprints.todoapp.UseCaseHandler;
import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskActivity;
import com.example.android.architecture.blueprints.todoapp.tasks.domain.model.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource;
import com.example.android.architecture.blueprints.todoapp.tasks.domain.usecase.ActivateTask;
import com.example.android.architecture.blueprints.todoapp.tasks.domain.usecase.ClearCompleteTasks;
import com.example.android.architecture.blueprints.todoapp.tasks.domain.usecase.CompleteTask;
import com.example.android.architecture.blueprints.todoapp.tasks.domain.usecase.GetTasks;

import java.util.List;

import javax.inject.Inject;

import static com.google.common.base.Preconditions.checkNotNull;


/**
 * Listens to user actions from the UI ({@link TasksFragment}), retrieves the data and updates the
 * UI as required.
 * <p />
 * By marking the constructor with {@code @Inject}, Dagger injects the dependencies required to
 * create an instance of the TasksPresenter (if it fails, it emits a compiler error).  It uses
 * {@link TasksPresenterModule} to do so.
 * <p/>
 * Dagger generated code doesn't require public access to the constructor or class, and
 * therefore, to ensure the developer doesn't instantiate the class manually and bypasses Dagger,
 * it's good practice minimise the visibility of the class/constructor as much as possible.
 **/
final class TasksPresenter implements TasksContract.Presenter {

    private final TasksContract.View mTasksView;

    private final UseCaseHandler mUseCaseHandler;
    private final GetTasks mGetTasks;
    private final CompleteTask mCompleteTask;
    private final ActivateTask mActivateTask;
    private final ClearCompleteTasks mClearCompleteTasks;

    private TasksFilterType mCurrentFiltering = TasksFilterType.ALL_TASKS;

    private boolean mFirstLoad = true;

    /**
     * Dagger strictly enforces that arguments not marked with {@code @Nullable} are not injected
     * with {@code @Nullable} values.
     */
    @Inject
    TasksPresenter(UseCaseHandler handler, TasksContract.View tasksView,
                   GetTasks getTasks, CompleteTask completeTask,
                   ActivateTask activateTask, ClearCompleteTasks clearCompleteTasks) {
        mTasksView = tasksView;
        mUseCaseHandler = handler;
        mGetTasks = getTasks;
        mCompleteTask = completeTask;
        mActivateTask = activateTask;
        mClearCompleteTasks = clearCompleteTasks;
    }

    /**
     * Method injection is used here to safely reference {@code this} after the object is created.
     * For more information, see Java Concurrency in Practice.
     */
    @Inject
    void setupListeners() {
        mTasksView.setPresenter(this);
    }

    @Override
    public void start() {
        loadTasks(false);
    }

    @Override
    public void result(int requestCode, int resultCode) {
        // If a task was successfully added, show snackbar
        if (AddEditTaskActivity.REQUEST_ADD_TASK == requestCode
                && Activity.RESULT_OK == resultCode) {
            mTasksView.showSuccessfullySavedMessage();
        }
    }

    @Override
    public void loadTasks(boolean forceUpdate) {
        // Simplification for sample: a network reload will be forced on first load.
        loadTasks(forceUpdate || mFirstLoad, true);
        mFirstLoad = false;
    }

    /**
     * @param forceUpdate   Pass in true to refresh the data in the {@link TasksDataSource}
     * @param showLoadingUI Pass in true to display a loading icon in the UI
     */
    private void loadTasks(boolean forceUpdate, final boolean showLoadingUI) {
        if (showLoadingUI) {
            mTasksView.setLoadingIndicator(true);
        }

        GetTasks.RequestValues requestValue = new GetTasks.RequestValues(forceUpdate,
                mCurrentFiltering);

        mUseCaseHandler.execute(mGetTasks, requestValue,
                new UseCase.UseCaseCallback<GetTasks.ResponseValue>() {
                    @Override
                    public void onSuccess(GetTasks.ResponseValue response) {
                        List<Task> tasks = response.getTasks();
                        // The view may not be able to handle UI updates anymore
                        if (!mTasksView.isActive()) {
                            return;
                        }
                        if (showLoadingUI) {
                            mTasksView.setLoadingIndicator(false);
                        }

                        processTasks(tasks);
                    }

                    @Override
                    public void onError() {
                        // The view may not be able to handle UI updates anymore
                        if (!mTasksView.isActive()) {
                            return;
                        }
                        mTasksView.showLoadingTasksError();
                    }
                });
    }

    private void processTasks(List<Task> tasks) {
        if (tasks.isEmpty()) {
            // Show a message indicating there are no tasks for that filter type.
            processEmptyTasks();
        } else {
            // Show the list of tasks
            mTasksView.showTasks(tasks);
            // Set the filter label's text.
            showFilterLabel();
        }
    }

    private void showFilterLabel() {
        switch (mCurrentFiltering) {
            case ACTIVE_TASKS:
                mTasksView.showActiveFilterLabel();
                break;
            case COMPLETED_TASKS:
                mTasksView.showCompletedFilterLabel();
                break;
            default:
                mTasksView.showAllFilterLabel();
                break;
        }
    }

    private void processEmptyTasks() {
        switch (mCurrentFiltering) {
            case ACTIVE_TASKS:
                mTasksView.showNoActiveTasks();
                break;
            case COMPLETED_TASKS:
                mTasksView.showNoCompletedTasks();
                break;
            default:
                mTasksView.showNoTasks();
                break;
        }
    }

    @Override
    public void addNewTask() {
        mTasksView.showAddTask();
    }

    @Override
    public void openTaskDetails(@NonNull Task requestedTask) {
        checkNotNull(requestedTask, "requestedTask cannot be null!");
        mTasksView.showTaskDetailsUi(requestedTask.getId());
    }

    @Override
    public void completeTask(@NonNull Task completedTask) {
        checkNotNull(completedTask, "completedTask cannot be null!");
        mUseCaseHandler.execute(mCompleteTask, new CompleteTask.RequestValues(
                        completedTask.getId()),
                new UseCase.UseCaseCallback<CompleteTask.ResponseValue>() {
                    @Override
                    public void onSuccess(CompleteTask.ResponseValue response) {
                        mTasksView.showTaskMarkedComplete();
                        loadTasks(false, false);
                    }

                    @Override
                    public void onError() {
                        mTasksView.showLoadingTasksError();
                    }
                });
    }

    @Override
    public void activateTask(@NonNull Task activeTask) {
        checkNotNull(activeTask, "activeTask cannot be null!");
        mUseCaseHandler.execute(mActivateTask, new ActivateTask.RequestValues(activeTask.getId()),
                new UseCase.UseCaseCallback<ActivateTask.ResponseValue>() {
                    @Override
                    public void onSuccess(ActivateTask.ResponseValue response) {
                        mTasksView.showTaskMarkedActive();
                        loadTasks(false, false);
                    }

                    @Override
                    public void onError() {
                        mTasksView.showLoadingTasksError();
                    }
                });
    }

    @Override
    public void clearCompletedTasks() {
        mUseCaseHandler.execute(mClearCompleteTasks, new ClearCompleteTasks.RequestValues(),
                new UseCase.UseCaseCallback<ClearCompleteTasks.ResponseValue>() {
                    @Override
                    public void onSuccess(ClearCompleteTasks.ResponseValue response) {
                        mTasksView.showCompletedTasksCleared();
                        loadTasks(false, false);
                    }

                    @Override
                    public void onError() {
                        mTasksView.showLoadingTasksError();
                    }
                });
    }

    /**
     * Sets the current task filtering type.
     *
     * @param requestType Can be {@link TasksFilterType#ALL_TASKS},
     *                    {@link TasksFilterType#COMPLETED_TASKS}, or
     *                    {@link TasksFilterType#ACTIVE_TASKS}
     */
    @Override
    public void setFiltering(TasksFilterType requestType) {
        mCurrentFiltering = requestType;
    }

    @Override
    public TasksFilterType getFiltering() {
        return mCurrentFiltering;
    }

}
