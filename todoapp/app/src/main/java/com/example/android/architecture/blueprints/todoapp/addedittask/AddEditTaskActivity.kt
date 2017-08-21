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

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import com.example.android.architecture.blueprints.todoapp.Injection
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.util.addFragment

/**
 * Displays an add or edit task screen.
 */
class AddEditTaskActivity : AppCompatActivity() {

    private lateinit var addEditTaskPresenter: AddEditTaskPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.addtask_act)

        // Set up the toolbar.
        setSupportActionBar(findViewById<Toolbar>(R.id.toolbar))
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        val taskId = intent.getStringExtra(AddEditTaskFragment.ARGUMENT_EDIT_TASK_ID)
        setToolbarTitle(taskId)
        val addEditTaskFragment = getFragment(taskId)

        var shouldLoadDataFromRepo = true

        // Prevent the presenter from loading data from the repository if this is a config change.
        if (savedInstanceState != null) {
            // Data might not have loaded when the config change happen, so we saved the state.
            shouldLoadDataFromRepo = savedInstanceState.getBoolean(SHOULD_LOAD_DATA_FROM_REPO_KEY)
        }

        // Create the presenter
        addEditTaskPresenter = AddEditTaskPresenter(
                taskId,
                Injection.provideTasksRepository(applicationContext),
                addEditTaskFragment,
                shouldLoadDataFromRepo)
    }

    private fun setToolbarTitle(taskId: String?) {
        supportActionBar?.apply {
            setTitle(if (taskId == null) R.string.add_task else R.string.edit_task)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        // Save the state so that next time we know if we need to refresh data.
        super.onSaveInstanceState(outState.apply {
            putBoolean(SHOULD_LOAD_DATA_FROM_REPO_KEY, addEditTaskPresenter.isDataMissing)
        })
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    fun getFragment(taskId: String?) =
            supportFragmentManager.findFragmentById(R.id.contentFrame) as AddEditTaskFragment? ?:
                    AddEditTaskFragment.newInstance().also {
                        if (intent.hasExtra(AddEditTaskFragment.ARGUMENT_EDIT_TASK_ID)) {
                            it.arguments = Bundle().apply {
                                putString(AddEditTaskFragment.ARGUMENT_EDIT_TASK_ID, taskId)
                            }
                        }
                        addFragment(it, R.id.contentFrame)
                    }

    companion object {
        @JvmField val SHOULD_LOAD_DATA_FROM_REPO_KEY = "SHOULD_LOAD_DATA_FROM_REPO_KEY"
        @JvmField val REQUEST_ADD_TASK = 1
    }
}
