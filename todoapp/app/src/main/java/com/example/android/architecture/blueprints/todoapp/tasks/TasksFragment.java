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

import android.databinding.DataBindingUtil;
import android.databinding.Observable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
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
import com.example.android.architecture.blueprints.todoapp.SnackBarChangedCallback;
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

    private TasksViewModel mTasksViewModel;

    private SnackBarChangedCallback mSnackBarChangedCallback;

    private TasksFragBinding mTasksFragBinding;

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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mTasksFragBinding = TasksFragBinding.inflate(inflater, container, false);

        mTasksFragBinding.setView(this);

        mTasksFragBinding.setViewmodel(mTasksViewModel);

        setHasOptionsMenu(true);

        View root = mTasksFragBinding.getRoot();

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

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Set up tasks view
        ListView listView = mTasksFragBinding.tasksList;

        // SnackBar setup
        mSnackBarChangedCallback = new SnackBarChangedCallback(getView(), mTasksViewModel);
        mTasksViewModel.snackBarText.addOnPropertyChangedCallback(mSnackBarChangedCallback);

        TasksAdapter mListAdapter = new TasksAdapter(
                new ArrayList<Task>(0),
                (TaskItemNavigator) getActivity(),
                Injection.provideTasksRepository(getContext().getApplicationContext()),
                mSnackBarChangedCallback,
                mTasksViewModel);
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
        final ScrollChildSwipeRefreshLayout swipeRefreshLayout = mTasksFragBinding.refreshLayout;
        swipeRefreshLayout.setColorSchemeColors(
                ContextCompat.getColor(getActivity(), R.color.colorPrimary),
                ContextCompat.getColor(getActivity(), R.color.colorAccent),
                ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark)
        );
        // Set the scrolling view in the custom SwipeRefreshLayout.
        swipeRefreshLayout.setScrollUpChild(listView);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // If the ViewModel
        mTasksViewModel.snackBarText.removeOnPropertyChangedCallback(mSnackBarChangedCallback);
    }

    public static class TasksAdapter extends BaseAdapter {

        private final TaskItemNavigator mTaskItemNavigator;

        private List<Task> mTasks;

        private TasksRepository mTasksRepository;

        private final SnackBarChangedCallback mSnackBarChangedCallback;

        private final TasksViewModel mTasksViewModel;

        public TasksAdapter(List<Task> tasks, TaskItemNavigator taskItemNavigator,
                            TasksRepository tasksRepository,
                            SnackBarChangedCallback snackBarChangedCallback,
                            TasksViewModel tasksViewModel) {
            mTaskItemNavigator = taskItemNavigator;
            mTasksRepository = tasksRepository;
            mSnackBarChangedCallback = snackBarChangedCallback;
            mTasksViewModel = tasksViewModel;
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
            } else {
                // Recycling view
                binding = DataBindingUtil.getBinding(view);
            }

            final TaskItemViewModel viewmodel = new TaskItemViewModel(
                    viewGroup.getContext().getApplicationContext(),
                    mTasksRepository,
                    mTaskItemNavigator);
            binding.setViewmodel(viewmodel);
            // To save on PropertyChangedCallbacks, wire the item's snackbar text observable to the
            // fragment's.
            viewmodel.snackBarText.addOnPropertyChangedCallback(
                    new Observable.OnPropertyChangedCallback() {
                @Override
                public void onPropertyChanged(Observable observable, int i) {
                    mTasksViewModel.snackBarText.set(viewmodel.getSnackBarText());
                }
            });
            viewmodel.start(task.getId());

            //binding.executePendingBindings();
            return binding.getRoot();
        }

    }
}
