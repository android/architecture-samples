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

package com.example.android.architecture.blueprints.todoapp.taskdetail;

/**
 * Listens to user actions from the UI ({@link TaskDetailFragment}), retrieves the data and updates
 * the UI as required.
 */
//public class TaskDetailPresenter implements TaskDetailContract.Presenter {
//
//    private final TaskDetailContract.View mTaskDetailView;
//
//    private TasksRepository mRepository;
//
//    @NonNull
//    private String mTaskId;
//
//    public TaskDetailPresenter(@NonNull String taskId, TasksRepository repository,
//            @NonNull TaskDetailContract.View view) {
//        mRepository = repository;
//        mTaskDetailView = view;
//        mTaskId = taskId;
//
//        mTaskDetailView.setPresenter(this);
//    }
//
//
//    @Override
//    public void start() {
//        getTask();
//    }
//
//    @Override
//    public void getTask() {
//        mRepository.getTask(mTaskId, new TasksDataSource.GetTaskCallback() {
//            @Override
//            public void onTaskLoaded(Task task) {
//                // The view may not be able to handle UI updates anymore
//                if (!mTaskDetailView.isActive()) {
//                    return;
//                }
//                if (task != null) {
//                    mTaskDetailView.showTask(task);
//                } else {
//                    mTaskDetailView.showError();
//                }
//            }
//
//            @Override
//            public void onDataNotAvailable() {
//                // The view may not be able to handle UI updates anymore
//                if (!mTaskDetailView.isActive()) {
//                    return;
//                }
//                mTaskDetailView.showError();
//            }
//        });
//    }
//
//    @Override
//    public void deleteTask() {
//        mRepository.deleteTask(mTaskId);
//        mTaskDetailView.showTaskDeleted();
//    }
//
//    /**
//     * Called by the Data Binding library.
//     */
//    @Override
//    public void completeChanged(Task task, boolean isChecked) {
//        task.setCompleted(isChecked);
//        if (isChecked) {
//            mRepository.completeTask(task);
//            mTaskDetailView.showTaskMarkedComplete();
//        } else {
//            mRepository.activateTask(task);
//            mTaskDetailView.showTaskMarkedActive();
//        }
//    }
//}
