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
package com.example.android.architecture.blueprints.todoapp.util

/**
 * Various extension functions for AppCompatActivity.
 */

import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import androidx.annotation.IdRes
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import com.example.android.architecture.blueprints.todoapp.TodoApplication
import com.example.android.architecture.blueprints.todoapp.ViewModelFactory


const val ADD_EDIT_RESULT_OK = Activity.RESULT_FIRST_USER + 1
const val DELETE_RESULT_OK = Activity.RESULT_FIRST_USER + 2
const val EDIT_RESULT_OK = Activity.RESULT_FIRST_USER + 3

fun AppCompatActivity.setupActionBar(@IdRes toolbarId: Int, action: ActionBar.() -> Unit) {
    setSupportActionBar(findViewById(toolbarId))
    supportActionBar?.run {
        action()
    }
}

fun <T : ViewModel> Fragment.obtainViewModel(viewModelClass: Class<T>): T {
    val repository = (requireContext().applicationContext as TodoApplication).taskRepository
    return ViewModelProviders.of(this, ViewModelFactory(repository)).get(viewModelClass)
}

private fun AppCompatActivity.rotateToLandscape() {
    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
}

private fun AppCompatActivity.rotateToPortrait() {
    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
}

fun AppCompatActivity.rotateOrientation() {
    when (resources.configuration.orientation) {
        Configuration.ORIENTATION_LANDSCAPE -> rotateToPortrait()
        Configuration.ORIENTATION_PORTRAIT -> rotateToLandscape()
        else -> rotateToLandscape()
    }
}