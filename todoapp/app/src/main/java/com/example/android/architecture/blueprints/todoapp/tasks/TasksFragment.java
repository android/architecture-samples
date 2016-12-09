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
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.example.android.architecture.blueprints.todoapp.Injection;
import com.example.android.architecture.blueprints.todoapp.R;
import com.example.android.architecture.blueprints.todoapp.ScrollChildSwipeRefreshLayout;
import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskActivity;
import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;
import com.example.android.architecture.blueprints.todoapp.databinding.TaskItemBinding;
import com.example.android.architecture.blueprints.todoapp.databinding.TasksFragBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * Display a grid of {@link Task}s. User can choose to view all, active or completed tasks.
 */
public class TasksFragment extends Fragment {

    private TasksAdapter mListAdapter;

    private TasksViewModel mTasksViewModel;


    public TasksFragment() {
        // Requires empty public constructor
    }

    public static TasksFragment newInstance() {
        return new TasksFragment();
    }

    @Override
    public void onResume() {
        super.onResume();
        mTasksViewModel.start();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // If a task was successfully added, show snackbar
        if (AddEditTaskActivity.REQUEST_ADD_TASK == requestCode
                && Activity.RESULT_OK == resultCode) {
            // TODO looks weird.
            mTasksViewModel.snackbar.get().showMessage(getString(R.string.successfully_saved_task_message));
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        TasksFragBinding tasksFragBinding = TasksFragBinding.inflate(inflater, container, false);

        tasksFragBinding.setView(this);

        tasksFragBinding.setViewmodel(mTasksViewModel);

        // Set up tasks view
        ListView listView = tasksFragBinding.tasksList;

        mListAdapter = new TasksAdapter(new ArrayList<Task>(0), (TaskItemNavigator) getActivity(),
                Injection.provideTasksRepository(getContext().getApplicationContext()));
        listView.setAdapter(mListAdapter);

        // Set up floating action button
        FloatingActionButton fab =
                (FloatingActionButton) getActivity().findViewById(R.id.fab_add_task);

        fab.setImageResource(R.drawable.ic_add);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTasksViewModel.addNewTask();
            }
        });

        // Set up progress indicator
        final ScrollChildSwipeRefreshLayout swipeRefreshLayout = tasksFragBinding.refreshLayout;
        swipeRefreshLayout.setColorSchemeColors(
                ContextCompat.getColor(getActivity(), R.color.colorPrimary),
                ContextCompat.getColor(getActivity(), R.color.colorAccent),
                ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark)
        );
        // Set the scrolling view in the custom SwipeRefreshLayout.
        swipeRefreshLayout.setScrollUpChild(listView);

        setHasOptionsMenu(true);

        View root = tasksFragBinding.getRoot();

        return root;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_clear:
                mTasksViewModel.clearCompletedTasks();
                break;
            case R.id.menu_filter:
                showFilteringPopUpMenu();
                break;
            case R.id.menu_refresh:
                mTasksViewModel.loadTasks(true);
                break;
        }
        return true;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.tasks_fragment_menu, menu);
    }

    public void setViewModel(TasksViewModel viewModel) {
        mTasksViewModel = viewModel;
    }

    private void showFilteringPopUpMenu() {
        PopupMenu popup = new PopupMenu(getContext(), getActivity().findViewById(R.id.menu_filter));
        popup.getMenuInflater().inflate(R.menu.filter_tasks, popup.getMenu());

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.active:

                        mTasksViewModel.setFiltering(TasksFilterType.ACTIVE_TASKS);
                        break;
                    case R.id.completed:
                        mTasksViewModel.setFiltering(TasksFilterType.COMPLETED_TASKS);
                        break;
                    default:
                        mTasksViewModel.setFiltering(TasksFilterType.ALL_TASKS);
                        break;
                }
                mTasksViewModel.loadTasks(false);
                return true;
            }
        });

        popup.show();
    }

    public void setLoadingIndicator(final boolean active) {

        if (getView() == null) {
            return;
        }
        final SwipeRefreshLayout srl =
                (SwipeRefreshLayout) getView().findViewById(R.id.refresh_layout);

        // Make sure setRefreshing() is called after the layout is done with everything else.
        srl.post(new Runnable() {
            @Override
            public void run() {
                srl.setRefreshing(active);
            }
        });
    }

    public void showTasks(List<Task> tasks) {
        mListAdapter.replaceData(tasks);
    }

    public void showSuccessfullySavedMessage() {
        showMessage(getString(R.string.successfully_saved_task_message));
    }

    public void showAddTask() {//todo
    }

    public void showTaskMarkedComplete() {
        showMessage(getString(R.string.task_marked_complete));
    }

    public void showTaskMarkedActive() {
        showMessage(getString(R.string.task_marked_active));
    }

    public void showCompletedTasksCleared() {
        showMessage(getString(R.string.completed_tasks_cleared));
    }

    public void showLoadingTasksError() {
        showMessage(getString(R.string.loading_tasks_error));
    }

    private void showMessage(String message) {
        Snackbar.make(getView(), message, Snackbar.LENGTH_LONG).show();
    }

    public boolean isActive() {
        return isAdded();
    }

    public static class TasksAdapter extends BaseAdapter {

        private final TaskItemNavigator mTaskItemNavigator;

        private List<Task> mTasks;

        private TasksRepository mTasksRepository;


        public TasksAdapter(List<Task> tasks, TaskItemNavigator taskItemNavigator,
                            TasksRepository tasksRepository) {
            mTaskItemNavigator = taskItemNavigator;
            mTasksRepository = tasksRepository;
            setList(tasks);
        }

        public void replaceData(List<Task> tasks) {
            setList(tasks);
        }

        private void setList(List<Task> tasks) {
            mTasks = tasks;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mTasks != null ? mTasks.size() : 0;
        }

        @Override
        public Task getItem(int i) {
            return mTasks.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            Task task = getItem(i);
            TaskItemBinding binding;
            if (view == null) {
                // Inflate
                LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());

                // Create the binding
                binding = TaskItemBinding.inflate(inflater, viewGroup, false);

                // Create view model
                TaskItemViewModel taskItemViewModel =
                        new TaskItemViewModel(
                                viewGroup.getContext().getApplicationContext(),
                                mTasksRepository,
                                mTaskItemNavigator);
                binding.setViewmodel(taskItemViewModel);
                taskItemViewModel.start(task.getId());
            } else {
                // Recycling view
                binding = DataBindingUtil.getBinding(view);
                TaskItemViewModel viewmodel = binding.getViewmodel();
                viewmodel.start(task.getId());
            }

            binding.executePendingBindings();
            return binding.getRoot();
        }
    }
}
