/*
 * Copyright (C) 2019 The Android Open Source Project
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
package com.example.android.architecture.blueprints.todoapp.tasks

import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.databinding.TasksActBinding

/**
 * Main activity for the todoapp. Holds the Navigation Host Fragment and the Drawer, Toolbar, etc.
 */
class TasksActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var appBarConfiguration: AppBarConfiguration
    lateinit var binding: TasksActBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = TasksActBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupNavigationDrawer()
        setSupportActionBar(binding.toolbar)

        val navController: NavController = findNavController(R.id.nav_host_fragment)
        appBarConfiguration =
                AppBarConfiguration.Builder(R.id.tasks_fragment_dest, R.id.statistics_fragment_dest)
                        .setDrawerLayout(drawerLayout)
                        .build()
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navView
                .setupWithNavController(navController)
    }

    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.nav_host_fragment).navigateUp(appBarConfiguration) ||
                super.onSupportNavigateUp()
    }

    private fun setupNavigationDrawer() {
        drawerLayout = (binding.drawerLayout)
                .apply {
                    setStatusBarBackground(R.color.colorPrimaryDark)
                }
    }
}

// Keys for navigation
const val ADD_EDIT_RESULT_OK = Activity.RESULT_FIRST_USER + 1
const val DELETE_RESULT_OK = Activity.RESULT_FIRST_USER + 2
const val EDIT_RESULT_OK = Activity.RESULT_FIRST_USER + 3
