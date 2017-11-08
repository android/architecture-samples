/*
 * Copyright 2017, The Android Open Source Project
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
package com.example.android.architecture.blueprints.todoapp.addedittask

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.util.ADD_EDIT_RESULT_OK
import com.example.android.architecture.blueprints.todoapp.util.obtainViewModel
import com.example.android.architecture.blueprints.todoapp.util.replaceFragmentInActivity
import com.example.android.architecture.blueprints.todoapp.util.setupActionBar

/**
 * Displays an add or edit task screen.
 */
class AddEditTaskActivity : AppCompatActivity(), AddEditTaskNavigator {

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onTaskSaved() {
        setResult(ADD_EDIT_RESULT_OK)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.addtask_act)

        setupActionBar(R.id.toolbar) {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        replaceFragmentInActivity(obtainViewFragment(), R.id.contentFrame)

        subscribeToNavigationChanges()
    }

    private fun subscribeToNavigationChanges() {
        // The activity observes the navigation events in the ViewModel
        obtainViewModel().taskUpdatedEvent.observe(this, Observer {
            this@AddEditTaskActivity.onTaskSaved()
        })
    }

    private fun obtainViewFragment() = supportFragmentManager.findFragmentById(R.id.contentFrame) ?:
            AddEditTaskFragment.newInstance().apply {
                arguments = Bundle().apply {
                    putString(AddEditTaskFragment.ARGUMENT_EDIT_TASK_ID,
                            intent.getStringExtra(AddEditTaskFragment.ARGUMENT_EDIT_TASK_ID))
                }
            }

    fun obtainViewModel(): AddEditTaskViewModel = obtainViewModel(AddEditTaskViewModel::class.java)

    companion object {
        const val REQUEST_CODE = 1
    }
}