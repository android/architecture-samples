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

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.test.espresso.IdlingResource;

import com.example.android.architecture.blueprints.todoapp.Injection;
import com.example.android.architecture.blueprints.todoapp.R;
import com.example.android.architecture.blueprints.todoapp.ViewModelHolder;
import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskActivity;
import com.example.android.architecture.blueprints.todoapp.statistics.StatisticsActivity;
import com.example.android.architecture.blueprints.todoapp.taskdetail.TaskDetailActivity;
import com.example.android.architecture.blueprints.todoapp.util.ActivityUtils;
import com.example.android.architecture.blueprints.todoapp.util.EspressoIdlingResource;
import com.google.android.material.navigation.NavigationView;


public class TasksActivity extends AppCompatActivity implements TaskItemNavigator, TasksNavigator {

    private DrawerLayout mDrawerLayout;

    public static final String TASKS_VIEWMODEL_TAG = "TASKS_VIEWMODEL_TAG";

    private TasksViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tasks_act);

        setupToolbar();

        setupNavigationDrawer();

        TasksFragment tasksFragment = findOrCreateViewFragment();

        mViewModel = findOrCreateViewModel();
        mViewModel.setNavigator(this);

        // Link View and ViewModel
        tasksFragment.setViewModel(mViewModel);
    }

    @Override
    protected void onDestroy() {
        mViewModel.onActivityDestroyed();
        super.onDestroy();
    }

    private TasksViewModel findOrCreateViewModel() {
        // In a configuration change we might have a ViewModel present. It's retained using the
        // Fragment Manager.
        @SuppressWarnings("unchecked")
        ViewModelHolder<TasksViewModel> retainedViewModel =
                (ViewModelHolder<TasksViewModel>) getSupportFragmentManager()
                        .findFragmentByTag(TASKS_VIEWMODEL_TAG);

        if (retainedViewModel != null && retainedViewModel.getViewmodel() != null) {
            // If the model was retained, return it.
            return retainedViewModel.getViewmodel();
        } else {
            // There is no ViewModel yet, create it.
            TasksViewModel viewModel = new TasksViewModel(
                    Injection.provideTasksRepository(getApplicationContext()),
                    getApplicationContext());
            // and bind it to this Activity's lifecycle using the Fragment Manager.
            ActivityUtils.addFragmentToActivity(
                    getSupportFragmentManager(),
                    ViewModelHolder.createContainer(viewModel),
                    TASKS_VIEWMODEL_TAG);
            return viewModel;
        }
    }

    @NonNull
    private TasksFragment findOrCreateViewFragment() {
        TasksFragment tasksFragment =
                (TasksFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        if (tasksFragment == null) {
            // Create the fragment
            tasksFragment = TasksFragment.newInstance();
            ActivityUtils.addFragmentToActivity(
                    getSupportFragmentManager(), tasksFragment, R.id.contentFrame);
        }
        return tasksFragment;
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.ic_menu);
        ab.setDisplayHomeAsUpEnabled(true);
    }

    private void setupNavigationDrawer() {
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerLayout.setStatusBarBackground(R.color.colorPrimaryDark);
        NavigationView navigationView = findViewById(R.id.nav_view);
        if (navigationView != null) {
            setupDrawerContent(navigationView);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Open the navigation drawer when the home icon is selected from the toolbar.
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.list_navigation_menu_item:
                                // Do nothing, we're already on that screen
                                break;
                            case R.id.statistics_navigation_menu_item:
                                Intent intent =
                                        new Intent(TasksActivity.this, StatisticsActivity.class);
                                startActivity(intent);
                                break;
                            default:
                                break;
                        }
                        // Close the navigation drawer when an item is selected.
                        menuItem.setChecked(true);
                        mDrawerLayout.closeDrawers();
                        return true;
                    }
                });
    }

    @VisibleForTesting
    public IdlingResource getCountingIdlingResource() {
        return EspressoIdlingResource.getIdlingResource();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mViewModel.handleActivityResult(requestCode, resultCode);
    }

    @Override
    public void openTaskDetails(String taskId) {
        Intent intent = new Intent(this, TaskDetailActivity.class);
        intent.putExtra(TaskDetailActivity.EXTRA_TASK_ID, taskId);
        startActivityForResult(intent, AddEditTaskActivity.REQUEST_CODE);

    }

    @Override
    public void addNewTask() {
        Intent intent = new Intent(this, AddEditTaskActivity.class);
        startActivityForResult(intent, AddEditTaskActivity.REQUEST_CODE);
    }
}
