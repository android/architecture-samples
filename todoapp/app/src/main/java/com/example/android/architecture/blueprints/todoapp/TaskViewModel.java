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

package com.example.android.architecture.blueprints.todoapp;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.Observable;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.support.annotation.Nullable;

import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;

import static android.R.attr.description;


/**
 * Abstract class for View Models that expose a single {@link Task}.
 */
public abstract class TaskViewModel extends ViewModel
        implements TasksDataSource.GetTaskCallback {
    public final MutableLiveData<String> snackbarText = new MutableLiveData<>();

    public final ObservableField<String> title = new ObservableField<>();

    public final ObservableField<String> titleForList = new ObservableField<>();

    public final ObservableField<String> description = new ObservableField<>();

    private final ObservableField<Task> mTaskObservable = new ObservableField<>();

    private final TasksRepository mTasksRepository;

    private final Context mContext;

    public final ObservableBoolean dataLoading = new ObservableBoolean();

    public final ObservableBoolean completed = new ObservableBoolean();

    public final ObservableBoolean dataAvailable = new ObservableBoolean();

    public TaskViewModel(Context context, TasksRepository tasksRepository) {
        mContext = context.getApplicationContext(); // Force use of Application Context.
        mTasksRepository = tasksRepository;

        // Exposed observables depend on the mTaskObservable observable:
        mTaskObservable.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable observable, int i) {
                Task task = mTaskObservable.get();
                notifyChange(); // earlier for the @Bindable properties. Was being used in onTaskLoaded. Now it has a custom implementation
                if (task != null) {
                    title.set(task.getTitle());
                    description.set(task.getDescription());
                } else {
                    title.set(mContext.getString(R.string.no_data));
                    description.set(mContext.getString(R.string.no_data_description));
                }
            }
        });

        completed.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable observable, int i) {
                if (dataLoading.get()) {
                    return;
                }
                Task task = mTaskObservable.get();
                // Update the entity
                task.setCompleted(completed.get());

                // Notify repository and user
                if (completed.get()) {
                    mTasksRepository.completeTask(task);
                    snackbarText.setValue(mContext.getResources().getString(R.string.task_marked_complete));
                } else {
                    mTasksRepository.activateTask(task);
                    snackbarText.setValue(mContext.getResources().getString(R.string.task_marked_active));
                }

            }
        });
    }

    public void start(String taskId) {
        if (taskId != null) {
            dataLoading.set(true);
            mTasksRepository.getTask(taskId, this);
        }
    }

    public void setTask(Task task) {
        mTaskObservable.set(task);
    }

    @Override
    public void onTaskLoaded(Task task) {
        mTaskObservable.set(task);
        dataLoading.set(false);
    }

    public void notifyChange() {
        titleForList.set(mTaskObservable.get() == null ?
                "No Data" :
                mTaskObservable.get().getTitleForList()
        );
        if (mTaskObservable.get() != null) {
            completed.set(mTaskObservable.get().isCompleted());
        }
        dataAvailable.set(mTaskObservable.get() != null);
    }

    @Override
    public void onDataNotAvailable() {
        mTaskObservable.set(null);
        dataLoading.set(false);
    }

    public void deleteTask() {
        if (mTaskObservable.get() != null) {
            mTasksRepository.deleteTask(mTaskObservable.get().getId());
        }
    }

    public void onRefresh() {
        if (mTaskObservable.get() != null) {
            start(mTaskObservable.get().getId());
        }
    }

    public String getSnackbarText() {
        return snackbarText.getValue();
    }

    @Nullable
    protected String getTaskId() {
        return mTaskObservable.get().getId();
    }
}
