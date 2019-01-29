/*
 * Copyright (C) 2017 The Android Open Source Project
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
package com.example.android.architecture.blueprints.todoapp.statistics

import android.os.Bundle
import com.google.android.material.navigation.NavigationView
import androidx.core.app.NavUtils
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import android.view.MenuItem

import com.example.android.architecture.blueprints.todoapp.Injection
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.util.replaceFragmentInActivity
import com.example.android.architecture.blueprints.todoapp.util.setupActionBar

/**
 * Show statistics for tasks.
 */
class StatisticsActivity : AppCompatActivity() {

    private lateinit var drawerLayout: androidx.drawerlayout.widget.DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.statistics_act)

        // Set up the toolbar.
        setupActionBar(R.id.toolbar) {
            setTitle(R.string.statistics_title)
            setHomeAsUpIndicator(R.drawable.ic_menu)
            setDisplayHomeAsUpEnabled(true)
        }

        // Set up the navigation drawer.
        drawerLayout = (findViewById<androidx.drawerlayout.widget.DrawerLayout>(R.id.drawer_layout)).apply {
            setStatusBarBackground(R.color.colorPrimaryDark)
        }
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        setupDrawerContent(navigationView)

        val statisticsFragment = supportFragmentManager
                .findFragmentById(R.id.contentFrame) as StatisticsFragment?
                ?: StatisticsFragment.newInstance().also {
            replaceFragmentInActivity(it, R.id.contentFrame)
        }

        StatisticsPresenter(
                Injection.provideTasksRepository(applicationContext), statisticsFragment)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            // Open the navigation drawer when the home icon is selected from the toolbar.
            drawerLayout.openDrawer(GravityCompat.START)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupDrawerContent(navigationView: NavigationView) {
        navigationView.setNavigationItemSelectedListener { menuItem ->
            if (menuItem.itemId == R.id.list_navigation_menu_item) {
                NavUtils.navigateUpFromSameTask(this@StatisticsActivity)
            }
            // Close the navigation drawer when an item is selected.
            menuItem.isChecked = true
            drawerLayout.closeDrawers()
            true
        }
    }
}
