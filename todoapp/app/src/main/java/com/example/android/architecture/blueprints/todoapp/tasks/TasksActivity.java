/*
 * Copyright (C) 2015 The Android Open Source Project
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

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.support.design.widget.NavigationView;
import android.support.test.espresso.IdlingResource;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.example.android.architecture.blueprints.todoapp.Injection;
import com.example.android.architecture.blueprints.todoapp.R;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;
import com.example.android.architecture.blueprints.todoapp.statistics.StatisticsFragment;
import com.example.android.architecture.blueprints.todoapp.statistics.StatisticsPresenter;
import com.example.android.architecture.blueprints.todoapp.util.ActivityUtils;
import com.example.android.architecture.blueprints.todoapp.util.EspressoIdlingResource;

public class TasksActivity extends AppCompatActivity {

    private static final String CURRENT_FILTERING_KEY = "CURRENT_FILTERING_KEY";

    private DrawerLayout mDrawerLayout;

    private TasksPresenter mTasksPresenter;

    private Bundle savedInstanceState;

    private ActionBar ab;

    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tasks_act);
        this.savedInstanceState = savedInstanceState;

        // Set up the toolbar.
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ab = getSupportActionBar();
        if (ab != null) {
            ab.setHomeAsUpIndicator(R.drawable.ic_menu);
            ab.setDisplayHomeAsUpEnabled(true);
        }

        // Set up the navigation drawer.
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setStatusBarBackground(R.color.colorPrimaryDark);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {
            setupDrawerContent(navigationView);
        }

        showTasksFragment(savedInstanceState);
    }

    private void showTasksFragment(Bundle savedInstanceState) {
        Fragment tasksFragment = getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        if (tasksFragment == null || !(tasksFragment instanceof TasksFragment)) {
            // Create the fragment(View)
            tasksFragment = TasksFragment.newInstance();
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), tasksFragment, R.id.contentFrame);
        }

        //Create the Repository
        TasksRepository tasksRepository = Injection.provideTasksRepository(getApplicationContext());

        // Create the presenter
        mTasksPresenter = new TasksPresenter(tasksRepository, (TasksFragment) tasksFragment);

        // Load previously saved state, if available.
        if (savedInstanceState != null) {
            TasksFilterType currentFiltering = (TasksFilterType) savedInstanceState.getSerializable(CURRENT_FILTERING_KEY);
            mTasksPresenter.setFiltering(currentFiltering);
        }

        MenuItem mi1 = navigationView.getMenu().getItem(0);
        MenuItem mi2 = navigationView.getMenu().getItem(1);
        mi1.setChecked(true);
        mi2.setChecked(false);
    }

    private void showStatisticsFragment() {
        Fragment statisticsFragment = getSupportFragmentManager().findFragmentById(R.id.contentFrame);

        if (statisticsFragment == null || !(statisticsFragment instanceof StatisticsFragment)) {
            statisticsFragment = StatisticsFragment.newInstance();
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), statisticsFragment, R.id.contentFrame);
        }

        TasksRepository tasksRepository = Injection.provideTasksRepository(getApplicationContext());

        new StatisticsPresenter(tasksRepository, (StatisticsFragment) statisticsFragment);

        MenuItem mi1 = navigationView.getMenu().getItem(0);
        MenuItem mi2 = navigationView.getMenu().getItem(1);
        mi1.setChecked(false);
        mi2.setChecked(true);

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(CURRENT_FILTERING_KEY, mTasksPresenter.getFiltering());

        super.onSaveInstanceState(outState);
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

    private void setupDrawerContent(final NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.list_navigation_menu_item:
                                if (ab != null)
                                    ab.setTitle(R.string.app_name);
                                showTasksFragment(savedInstanceState);


                                break;
                            case R.id.statistics_navigation_menu_item:
                                if (ab != null)
                                    ab.setTitle(R.string.statistics_title);
                                showStatisticsFragment();

                                break;
                            default:
                                break;
                        }
                        // Close the navigation drawer when an item is selected.
                        mDrawerLayout.closeDrawers();
                        return true;
                    }
                });
    }

    @VisibleForTesting
    public IdlingResource getCountingIdlingResource() {
        return EspressoIdlingResource.getIdlingResource();
    }
}
