/*
 *  Copyright 2017 Google Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.example.android.architecture.blueprints.todoapp.tasks;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;

import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.databinding.TaskItemBinding;

import java.util.List;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.LifecycleOwner;


public class TasksAdapter extends BaseAdapter {

    private final TasksViewModel mTasksViewModel;

    private List<Task> mTasks;

    private LifecycleOwner mLifecycleOwner;

    public TasksAdapter(List<Task> tasks,
            TasksViewModel tasksViewModel, LifecycleOwner activity) {
        mTasksViewModel = tasksViewModel;
        setList(tasks);
        mLifecycleOwner = activity;

    }

    public void replaceData(List<Task> tasks) {
        setList(tasks);
    }

    @Override
    public int getCount() {
        return mTasks != null ? mTasks.size() : 0;
    }

    @Override
    public Task getItem(int position) {
        return mTasks.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, final View view, final ViewGroup viewGroup) {
        TaskItemBinding binding;
        if (view == null) {
            // Inflate
            LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());

            // Create the binding
            binding = TaskItemBinding.inflate(inflater, viewGroup, false);
        } else {
            // Recycling view
            binding = DataBindingUtil.getBinding(view);
        }

        TaskItemUserActionsListener userActionsListener = new TaskItemUserActionsListener() {
            @Override
            public void onCompleteChanged(Task task, View v) {
                boolean checked = ((CheckBox)v).isChecked();
                mTasksViewModel.completeTask(task, checked);
            }

            @Override
            public void onTaskClicked(Task task) {
                mTasksViewModel.openTask(task.getId());
            }
        };

        binding.setTask(mTasks.get(position));
        binding.setLifecycleOwner(mLifecycleOwner);

        binding.setListener(userActionsListener);

        binding.executePendingBindings();
        return binding.getRoot();
    }


    private void setList(List<Task> tasks) {
        mTasks = tasks;
        notifyDataSetChanged();
    }
}
